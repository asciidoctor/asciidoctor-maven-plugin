package org.asciidoctor.maven.monitor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.maven.Synchronization;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to monitor resources and trigger updates in the "refresh" and "http" mojos
 *
 */
public class SynchronizingFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {

    private Log log;
    private AtomicBoolean needsUpdate = new AtomicBoolean(false);
    private final Synchronization s;

    public SynchronizingFileAlterationListenerAdaptor(Log log, AtomicBoolean needsUpdate, Synchronization s){
        this.log = log;
        this.needsUpdate = needsUpdate;
        this.s = s;
    }

    @Override
    public void onFileCreate(final File file) {
        getLog().info("File " + file.getAbsolutePath() + " created.");
        synchronize(s, log);
        needsUpdate.set(true);
    }

    @Override
    public void onFileChange(final File file) {
        getLog().info("File " + file.getAbsolutePath() + " updated.");
        synchronize(s, log);
        needsUpdate.set(true);
    }

    @Override
    public void onFileDelete(final File file) {
        getLog().info("File " + file.getAbsolutePath() + " deleted.");
        FileUtils.deleteQuietly(file);
        needsUpdate.set(true);
    }

    public static void synchronize(final Synchronization synchronization, final Log log) {
        if (synchronization.getSource().isDirectory()) {
            try {
                FileUtils.copyDirectory(synchronization.getSource(), synchronization.getTarget());
            } catch (IOException e) {
                log.error(String.format("Can't synchronize %s -> %s", synchronization.getSource(), synchronization.getTarget()));
            }
        } else {
            try {
                FileUtils.copyFile(synchronization.getSource(), synchronization.getTarget());
            } catch (IOException e) {
                log.error(String.format("Can't synchronize %s -> %s", synchronization.getSource(), synchronization.getTarget()));
            }
        }
    }

    public Log getLog() {
        return log;
    }

}
