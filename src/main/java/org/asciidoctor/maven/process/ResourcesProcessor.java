package org.asciidoctor.maven.process;

import org.apache.maven.plugin.MojoExecutionException;
import org.asciidoctor.maven.AsciidoctorMojo;

import java.io.File;

public interface ResourcesProcessor {

    void process(File sourcesRootDirectory, File outputRootDirectory,
                 String encoding, AsciidoctorMojo configuration) throws MojoExecutionException;

}
