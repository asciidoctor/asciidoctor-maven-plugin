package org.asciidoctor.maven.refresh;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.maven.AsciidoctorRefreshMojo;
import org.asciidoctor.maven.process.ResourcesProcessor;

public class AdditionalSourceFileAlterationListenerAdaptor extends AbstractFileAlterationListenerAdaptor {

    private static final ResourcesProcessor EMPTY_RESOURCES_PROCESSOR = (sourcesDir, outputDir, configuration) -> {
    };


    public AdditionalSourceFileAlterationListenerAdaptor(AsciidoctorRefreshMojo mojo, Runnable postAction, Log log) {
        super(mojo, postAction, log);
    }

    @Override
    synchronized void processFile(File file, String actionName) {
        getLog().info(String.format("Additional source file %s %s", file.getAbsolutePath(), actionName));
        getLog().info("Full refresh");
        long timeInMillis = TimeCounter.timed(() -> {
            try {
                getMojo().processAllSources(EMPTY_RESOURCES_PROCESSOR);
            } catch (MojoExecutionException e) {
                getLog().error(e);
            }
        });
        getLog().info("Converted document(s) in " + timeInMillis + "ms");
    }

}
