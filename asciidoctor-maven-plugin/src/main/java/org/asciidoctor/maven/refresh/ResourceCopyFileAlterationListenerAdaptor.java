package org.asciidoctor.maven.refresh;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.maven.AsciidoctorRefreshMojo;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;

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

                List<Resource> matchingResources = findMatchingResources(mojo.getResources(), file);
                if (matchingResources.isEmpty()) {
                    final String relativePath = file.getParentFile().getCanonicalPath().substring(sourceDirectory.getCanonicalPath().length());
                    final File destinationDirectory = new File(outputDirectory, relativePath);

                    FileUtils.forceMkdir(outputDirectory);
                    FileUtils.copyFileToDirectory(file, destinationDirectory);
                } else {
                    for (Resource matchingResource : matchingResources) {
                        DirectoryScanner scanner = new DirectoryScanner();
                        File basedir = resourceDirectory(matchingResource);
                        scanner.setBasedir(basedir);
                        if (matchingResource.getIncludes().isEmpty())
                            scanner.setIncludes(new String[]{"**/**"});
                        else
                            scanner.setIncludes(matchingResource.getIncludes().toArray(new String[]{}));
                        scanner.setExcludes(matchingResource.getExcludes().toArray(new String[]{}));
                        scanner.scan();
                        if (containsFile(scanner.getIncludedFiles(), basedir, file)) {
                            final File destDir = isBlank(matchingResource.getTargetPath())
                                    ? outputDirectory
                                    : new File(outputDirectory, matchingResource.getTargetPath());
                            FileUtils.forceMkdir(destDir);
                            FileUtils.copyFileToDirectory(file, destDir);
                        }
                    }
                }

            } catch (Exception e) {
                getLog().error("Could not copy file: " + file.getAbsolutePath());
            }
        });

        getLog().info("Copied resource in " + timeInMillis + "ms");
    }

    private boolean containsFile(String[] includedFiles, File basedir, File file) {
        for (String includedFile : includedFiles) {
            if (equalFiles(new File(basedir, includedFile), file))
                return true;
        }
        return false;
    }

    /**
     * Provides resources that could match with a given file.
     * It does not take into consideration exclusions.
     * NOTE: The goal is to reduce the amount of file processing done, not to be 100% accurate.
     */
    private List<Resource> findMatchingResources(List<Resource> resources, File file) {
        if (resources == null || resources.isEmpty())
            return Collections.emptyList();

        return resources.stream()
                .filter(resource -> {
                    File resourceDirectory = resourceDirectory(resource);
                    if (resource.getIncludes().isEmpty()) {
                        return equalDirectories(resourceDirectory, file.getParentFile());
                    }
                    if (containsRecursiveIncludes(resource)) {
                        return isSubDirectory(resourceDirectory, file);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    private boolean equalDirectories(File dir1, File dir2) {
        return dir1.isDirectory()
                && dir1.isDirectory()
                && equalFiles(dir1, dir2);
    }

    private boolean equalFiles(File file1, File file2) {
        try {
            return file1.getCanonicalPath().equals(file2.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    private File resourceDirectory(Resource resource) {
        File projectDirectory = this.getMojo().getProjectDirectory();
        return new File(projectDirectory, resource.getDirectory().replaceAll("\\\\", "/"));
    }

    private boolean containsRecursiveIncludes(Resource resource) {
        return resource.getIncludes().stream().anyMatch(include -> include.contains("**/") || include.contains("**\\"));
    }

    private boolean isSubDirectory(File parent, File file) {
        try {
            return file.getParentFile().getCanonicalPath()
                    .startsWith(parent.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

}
