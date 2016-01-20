/*
 * Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidoctor.maven;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.maven.monitor.LoggingFileAlterationListenerAdaptor;
import org.asciidoctor.maven.monitor.SynchronizingFileAlterationListenerAdaptor;
import org.asciidoctor.maven.io.AsciidoctorFileScanner;

@Mojo(name = "auto-refresh")
public class AsciidoctorRefreshMojo extends AsciidoctorMojo {
    public static final String PREFIX = AsciidoctorMaven.PREFIX + "refresher.";
    @Parameter(property = PREFIX + "port", required = false)
    protected int port = 2000;

    @Parameter(property = PREFIX + "interval", required = false)
    protected int interval = 2000; // 2s

    private Future<Asciidoctor> asciidoctor = null;
    private Collection<FileAlterationMonitor> monitors = null;

    private final AtomicBoolean needsUpdate = new AtomicBoolean(false);
    private ScheduledExecutorService updaterScheduler = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // this is long because of JRuby startup
        createAsciidoctor();

        startPolling();
        startUpdater();

        doWork();

        stopUpdater();
        stopMonitor();
    }

    private void stopUpdater() {
        if (updaterScheduler != null) {
            updaterScheduler.shutdown();
        }
    }

    private void startUpdater() {
        updaterScheduler = Executors.newScheduledThreadPool(1);

        // we prevent refreshing more often than all 200ms and we refresh at least once/s
        // NOTE1: it is intended to avoid too much time space between file polling and re-rendering
        // NOTE2: if nothing to refresh it does nothing so all is fine
        updaterScheduler.scheduleAtFixedRate(new Updater(needsUpdate, this), 0, Math.min(1000, Math.max(200, interval / 2)), TimeUnit.MILLISECONDS);
    }

    protected void doWork() throws MojoFailureException, MojoExecutionException {
        getLog().info("Rendered doc in " + executeAndReturnDuration() + "ms");
        doWait();
    }

    protected void doWait() {
        getLog().info("Type [exit|quit] to exit and [refresh] to force a manual re-rendering.");

        String line;
        final Scanner scanner = new Scanner(System.in);
        while ((line = scanner.nextLine()) != null) {
            line = line.trim();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                break;
            }

            if ("refresh".equalsIgnoreCase(line)) {
                doExecute();
            } else {
                getLog().warn("'" + line + "' not understood, available commands are [quit, exit, refresh].");
            }
        }
    }

    private void stopMonitor() throws MojoExecutionException {
        if (monitors != null) {
            for (final FileAlterationMonitor monitor : monitors) {
                try {
                    monitor.stop();
                } catch (Exception e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }
    }

    protected synchronized void doExecute() {
        ensureOutputExists();

        // delete only content files, resources are synchronized so normally up to date
        for (final File f : FileUtils.listFiles(outputDirectory, new RegexFileFilter(ASCIIDOC_REG_EXP_EXTENSION), TrueFileFilter.INSTANCE)) {
            FileUtils.deleteQuietly(f);
        }

        try {
            getLog().info("Re-rendered doc in " + executeAndReturnDuration() + "ms");
        } catch (final MojoExecutionException e) {
            getLog().error(e);
        } catch (final MojoFailureException e) {
            getLog().error(e);
        }
    }

    protected long executeAndReturnDuration() throws MojoExecutionException, MojoFailureException {
        final long start = System.nanoTime();
        super.execute();
        final long end = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(end - start);
    }

    private void startPolling() throws MojoExecutionException {
        monitors = new ArrayList<FileAlterationMonitor>();

        { // content monitor
            List<Resource> _sources = getSources();
            // If sources is empty, create one with the default simplest options
            if (_sources == null || _sources.isEmpty()) {
                Resource defaultSource = new Resource();
                defaultSource.setDirectory(SOURCE_DIRECTORY);
                _sources = new ArrayList<Resource>();
                _sources.add(defaultSource);
            }

            for (Resource source: _sources) {
                // source is a directory -> create 1 monitor
                if (!StringUtils.isEmpty(source.getDirectory())
                        && (source.getIncludes() == null || source.getIncludes().isEmpty())
                        && (source.getExcludes() == null || source.getExcludes().isEmpty())) {

                    monitors.add(monitorDirectory(new File(source.getDirectory())));
                } // source is a list of files with includes/excludes -> create 1 monitor for each source file
                else {
                    // TODO find a solution with only 1 monitor. Maybe creating a custom FileFilter
                    AsciidoctorFileScanner scanner = new AsciidoctorFileScanner(buildContext);
                    for (File sourceFile: scanner.scan(source)) {
                        monitors.add(monitorFile(sourceFile));
                    }
                }
            }
        }

        { // resources monitors
            if (synchronizations != null) {
                for (final Synchronization s : synchronizations) {
                    final FileAlterationMonitor monitor = new FileAlterationMonitor(interval);
                    final FileAlterationListener listener = new SynchronizingFileAlterationListenerAdaptor(getLog(), needsUpdate, s);

                    final File source = s.getSource();

                    final FileAlterationObserver observer;
                    if (source.isDirectory()) {
                        observer = new FileAlterationObserver(source);
                    } else {
                        observer = new FileAlterationObserver(source.getParentFile(), new NameFileFilter(source.getName()));
                    }

                    observer.addListener(listener);
                    monitor.addObserver(observer);

                    monitors.add(monitor);
                }
            }
        }

        for (final FileAlterationMonitor monitor : monitors) {
            try {
                monitor.start();
            } catch (final Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a monitor on a directory
     *
     * @param directory directory to monitor
     * @return FileAlterationMonitor instance fully configured
     */
    private FileAlterationMonitor monitorDirectory (File directory) {
        final FileAlterationObserver observer;
        observer = new FileAlterationObserver(directory, new RegexFileFilter(ASCIIDOC_REG_EXP_EXTENSION));
        final FileAlterationMonitor monitor = new FileAlterationMonitor(interval);
        final FileAlterationListener listener = new LoggingFileAlterationListenerAdaptor(getLog(), needsUpdate);

        observer.addListener(listener);
        monitor.addObserver(observer);

        return monitor;
    }

    /**
     * Creates a monitor on a single file (not a directory)
     *
     * @param file file to monitor
     * @return FileAlterationMonitor instance fully configured
     */
    private FileAlterationMonitor monitorFile (File file) {
        final FileAlterationObserver observer;
        observer = new FileAlterationObserver(file.getParent(), new NameFileFilter(file.getName()));
        final FileAlterationMonitor monitor = new FileAlterationMonitor(interval);
        final FileAlterationListener listener = new LoggingFileAlterationListenerAdaptor(getLog(), needsUpdate);

        observer.addListener(listener);
        monitor.addObserver(observer);

        return monitor;
    }

    private void createAsciidoctor() {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        asciidoctor = es.submit(new Callable<Asciidoctor>() {
            @Override
            public Asciidoctor call() throws Exception {
                return Asciidoctor.Factory.create();
            }
        });
        es.shutdown();
    }

    private static class Updater implements Runnable {
        private final AtomicBoolean run;
        private final AsciidoctorRefreshMojo mojo;

        private Updater(final AtomicBoolean run, final AsciidoctorRefreshMojo mojo) {
            this.run = run;
            this.mojo = mojo;
        }

        @Override
        public void run() {
            if (run.get()) {
                run.set(false);
                mojo.doExecute();
            }
        }
    }
}
