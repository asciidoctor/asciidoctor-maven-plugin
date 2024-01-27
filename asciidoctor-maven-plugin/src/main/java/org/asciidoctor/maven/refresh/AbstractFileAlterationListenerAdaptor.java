package org.asciidoctor.maven.refresh;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.maven.AsciidoctorRefreshMojo;

public abstract class AbstractFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {

    private final AsciidoctorRefreshMojo mojo;
    private final Runnable postAction;
    private final Log log;

    public AbstractFileAlterationListenerAdaptor(AsciidoctorRefreshMojo mojo, Runnable postAction, Log log) {
        this.mojo = mojo;
        this.postAction = postAction;
        this.log = log;
    }

    @Override
    public void onFileCreate(final File file) {
        processFile(file, "created");
        postAction.run();
    }

    @Override
    public void onFileChange(final File file) {
        processFile(file, "updated");
        postAction.run();
    }

    abstract void processFile(File file, String actionName);

    public AsciidoctorRefreshMojo getMojo() {
        return mojo;
    }

    public Log getLog() {
        return log;
    }
}
