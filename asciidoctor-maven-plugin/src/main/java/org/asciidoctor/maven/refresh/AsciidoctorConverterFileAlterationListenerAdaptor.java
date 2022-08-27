package org.asciidoctor.maven.refresh;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.maven.AsciidoctorRefreshMojo;
import org.asciidoctor.maven.process.ResourcesProcessor;

import java.io.File;
import java.util.Collections;

public class AsciidoctorConverterFileAlterationListenerAdaptor extends AbstractFileAlterationListenerAdaptor {

    private static final ResourcesProcessor EMPTY_RESOURCES_PROCESSOR = (sourcesDir, outputDir, configuration) -> {
    };


    public AsciidoctorConverterFileAlterationListenerAdaptor(AsciidoctorRefreshMojo mojo, Runnable postAction, Log log) {
        super(mojo, postAction, log);
    }

    @Override
    synchronized void processFile(File file, String actionName) {
        getLog().info(String.format("Source file %s %s", file.getAbsolutePath(), actionName));
        long timeInMillis = TimeCounter.timed(() -> {
            try {
                getMojo().processSources(Collections.singletonList(file), EMPTY_RESOURCES_PROCESSOR);
            } catch (MojoExecutionException e) {
                getLog().error(e);
            }
        });
        getLog().info("Converted document in " + timeInMillis + "ms");
    }

}
