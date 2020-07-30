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

import org.apache.commons.io.filefilter.*;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.maven.process.ResourcesProcessor;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

@Mojo(name = "auto-refresh")
public class AsciidoctorRefreshMojo extends AsciidoctorMojo {

    public static final String PREFIX = AsciidoctorMaven.PREFIX + "refresher.";

    private static final ResourcesProcessor EMPTY_RESOURCE_PROCESSOR =
            (sourcesRootDirectory, outputRootDirectory, encoding, configuration) -> {
            };

    @Parameter(property = PREFIX + "interval")
    protected int interval = 2000; // 2s

    private Collection<FileAlterationMonitor> monitors = null;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        startPolling();
        doWork();
        stopMonitors();
    }

    protected void doWork() throws MojoFailureException, MojoExecutionException {
        long timeInMillis = timed(() -> {
            try {
                processAllSources(defaultResourcesProcessor);
            } catch (MojoExecutionException e) {
                getLog().error(e);
            }
        });
        getLog().info("Converted document(s) in " + timeInMillis + "ms");
        doWait();
    }

    protected void doWait() throws MojoExecutionException, MojoFailureException {
        showWaitMessage();

        String line;
        final Scanner scanner = new Scanner(System.in);
        while ((line = scanner.nextLine()) != null) {
            line = line.trim();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                break;
            }

            if ("refresh".equalsIgnoreCase(line)) {
                doWork();
            } else {
                getLog().warn("'" + line + "' not understood, available commands are [quit, exit, refresh].");
            }
        }
    }

    private void showWaitMessage() {
        getLog().info("Type [exit|quit] to exit and [refresh] to force a manual re-conversion.");
    }

    private void stopMonitors() throws MojoExecutionException {
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

    private void startPolling() throws MojoExecutionException {

        { // content monitor
            final FileAlterationObserver observer = new FileAlterationObserver(sourceDirectory, buildFileFilter());
            final FileAlterationListener listener = new AsciidoctorConverterFileAlterationListenerAdaptor(this, () -> showWaitMessage(), getLog());
            final FileAlterationMonitor monitor = new FileAlterationMonitor(interval);

            observer.addListener(listener);
            monitor.addObserver(observer);

            monitors = Collections.singletonList(monitor);
        }

        for (final FileAlterationMonitor monitor : monitors) {
            try {
                monitor.start();
            } catch (final Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private IOFileFilter buildFileFilter() {
        if (sourceDocumentName != null)
            return new NameFileFilter(sourceDocumentName);

        if (!sourceDocumentExtensions.isEmpty()) {
            final StringJoiner stringJoiner = new StringJoiner("|", "^[^_.].*\\.(", ")$");
            for (String extension : sourceDocumentExtensions) {
                stringJoiner.add(extension);
            }
            return directoryRecursiveFileFilter(new RegexFileFilter(stringJoiner.toString()));
        }

        return directoryRecursiveFileFilter(new RegexFileFilter(ASCIIDOC_REG_EXP_EXTENSION));
    }

    private IOFileFilter directoryRecursiveFileFilter(AbstractFileFilter fileFilter) {
        return FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), fileFilter);
    }

    private class AsciidoctorConverterFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {

        private final AsciidoctorMojo mojo;
        private final Runnable postAction;
        private final Log log;

        private AsciidoctorConverterFileAlterationListenerAdaptor(AsciidoctorMojo config, Runnable postAction, Log log) {
            this.mojo = config;
            this.postAction = postAction;
            this.log = log;
        }

        @Override
        public void onFileCreate(final File file) {
            processFile(file, "created");
        }

        @Override
        public void onFileChange(final File file) {
            processFile(file, "updated");
        }

        private synchronized void processFile(File file, String actionName) {
            log.info(String.format("Source file %s %s", file.getAbsolutePath(), actionName));
            long timeInMillis = timed(() -> {
                try {
                    mojo.processSources(Collections.singletonList(file), EMPTY_RESOURCE_PROCESSOR);
                } catch (MojoExecutionException e) {
                    log.error(e);
                }
            });
            getLog().info("Converted document in " + timeInMillis + "ms");
            postAction.run();
        }
    }

    public long timed(Runnable runnable) {
        final long start = System.nanoTime();
        runnable.run();
        final long end = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(end - start);
    }
}
