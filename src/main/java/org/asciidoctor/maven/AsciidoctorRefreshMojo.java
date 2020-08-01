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

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.maven.refresh.AsciidoctorConverterFileAlterationListenerAdaptor;
import org.asciidoctor.maven.refresh.ResourceCopyFileAlterationListenerAdaptor;
import org.asciidoctor.maven.refresh.TimeCounter;

import java.io.FileFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;
import java.util.StringJoiner;

@Mojo(name = "auto-refresh")
public class AsciidoctorRefreshMojo extends AsciidoctorMojo {

    public static final String PREFIX = AsciidoctorMaven.PREFIX + "refresher.";

    @Parameter(property = PREFIX + "interval")
    protected int interval = 2000; // 2s

    private Collection<FileAlterationMonitor> monitors = null;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        startPolling();
        doWork();
        doWait();
        stopMonitors();
    }

    protected void doWork() throws MojoFailureException, MojoExecutionException {
        long timeInMillis = TimeCounter.timed(() -> {
            try {
                processAllSources(defaultResourcesProcessor);
            } catch (MojoExecutionException e) {
                getLog().error(e);
            }
        });
        getLog().info("Converted document(s) in " + timeInMillis + "ms");
    }

    protected void doWait() throws MojoExecutionException, MojoFailureException {
        showWaitMessage();

        String line;
        final Scanner scanner = new Scanner(System.in);
        while ((line = scanner.nextLine()) != null) {
            line = line.trim();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                return;
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

        final FileAlterationMonitor fileAlterationMonitor = new FileAlterationMonitor(interval);

        { // content monitor
            final FileAlterationObserver observer = new FileAlterationObserver(sourceDirectory, buildSourcesFileFilter());
            final FileAlterationListener listener = new AsciidoctorConverterFileAlterationListenerAdaptor(this, () -> showWaitMessage(), getLog());

            observer.addListener(listener);
            fileAlterationMonitor.addObserver(observer);
        }

        { // resources monitor
            final FileAlterationObserver observer = new FileAlterationObserver(sourceDirectory, buildResourcesFileFilter());
            final FileAlterationListener listener = new ResourceCopyFileAlterationListenerAdaptor(this, () -> showWaitMessage(), getLog());

            observer.addListener(listener);
            fileAlterationMonitor.addObserver(observer);
        }

        monitors = Collections.singletonList(fileAlterationMonitor);

        for (final FileAlterationMonitor monitor : monitors) {
            try {
                monitor.start();
            } catch (final Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private FileFilter buildResourcesFileFilter() {
        return FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), new RegexFileFilter("^[^_.].*\\.(jpg|png)$"));
    }

    private IOFileFilter buildSourcesFileFilter() {
        if (sourceDocumentName != null)
            return new NameFileFilter(sourceDocumentName);

        if (!sourceDocumentExtensions.isEmpty()) {
            final StringJoiner stringJoiner = new StringJoiner("|", "^[^_.].*\\.(", ")$");
            for (String extension : sourceDocumentExtensions) {
                stringJoiner.add(extension);
            }
            return FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), new RegexFileFilter(stringJoiner.toString()));
        }

        return FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), new RegexFileFilter(ASCIIDOC_REG_EXP_EXTENSION));
    }

}
