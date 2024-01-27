package org.asciidoctor.maven.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.asciidoctor.maven.AsciidoctorMojo;
import org.asciidoctor.maven.model.Resource;
import org.codehaus.plexus.util.DirectoryScanner;

import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;


/**
 * {@link ResourcesProcessor} implementation that copies all valid resources from
 * a source directory to an output one.
 * <p>
 * Following resources are not valid:
 * - AsciiDoc documents: based on file extension.
 * - Asciidoctor Docinfo files.
 * - Internal files and folders: those not starting with underscore '_'.
 */
public class CopyResourcesProcessor implements ResourcesProcessor {

    // docinfo snippets should not be copied
    public static String[] IGNORED_FILE_NAMES = {
        "docinfo.html",
        "docinfo-header.html",
        "docinfo-footer.html",
        "*-docinfo.html",
        "*-docinfo-header.html",
        "*-docinfo-footer.html",
        "docinfo.xml",
        "docinfo-header.xml",
        "docinfo-footer.xml",
        "*-docinfo.xml",
        "*-docinfo-header.xml",
        "*-docinfo-footer.xml"
    };

    private static String[] DEFAULT_ASCIIDOC_EXTENSIONS = {"**/*.adoc", "**/*.ad", "**/*.asc", "**/*.asciidoc"};

    // Files and directories beginning with underscore are ignored
    private static String[] INTERNAL_FOLDERS_AND_FILES_PATTERNS = {
        "**/_*.*",
        "**/_*",
        "**/.*",
        "**/_*/**/*.*",
    };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.asciidoctor.maven.process.ResourcesProcessor#process(File sourceRootDirectory, File outputRootDirectory, AsciidoctorMojo configuration)
     */
    @Override
    public void process(File sourceRootDirectory, File outputRootDirectory, AsciidoctorMojo configuration) {
        final List<Resource> finalResources = prepareResources(sourceRootDirectory, configuration);
        copyResources(finalResources, outputRootDirectory);
    }

    /**
     * Initializes resources attribute excluding AsciiDoc documents, internal directories/files (those prefixed with
     * underscore), and docinfo files.
     * By default everything in the sources directories is copied.
     *
     * @return Collection of resources with properly configured includes and excludes conditions.
     */
    private List<Resource> prepareResources(File sourceDirectory, AsciidoctorMojo configuration) {
        final List<Resource> resources = configuration.getResources() != null
            ? configuration.getResources()
            : new ArrayList<>();
        if (resources.isEmpty()) {
            // we don't want to copy files considered sources
            Resource resource = new Resource();
            resource.setDirectory(sourceDirectory.getAbsolutePath());
            // exclude sourceDocumentName if defined
            if (isNotBlank(configuration.getSourceDocumentName())) {
                resource.getExcludes()
                    .add(configuration.getSourceDocumentName());
            }
            resources.add(resource);
        }

        // All resources must exclude AsciiDoc documents and folders beginning with underscore
        for (Resource resource : resources) {

            List<String> excludes = new ArrayList<>();
            for (String value : INTERNAL_FOLDERS_AND_FILES_PATTERNS) {
                excludes.add(value);
            }
            for (String value : IGNORED_FILE_NAMES) {
                excludes.add("**/" + value);
            }

            for (String value : DEFAULT_ASCIIDOC_EXTENSIONS) {
                excludes.add(value);
            }
            // exclude filename extensions if defined
            for (String docExtension : configuration.getSourceDocumentExtensions()) {
                resource.getExcludes().add("**/*." + docExtension);
            }
            // in case someone wants to include some of the default excluded files (e.g. AsciiDoc sources)
            excludes.removeAll(resource.getIncludes());
            resource.getExcludes().addAll(excludes);
        }
        return resources;
    }

    /**
     * Copies the resources defined in the 'resources' attribute.
     *
     * @param resources       Collection of {@link Resource} defining what resources to {@code outputDirectory}.
     * @param outputDirectory Directory where to copy resources.
     */
    private void copyResources(List<Resource> resources, File outputDirectory) {

        resources.stream()
            .filter(resource -> new File(resource.getDirectory()).exists())
            .forEach(resource -> {
                DirectoryScanner directoryScanner = new DirectoryScanner();
                directoryScanner.setBasedir(resource.getDirectory());

                if (resource.getIncludes().isEmpty())
                    directoryScanner.setIncludes(new String[]{"**/*.*", "**/*"});
                else
                    directoryScanner.setIncludes(resource.getIncludes().toArray(new String[0]));

                directoryScanner.setExcludes(resource.getExcludes().toArray(new String[0]));
                directoryScanner.setFollowSymlinks(false);
                directoryScanner.scan();

                for (String includedFile : directoryScanner.getIncludedFiles()) {
                    File source = new File(directoryScanner.getBasedir(), includedFile);
                    copyFileToDirectory(source, resource, outputDirectory);
                }
            });
    }


    private void copyFileToDirectory(File source, Resource resource, File outputDirectory) {
        try {
            final File target = resource.getTargetPath() == null
                ? outputDirectory
                : composeTargetPath(resource, outputDirectory);

            Path sourceDirectoryPath = new File(resource.getDirectory()).toPath();
            Path sourcePath = source.toPath();
            Path relativize = sourceDirectoryPath.relativize(sourcePath);

            if (relativize.getParent() == null) {
                FileUtils.copyFileToDirectory(source, target);
            } else {
                Path realTarget = target.toPath().resolve(relativize.getParent());
                File realTargetFile = realTarget.toFile();
                FileUtils.forceMkdir(realTargetFile);
                FileUtils.copyFileToDirectory(source, realTargetFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File composeTargetPath(Resource resource, File outputDirectory) {
        final File targetFile = new File(resource.getTargetPath());
        return targetFile.isAbsolute()
            ? targetFile
            : new File(outputDirectory, resource.getTargetPath());
    }
}
