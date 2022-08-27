package org.asciidoctor.maven.process;

import org.asciidoctor.maven.AsciidoctorMojo;

import java.io.File;

public interface ResourcesProcessor {

    /**
     * Identifies requires resources and prepares them based on configuration.
     *
     * @param sourceRootDirectory starting directory to search resources. 'configuration' may add or modify it.
     * @param outputRootDirectory target directory to place final resources when copying is required.
     * @param configuration       Asciidoctor conversion configuration
     */
    void process(File sourceRootDirectory, File outputRootDirectory, AsciidoctorMojo configuration);

}
