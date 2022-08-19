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
import org.asciidoctor.maven.refresh.*;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.asciidoctor.maven.io.AsciidoctorFileScanner.ASCIIDOC_NON_INTERNAL_REG_EXP;
import static org.asciidoctor.maven.process.SourceDocumentFinder.CUSTOM_FILE_EXTENSIONS_PATTERN_PREFIX;
import static org.asciidoctor.maven.process.SourceDocumentFinder.CUSTOM_FILE_EXTENSIONS_PATTERN_SUFFIX;

@Mojo(name = "auto-refresh")
public class AsciidoctorRefreshMojo extends AsciidoctorMojo {

    public static final String PREFIX = AsciidoctorMaven.PREFIX + "refresher.";

    @Parameter(property = PREFIX + "interval")
    protected int interval = 2000; // 2s

    @Parameter(property = PREFIX + "refreshOn")
    protected String refreshOn;

    private Collection<FileAlterationMonitor> monitors = null;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        startPolling();
        doWork();
        doWait();
        stopMonitors();
    }

    protected void doWork() {
        long timeInMillis = TimeCounter.timed(() -> {
            try {
                processAllSources(defaultResourcesProcessor);
            } catch (MojoExecutionException e) {
                getLog().error(e);
            }
        });
        getLog().info("Converted document(s) in " + timeInMillis + "ms");
    }

    protected void doWait() {
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

    protected void stopMonitors() throws MojoExecutionException {
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

    protected void startPolling() throws MojoExecutionException {

        // TODO avoid duplication with AsciidoctorMojo
        final Optional<File> sourceDirectoryCandidate = findSourceDirectory(sourceDirectory, project.getBasedir());
        if (!sourceDirectoryCandidate.isPresent()) {
            getLog().info("No sourceDirectory found. Skipping processing");
            return;
        }
        final File sourceDirectory = sourceDirectoryCandidate.get();

        final FileAlterationMonitor fileAlterationMonitor = new FileAlterationMonitor(interval);

        { // sources monitor
            final FileAlterationObserver observer = new FileAlterationObserver(sourceDirectory, buildSourcesFileFilter());
            final FileAlterationListener listener = new AsciidoctorConverterFileAlterationListenerAdaptor(this, () -> showWaitMessage(), getLog());

            observer.addListener(listener);
            fileAlterationMonitor.addObserver(observer);
        }

        { // included-sources monitor
            if (!isBlank(refreshOn)) {
                final FileAlterationObserver observer = new FileAlterationObserver(sourceDirectory, new RegexFileFilter(refreshOn));
                final FileAlterationListener listener = new AdditionalSourceFileAlterationListenerAdaptor(this, () -> showWaitMessage(), getLog());

                observer.addListener(listener);
                fileAlterationMonitor.addObserver(observer);
            }
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
        final String resourcesRegexPattern = new ResourcesPatternBuilder(sourceDocumentName, sourceDocumentExtensions).build();
        return FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), new RegexFileFilter(resourcesRegexPattern));
    }

    private IOFileFilter buildSourcesFileFilter() {
        if (!isBlank(sourceDocumentName))
            return new NameFileFilter(sourceDocumentName);

        if (!sourceDocumentExtensions.isEmpty()) {
            final StringJoiner stringJoiner = new StringJoiner("|", CUSTOM_FILE_EXTENSIONS_PATTERN_PREFIX, CUSTOM_FILE_EXTENSIONS_PATTERN_SUFFIX);
            for (String extension : sourceDocumentExtensions) {
                stringJoiner.add(extension);
            }
            return FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), new RegexFileFilter(stringJoiner.toString()));
        }

        return FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), new RegexFileFilter(ASCIIDOC_NON_INTERNAL_REG_EXP));
    }

}
