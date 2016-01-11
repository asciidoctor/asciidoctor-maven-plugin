package org.asciidoctor.maven.monitor;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to monitor sources (AsciiDoc documents) in the "refresh" and "http" mojos
 *
 */
public class LoggingFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {

    private Log log;
    private AtomicBoolean needsUpdate = new AtomicBoolean(false);

    public LoggingFileAlterationListenerAdaptor(Log log, AtomicBoolean needsUpdate) {
        this.log = log;
        this.needsUpdate = needsUpdate;
    }

    @Override
    public void onFileCreate(final File file) {
        getLog().info("File " + file.getAbsolutePath() + " created.");
        needsUpdate.set(true);
    }

    @Override
    public void onFileChange(final File file) {
        getLog().info("File " + file.getAbsolutePath() + " updated.");
        needsUpdate.set(true);
    }

    @Override
    public void onFileDelete(final File file) {
        getLog().info("File " + file.getAbsolutePath() + " deleted.");
        needsUpdate.set(true);
    }

    public Log getLog() {
        return log;
    }
}
