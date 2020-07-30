package org.asciidoctor.maven.refresh;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.maven.AsciidoctorRefreshMojo;

import java.io.File;

public class ResourceCopyFileAlterationListenerAdaptor extends AbstractFileAlterationListenerAdaptor {

    public ResourceCopyFileAlterationListenerAdaptor(AsciidoctorRefreshMojo mojo, Runnable postAction, Log log) {
        super(mojo, postAction, log);
    }

    @Override
    synchronized void processFile(File file, String actionName) {
        getLog().info(String.format("Resource file %s %s", file.getAbsolutePath(), actionName));
        long timeInMillis = TimeCounter.timed(() -> {
            try {
                AsciidoctorRefreshMojo mojo = getMojo();
                final File sourceDirectory = mojo.findSourceDirectory(mojo.getSourceDirectory(), mojo.getBaseDir()).get();
                final File outputDirectory = mojo.getOutputDirectory();

                final String relativePath = file.getParentFile().getCanonicalPath().substring(sourceDirectory.getCanonicalPath().length());
                final File destinationDirectory = new File(outputDirectory, relativePath);

                FileUtils.forceMkdir(outputDirectory);
                FileUtils.copyFileToDirectory(file, destinationDirectory);
            } catch (Exception e) {
                getLog().error("Could not copy file: " + file.getAbsolutePath());
            }
        });

        getLog().info("Copied resource in " + timeInMillis + "ms");
    }

}
