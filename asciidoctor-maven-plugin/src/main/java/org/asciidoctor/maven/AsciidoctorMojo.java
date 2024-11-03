package org.asciidoctor.maven;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.maven.extensions.AsciidoctorJExtensionRegistry;
import org.asciidoctor.maven.extensions.ExtensionConfiguration;
import org.asciidoctor.maven.extensions.ExtensionRegistry;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.log.LogRecordFormatter;
import org.asciidoctor.maven.log.LogRecordsProcessors;
import org.asciidoctor.maven.log.MemoryLogHandler;
import org.asciidoctor.maven.model.Resource;
import org.asciidoctor.maven.process.ResourcesProcessor;
import org.asciidoctor.maven.process.SourceDirectoryFinder;
import org.asciidoctor.maven.process.SourceDocumentFinder;

import static org.asciidoctor.maven.process.SourceDirectoryFinder.DEFAULT_SOURCE_DIR;


/**
 * Basic maven plugin goal to convert AsciiDoc files using AsciidoctorJ.
 */
@Mojo(name = "process-asciidoc", threadSafe = true)
public class AsciidoctorMojo extends AbstractMojo {

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDirectory", defaultValue = "${basedir}/" + DEFAULT_SOURCE_DIR)
    protected File sourceDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDirectory", defaultValue = "${project.build.directory}/generated-docs")
    protected File outputDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "outputFile")
    protected File outputFile;

    @Parameter(property = AsciidoctorMaven.PREFIX + "preserveDirectories", defaultValue = "false")
    protected boolean preserveDirectories;

    @Parameter(property = AsciidoctorMaven.PREFIX + "relativeBaseDir", defaultValue = "false")
    protected boolean relativeBaseDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "projectDirectory", defaultValue = "${basedir}")
    protected File projectDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "rootDir", defaultValue = "${basedir}")
    protected File rootDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "baseDir")
    protected File baseDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "skip", defaultValue = "false")
    protected boolean skip;

    @Parameter(property = AsciidoctorMaven.PREFIX + "gemPath")
    protected String gemPath;

    @Parameter(property = AsciidoctorMaven.PREFIX + "requires")
    protected List<String> requires = new ArrayList<>();

    @Parameter
    protected Map<String, Object> attributes = new HashMap<>();

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.ATTRIBUTES)
    protected String attributesChain;

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.BACKEND, defaultValue = "html5")
    protected String backend;

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.DOCTYPE)
    protected String doctype;

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.ERUBY)
    protected String eruby;

    @Parameter(property = AsciidoctorMaven.PREFIX + "standalone", defaultValue = "true")
    protected boolean standalone;

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateDirs")
    protected List<File> templateDirs = new ArrayList<>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateEngine")
    protected String templateEngine;

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateCache", defaultValue = "true")
    protected boolean templateCache;

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDocumentName")
    protected String sourceDocumentName;

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDocumentExtensions")
    protected List<String> sourceDocumentExtensions = new ArrayList<>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourcemap", defaultValue = "false")
    protected boolean sourcemap;

    @Parameter(property = AsciidoctorMaven.PREFIX + "catalogAssets", defaultValue = "false")
    protected boolean catalogAssets;

    @Parameter
    protected List<ExtensionConfiguration> extensions = new ArrayList<>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "embedAssets", defaultValue = "false")
    protected boolean embedAssets;

    // List of resources to copy to the output directory (e.g., images, css). By default, everything is copied
    @Parameter
    protected List<Resource> resources;

    @Parameter(property = AsciidoctorMaven.PREFIX + "verbose", defaultValue = "false")
    protected boolean enableVerbose;

    @Parameter
    private LogHandler logHandler = new LogHandler();

    @Inject
    protected MavenProject project;
    @Inject
    private AsciidoctorJFactory asciidoctorJFactory;
    @Inject
    private SourceDocumentFinder finder;
    @Inject
    protected ResourcesProcessor defaultResourcesProcessor;
    @Inject
    private AsciidoctorOptionsFactory asciidoctorOptionsFactory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        processAllSources(defaultResourcesProcessor);
    }

    /**
     * Converts all found AsciiDoc sources according to mojo rules.
     *
     * @param resourcesProcessor Behavior to apply for resources.
     * @throws MojoExecutionException If requirements are not met
     */
    public void processAllSources(ResourcesProcessor resourcesProcessor) throws MojoExecutionException {
        processSources(null, resourcesProcessor);
    }

    /**
     * Converts a collection of AsciiDoc sources.
     *
     * @param sourceFiles        Collection of source files to convert.
     * @param resourcesProcessor Behavior to apply for resources.
     * @throws MojoExecutionException If requirements are not met
     */
    public void processSources(List<File> sourceFiles, ResourcesProcessor resourcesProcessor) throws MojoExecutionException {
        if (skip) {
            getLog().info("AsciiDoc processing is skipped.");
            return;
        }

        if (sourceDirectory == null) {
            throw new MojoExecutionException("Required parameter 'asciidoctor.sourceDirectory' not set.");
        }

        Optional<File> sourceDirectoryCandidate = findSourceDirectory(sourceDirectory, project.getBasedir());
        if (!sourceDirectoryCandidate.isPresent()) {
            getLog().info("No sourceDirectory found. Skipping processing");
            return;
        }

        if (sourceFiles == null) {
            sourceFiles = findSourceFiles(sourceDirectoryCandidate.get());
        }
        if (sourceFiles.isEmpty()) {
            getLog().info("No sources found. Skipping processing");
            return;
        }

        if (!ensureOutputExists()) {
            getLog().error("Can't create " + outputDirectory.getPath());
            return;
        }

        // Validate resources to avoid errors later on
        if (resources != null) {
            for (Resource resource : resources) {
                if (resource.getDirectory() == null || resource.getDirectory().isEmpty()) {
                    throw new MojoExecutionException("Found empty resource directory");
                }
            }
        }

        final Asciidoctor asciidoctor = asciidoctorJFactory.create(gemPath, getLog());

        if (enableVerbose) {
            asciidoctor.requireLibrary("enable_verbose.rb");
        }
        asciidoctor.requireLibraries(requires);

        ExtensionRegistry extensionRegistry = new AsciidoctorJExtensionRegistry(asciidoctor);
        for (ExtensionConfiguration extension : extensions) {
            try {
                extensionRegistry.register(extension.getClassName(), extension.getBlockName());
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        OptionsBuilder optionsBuilder = asciidoctorOptionsFactory.create(this, project, getLog());

        // Copy output resources
        final File sourceDir = sourceDirectoryCandidate.get();
        resourcesProcessor.process(sourceDir, outputDirectory, this);

        // register LogHandler to capture asciidoctor messages
        final Boolean outputToConsole = logHandler.getOutputToConsole() == null ? Boolean.TRUE : logHandler.getOutputToConsole();
        final MemoryLogHandler memoryLogHandler = new MemoryLogHandler(outputToConsole,
            logRecord -> getLog().info(LogRecordFormatter.format(logRecord, sourceDir)));
        asciidoctor.registerLogHandler(memoryLogHandler);
        // disable default console output of AsciidoctorJ
        Logger.getLogger("asciidoctor").setUseParentHandlers(false);

        final Set<File> uniquePaths = new HashSet<>();
        for (final File source : sourceFiles) {
            final Destination destination = setDestinationPaths(source, optionsBuilder, sourceDir, this);
            final File destinationPath = destination.path;
            if (!uniquePaths.add(destinationPath)) {
                String destinationFile = destinationPath.getAbsolutePath();
                if (!destination.isOutput) {
                    String baseName = FilenameUtils.getBaseName(destinationPath.getName());
                    destinationFile = destinationPath.getParentFile().getAbsolutePath() + File.separator + baseName + ".*";
                }
                getLog().warn("Duplicated destination found: overwriting file: " + destinationFile);
            }

            convertFile(asciidoctor, optionsBuilder.build(), source);

            try {
                // process log messages according to mojo configuration
                new LogRecordsProcessors(logHandler, sourceDir, errorMessage -> getLog().error(errorMessage))
                    .processLogRecords(memoryLogHandler);
            } catch (Exception exception) {
                throw new MojoExecutionException(exception.getMessage());
            }
        }
    }

    public Optional<File> findSourceDirectory(File initialSourceDirectory, File baseDir) {
        Optional<File> sourceDirCandidate = new SourceDirectoryFinder(initialSourceDirectory, baseDir,
            candidate -> {
                String candidateName = candidate.toString();
                if (isRelativePath(candidateName)) candidateName = candidateName.substring(2);
                getLog().info("sourceDirectory " + candidateName + " does not exist");
            })
            .find();

        return sourceDirCandidate;
    }

    private boolean isRelativePath(String candidateName) {
        return candidateName.startsWith("./") || candidateName.startsWith(".\\");
    }

    /**
     * Updates optionsBuilder's baseDir and toDir accordingly to the conversion configuration.
     *
     * @param sourceFile      AsciiDoc source file to process.
     * @param optionsBuilder  Asciidoctor options to be updated.
     * @param sourceDirectory Source directory configured (`sourceFile` may include relative path).
     * @param configuration   AsciidoctorMojo containing conversion configuration.
     * @return the final destination file path.
     * @throws MojoExecutionException If output is not valid
     */
    public Destination setDestinationPaths(final File sourceFile, final OptionsBuilder optionsBuilder, final File sourceDirectory,
                                           final AsciidoctorMojo configuration) throws MojoExecutionException {
        try {
            if (configuration.getBaseDir() != null) {
                optionsBuilder.baseDir(configuration.getBaseDir());
            } else {
                // when preserveDirectories == false, parent and sourceDirectory are the same
                if (configuration.isRelativeBaseDir()) {
                    optionsBuilder.baseDir(sourceFile.getParentFile());
                } else {
                    optionsBuilder.baseDir(sourceDirectory);
                }
            }
            final File outputDir = configuration.getOutputDirectory();
            if (configuration.isPreserveDirectories()) {
                final String candidatePath = sourceFile.getParentFile().getCanonicalPath().substring(sourceDirectory.getCanonicalPath().length());
                final File relativePath = new File(outputDir.getCanonicalPath() + candidatePath);
                optionsBuilder.toDir(relativePath);
            } else {
                optionsBuilder.toDir(outputDir);
            }
            final File outputFile = configuration.getOutputFile();
            final String toDir = (String) optionsBuilder.build().map().get(Options.TO_DIR);
            if (outputFile != null) {
                // allow overriding the output file name
                optionsBuilder.toFile(outputFile);
                return outputFile.isAbsolute()
                    ? new Destination(outputFile, true)
                    : new Destination(new File(toDir, outputFile.getPath()), true);
            } else {
                return new Destination(new File(toDir, sourceFile.getName()), false);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to locate output directory", e);
        }
    }

    class Destination {
        final File path;
        // Whether path is the actual output file or an approximation
        final boolean isOutput;

        Destination(File destination, boolean isSource) {
            this.path = destination;
            this.isOutput = isSource;
        }
    }

    protected List<File> findSourceFiles(File sourceDirectory) {
        if (sourceDocumentName != null)
            return List.of(new File(sourceDirectory, sourceDocumentName));

        final Path sourceDirectoryPath = sourceDirectory.toPath();
        return sourceDocumentExtensions.isEmpty() ?
            finder.find(sourceDirectoryPath) :
            finder.find(sourceDirectoryPath, sourceDocumentExtensions);
    }

    protected void convertFile(Asciidoctor asciidoctor, Options options, File f) {
        asciidoctor.convertFile(f, options);
        logConvertedFile(f);
    }

    protected void logConvertedFile(File f) {
        getLog().info("Converted " + f.getAbsolutePath());
    }

    protected boolean ensureOutputExists() {
        if (!outputDirectory.exists()) {
            return outputDirectory.mkdirs();
        }
        return true;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Maven sets it as an absolute path relative to project root.
     * Using a string circumvents it.
     * Maven properties (e.g. ${project.build.directory}) are resolved as string into absolute paths.
     *
     * @param outputFile output file path
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = new File(outputFile);
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public String getDoctype() {
        return doctype;
    }

    public void setDoctype(String doctype) {
        this.doctype = doctype;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    public List<String> getSourceDocumentExtensions() {
        return sourceDocumentExtensions;
    }

    public void setSourceDocumentExtensions(final List<String> sourceDocumentExtensions) {
        this.sourceDocumentExtensions = sourceDocumentExtensions;
    }

    public String getEruby() {
        return eruby;
    }

    public void setEruby(String eruby) {
        this.eruby = eruby;
    }

    public String getSourceDocumentName() {
        return sourceDocumentName;
    }

    public void setSourceDocumentName(String sourceDocumentName) {
        this.sourceDocumentName = sourceDocumentName;
    }

    public boolean isEmbedAssets() {
        return embedAssets;
    }

    public void setEmbedAssets(boolean embedAssets) {
        this.embedAssets = embedAssets;
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }

    public void setProjectDirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public File getRootDir() {
        return rootDir;
    }

    public void setRootDir(File rootDir) {
        this.rootDir = rootDir;
    }

    public String getGemPath() {
        return gemPath;
    }

    public void setGemPath(String gemPath) {
        this.gemPath = gemPath;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setPreserveDirectories(boolean preserveDirectories) {
        this.preserveDirectories = preserveDirectories;
    }

    public void setRelativeBaseDir(boolean relativeBaseDir) {
        this.relativeBaseDir = relativeBaseDir;
    }

    public List<ExtensionConfiguration> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ExtensionConfiguration> extensions) {
        this.extensions = extensions;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public boolean isEnableVerbose() {
        return enableVerbose;
    }

    public List<String> getRequires() {
        return requires;
    }

    public void setEnableVerbose(boolean enableVerbose) {
        this.enableVerbose = enableVerbose;
    }

    public boolean isSourcemap() {
        return sourcemap;
    }

    public boolean isCatalogAssets() {
        return catalogAssets;
    }

    public boolean isTemplateCache() {
        return templateCache;
    }

    public List<File> getTemplateDirs() {
        return templateDirs;
    }

    public String getAttributesChain() {
        return attributesChain;
    }

    public boolean isRelativeBaseDir() {
        return relativeBaseDir;
    }

    public boolean isPreserveDirectories() {
        return preserveDirectories;
    }
}
