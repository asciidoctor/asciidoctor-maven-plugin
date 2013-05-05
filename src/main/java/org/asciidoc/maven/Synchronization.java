package org.asciidoc.maven;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public class Synchronization {
    @Parameter(property = "source")
    protected File source;

    @Parameter(property = "target")
    protected File target;

    public File getSource() {
        return source;
    }

    public void setSource(final File source) {
        this.source = source;
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(final File target) {
        this.target = target;
    }
}
