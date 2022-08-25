package org.asciidoctor.maven.model;

import java.util.ArrayList;
import java.util.List;

public class Resource {

    private String directory;
    private String targetPath;
    private List<String> includes;
    private List<String> excludes;

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return this.directory;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getTargetPath() {
        return this.targetPath;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getIncludes() {
        if (this.includes == null) {
            this.includes = new ArrayList();
        }

        return this.includes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public List<String> getExcludes() {
        if (this.excludes == null) {
            this.excludes = new ArrayList();
        }

        return this.excludes;
    }
}
