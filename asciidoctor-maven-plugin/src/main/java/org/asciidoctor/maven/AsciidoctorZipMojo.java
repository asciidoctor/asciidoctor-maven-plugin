package org.asciidoctor.maven;

import java.io.File;
import java.io.IOException;

import jnr.ffi.annotations.In;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.asciidoctor.maven.io.Zips;
import org.asciidoctor.maven.process.ResourcesProcessor;
import org.asciidoctor.maven.process.SourceDocumentFinder;

import javax.inject.Inject;

@Deprecated(since = "3.0.0", forRemoval = true)
@Mojo(name = "zip")
public class AsciidoctorZipMojo extends AsciidoctorMojo {

    public static final String PREFIX = AsciidoctorMaven.PREFIX + "zip.";

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(property = PREFIX + "attach", defaultValue = "true")
    protected boolean attach;

    @Parameter(property = PREFIX + "zip", defaultValue = "true")
    protected boolean zip;

    @Parameter(property = PREFIX + "zipDestination",
            defaultValue = "${project.build.directory}/${project.build.finalName}.zip")
    protected File zipDestination;

    @Parameter(property = PREFIX + "zipClassifier", defaultValue = "asciidoctor")
    protected String zipClassifier;

    @Inject
    public AsciidoctorZipMojo(AsciidoctorJFactory asciidoctorJFactory, AsciidoctorOptionsFactory asciidoctorOptionsFactory, SourceDocumentFinder finder, ResourcesProcessor defaultResourcesProcessor) {
        super(asciidoctorJFactory, asciidoctorOptionsFactory, finder, defaultResourcesProcessor);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        if (zip) {
            try {
                Zips.zip(outputDirectory, zipDestination);
                getLog().info("Created " + zipDestination.getAbsolutePath() + ".");
            } catch (final IOException e) {
                getLog().error("Can't zip " + outputDirectory.getAbsolutePath(), e);
            }
            if (attach) {
                if (zipClassifier != null) {
                    projectHelper.attachArtifact(project, "zip", zipClassifier, zipDestination);
                    getLog().info("Attached " + zipDestination.getAbsolutePath() + " with classifier " + zipClassifier + ".");
                } else {
                    projectHelper.attachArtifact(project, "zip", zipDestination);
                    getLog().info("Attached " + zipDestination.getAbsolutePath() + ".");
                }
            }
        }
    }

    public String getZipClassifier() {
        return zipClassifier;
    }

    public void setZipClassifier(final String zipClassifier) {
        this.zipClassifier = zipClassifier;
    }

    public File getZipDestination() {
        return zipDestination;
    }

    public void setZipDestination(final File zipDestination) {
        this.zipDestination = zipDestination;
    }

    public boolean isZip() {
        return zip;
    }

    public void setZip(final boolean zip) {
        this.zip = zip;
    }

    public boolean isAttach() {
        return attach;
    }

    public void setAttach(final boolean attach) {
        this.attach = attach;
    }
}
