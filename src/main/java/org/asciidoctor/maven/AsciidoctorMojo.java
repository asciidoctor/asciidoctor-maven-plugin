/*
 * Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidoctor.maven;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.asciidoctor.*;
import org.asciidoctor.jruby.AbstractDirectoryWalker;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.jruby.DirectoryWalker;
import org.asciidoctor.jruby.internal.JRubyRuntimeContext;
import org.asciidoctor.maven.extensions.AsciidoctorJExtensionRegistry;
import org.asciidoctor.maven.extensions.ExtensionConfiguration;
import org.asciidoctor.maven.extensions.ExtensionRegistry;
import org.asciidoctor.maven.io.AsciidoctorFileScanner;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.log.LogRecordHelper;
import org.asciidoctor.maven.log.LogRecordsProcessors;
import org.asciidoctor.maven.log.MemoryLogHandler;
import org.jruby.Ruby;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.asciidoctor.maven.SourceDirectoryFinder.DEFAULT_SOURCE_DIR;


/**
 * Basic maven plugin goal to convert AsciiDoc files using Asciidoctor, a ruby port.
 */
@Mojo(name = "process-asciidoc", threadSafe = true)
public class AsciidoctorMojo extends AbstractMojo {
    // copied from org.asciidoctor.AsciiDocDirectoryWalker.ASCIIDOC_REG_EXP_EXTENSION
    // should probably be configured in AsciidoctorMojo through @Parameter 'extension'
    protected static final String ASCIIDOC_REG_EXP_EXTENSION = ".*\\.a((sc(iidoc)?)|d(oc)?)$";

    @Parameter(defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDirectory", defaultValue = "${basedir}/" + DEFAULT_SOURCE_DIR)
    protected File sourceDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDirectory", defaultValue = "${project.build.directory}/generated-docs")
    protected File outputDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "outputFile")
    protected File outputFile;

    @Parameter(property = AsciidoctorMaven.PREFIX + "preserveDirectories", defaultValue = "false")
    protected boolean preserveDirectories = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "relativeBaseDir", defaultValue = "false")
    protected boolean relativeBaseDir = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "projectDirectory", defaultValue = "${basedir}")
    protected File projectDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "rootDir", defaultValue = "${basedir}")
    protected File rootDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "baseDir")
    protected File baseDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "skip")
    protected boolean skip = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "gemPath")
    protected String gemPath;

    @Parameter(property = AsciidoctorMaven.PREFIX + "requires")
    protected List<String> requires = new ArrayList<>();

    @Parameter
    protected Map<String, Object> attributes = new HashMap<>();

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.ATTRIBUTES)
    protected String attributesChain = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.BACKEND, defaultValue = "html5")
    protected String backend = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.DOCTYPE)
    protected String doctype;

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.ERUBY)
    protected String eruby = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "headerFooter")
    protected boolean headerFooter = true;

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateDirs")
    protected List<File> templateDirs = new ArrayList<>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateEngine")
    protected String templateEngine;

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateCache")
    protected boolean templateCache = true;

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDocumentName")
    protected String sourceDocumentName;

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDocumentExtensions")
    protected List<String> sourceDocumentExtensions = new ArrayList<>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourcemap")
    protected boolean sourcemap = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "catalogAssets")
    protected boolean catalogAssets = false;

    @Parameter
    protected List<Synchronization> synchronizations = new ArrayList<>();

    @Parameter
    protected List<ExtensionConfiguration> extensions = new ArrayList<>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "embedAssets")
    protected boolean embedAssets = false;

    // List of resources to copy to the output directory (e.g., images, css). By default everything is copied
    @Parameter
    protected List<Resource> resources;

    @Parameter(property = AsciidoctorMaven.PREFIX + "verbose")
    protected boolean enableVerbose = false;

    @Parameter
    private LogHandler logHandler = new LogHandler();

    @Inject
    protected MavenProject project;

    @Inject
    protected MavenSession session;

    @Inject
    protected MavenResourcesFiltering outputResourcesFiltering;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("AsciiDoc processing is skipped.");
            return;
        }

        if (sourceDirectory == null) {
            throw new MojoExecutionException("Required parameter 'asciidoctor.sourceDirectory' not set.");
        }

        if (!initSourceDirectory()) return;
        ensureOutputExists();

        // Validate resources to avoid errors later on
        if (resources != null) {
            for (Resource resource : resources) {
                if (resource.getDirectory() == null || resource.getDirectory().isEmpty()) {
                    throw new MojoExecutionException("Found empty resource directory");
                }
            }
        }

        final Asciidoctor asciidoctor = getAsciidoctorInstance(gemPath);
        if (enableVerbose) {
            asciidoctor.requireLibrary("enable_verbose.rb");
        }

        asciidoctor.requireLibraries(requires);

        final OptionsBuilder optionsBuilder = OptionsBuilder.options();
        setOptionsOnBuilder(optionsBuilder);

        final AttributesBuilder attributesBuilder = AttributesBuilder.attributes();
        setAttributesOnBuilder(attributesBuilder);

        optionsBuilder.attributes(attributesBuilder);

        ExtensionRegistry extensionRegistry = new AsciidoctorJExtensionRegistry(asciidoctor);
        for (ExtensionConfiguration extension : extensions) {
            try {
                extensionRegistry.register(extension.getClassName(), extension.getBlockName());
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        // Copy output resources
        prepareResources();
        copyResources();

        // Prepare sources
        final List<File> sourceFiles = sourceDocumentName == null ?
                scanSourceFiles() : Arrays.asList(new File(sourceDirectory, sourceDocumentName));

        final Set<File> dirs = new HashSet<File>();

        // register LogHandler to capture asciidoctor messages
        final Boolean outputToConsole = logHandler.getOutputToConsole() == null ? Boolean.TRUE : logHandler.getOutputToConsole();
        final MemoryLogHandler memoryLogHandler = new MemoryLogHandler(outputToConsole, sourceDirectory,
                logRecord -> getLog().info(LogRecordHelper.format(logRecord, sourceDirectory)));
        if (!sourceFiles.isEmpty()) {
            asciidoctor.registerLogHandler(memoryLogHandler);
            // disable default console output of AsciidoctorJ
            Logger.getLogger("asciidoctor").setUseParentHandlers(false);
        }

        for (final File source : sourceFiles) {
            final File destinationPath = setDestinationPaths(optionsBuilder, source);
            if (!dirs.add(destinationPath))
                getLog().warn("Duplicated destination found: overwriting file: " + destinationPath.getAbsolutePath());

            convertFile(asciidoctor, optionsBuilder.asMap(), source);

            try {
                // process log messages according to mojo configuration
                new LogRecordsProcessors(logHandler, sourceDirectory, errorMessage -> getLog().error(errorMessage))
                        .processLogRecords(memoryLogHandler);
            } catch (Exception exception) {
                throw new MojoExecutionException(exception.getMessage());
            }
        }

        if (synchronizations != null && !synchronizations.isEmpty()) {
            synchronize();
        }
    }

    private boolean initSourceDirectory() {
        Optional<File> sourceDirCandidate = new SourceDirectoryFinder(sourceDirectory, project.getBasedir(),
                candidate -> {
                    String candidateName = candidate.toString();
                    if (isRelativePath(candidateName)) candidateName = candidateName.substring(2);
                    getLog().info("sourceDirectory " + candidateName + " does not exist");
                })
                .find();

        if (sourceDirCandidate.isPresent()) {
            this.sourceDirectory = sourceDirCandidate.get();
        } else {
            getLog().info("No sourceDirectory found. Skipping processing");
            return false;
        }
        return true;
    }

    private boolean isRelativePath(String candidateName) {
        return candidateName.startsWith("./") || candidateName.startsWith(".\\");
    }

    /**
     * Initializes resources attribute excluding AsciiDoc documents and hidden directories/files (those prefixed with
     * underscore).
     * By default everything in the sources directories is copied.
     */
    private void prepareResources() {
        if (resources == null || resources.isEmpty()) {
            resources = new ArrayList<Resource>();
            // we don't want to copy files considered sources
            Resource resource = new Resource();
            resource.setDirectory(sourceDirectory.getAbsolutePath());
            resource.setExcludes(new ArrayList<String>());
            // exclude sourceDocumentName if defined
            if (sourceDocumentName != null && sourceDocumentName.isEmpty()) {
                resource.getExcludes().add(sourceDocumentName);
            }
            // exclude filename extensions if defined
            resources.add(resource);
        }

        // All resources must exclude AsciiDoc documents and folders beginning with underscore
        for (Resource resource : resources) {
            if (resource.getExcludes() == null || resource.getExcludes().isEmpty()) {
                resource.setExcludes(new ArrayList<String>());
            }
            List<String> excludes = new ArrayList<String>();
            for (String value : AsciidoctorFileScanner.IGNORED_FOLDERS_AND_FILES) {
                excludes.add(value);
            }
            for (String value : AsciidoctorFileScanner.DEFAULT_FILE_EXTENSIONS) {
                excludes.add(value);
            }
            for (String docExtension : sourceDocumentExtensions) {
                resource.getExcludes().add("**/*." + docExtension);
            }
            // in case someone wants to include some of the default excluded files (.e.g., AsciiDoc docs)
            excludes.removeAll(resource.getIncludes());
            resource.getExcludes().addAll(excludes);
        }
    }

    /**
     * Copies the resources defined in the 'resources' attribute
     */
    private void copyResources() throws MojoExecutionException {
        try {
            // Right now it's not used at all, but could be used to apply resource filters/replacements
            MavenResourcesExecution resourcesExecution =
                    new MavenResourcesExecution(resources, outputDirectory, project, encoding,
                            Collections.<String>emptyList(), Collections.<String>emptyList(), session);
            resourcesExecution.setIncludeEmptyDirs(true);
            resourcesExecution.setAddDefaultExcludes(true);
            outputResourcesFiltering.filterResources(resourcesExecution);
        } catch (MavenFilteringException e) {
            throw new MojoExecutionException("Could not copy resources", e);
        }
    }

    /**
     * Updates optionsBuilder object's baseDir and destination(s) accordingly to the options.
     *
     * @param optionsBuilder AsciidoctorJ options to be updated.
     * @param sourceFile     AsciiDoc source file to process.
     * @return the final destination file path.
     */
    private File setDestinationPaths(OptionsBuilder optionsBuilder, final File sourceFile) throws MojoExecutionException {
        try {
            if (baseDir != null) {
                optionsBuilder.baseDir(baseDir);
            } else {
                // when preserveDirectories == false, parent and sourceDirectory are the same
                if (relativeBaseDir) {
                    optionsBuilder.baseDir(sourceFile.getParentFile());
                } else {
                    optionsBuilder.baseDir(sourceDirectory);
                }
            }
            if (preserveDirectories) {
                final String candidatePath = sourceFile.getParentFile().getCanonicalPath().substring(sourceDirectory.getCanonicalPath().length());
                final File relativePath = new File(outputDirectory.getCanonicalPath() + candidatePath);
                optionsBuilder.toDir(relativePath).destinationDir(relativePath);
            } else {
                optionsBuilder.toDir(outputDirectory).destinationDir(outputDirectory);
            }
            if (outputFile != null) {
                //allow overriding the output file name
                optionsBuilder.toFile(outputFile);
            }
            // return destination file path
            if (outputFile != null) {
                return outputFile.isAbsolute() ?
                        outputFile : new File((String) optionsBuilder.asMap().get(Options.DESTINATION_DIR), outputFile.getPath());
            } else
                return new File((String) optionsBuilder.asMap().get(Options.DESTINATION_DIR), sourceFile.getName());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to locate output directory", e);
        }
    }

    protected Asciidoctor getAsciidoctorInstance(String gemPath) throws MojoExecutionException {
        Asciidoctor asciidoctor = null;
        if (gemPath == null) {
            asciidoctor = AsciidoctorJRuby.Factory.create();
        } else {
            // Replace Windows path separator to avoid paths with mixed \ and /.
            // This happens for instance when setting: <gemPath>${project.build.directory}/gems-provided</gemPath>
            // because the project's path is converted to string.
            String normalizedGemPath = (File.separatorChar == '\\') ? gemPath.replaceAll("\\\\", "/") : gemPath;
            asciidoctor = AsciidoctorJRuby.Factory.create(normalizedGemPath);
        }

        Ruby rubyInstance = null;
        try {
            rubyInstance = (Ruby) JRubyRuntimeContext.class.getMethod("get")
                    .invoke(null);
        } catch (NoSuchMethodException e) {
            if (rubyInstance == null) {
                try {
                    rubyInstance = (Ruby) JRubyRuntimeContext.class.getMethod(
                            "get", Asciidoctor.class).invoke(null, asciidoctor);
                } catch (Exception e1) {
                    throw new MojoExecutionException(
                            "Failed to invoke get(AsciiDoctor) for JRubyRuntimeContext",
                            e1);
                }

            }
        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Failed to invoke get for JRubyRuntimeContext", e);
        }

        String gemHome = rubyInstance.evalScriptlet("ENV['GEM_HOME']").toString();
        String gemHomeExpected = (gemPath == null || "".equals(gemPath)) ? "" : gemPath.split(java.io.File.pathSeparator)[0];

        if (!"".equals(gemHome) && !gemHomeExpected.equals(gemHome)) {
            getLog().warn("Using inherited external environment to resolve gems (" + gemHome + "), i.e. build is platform dependent!");
        }

        return asciidoctor;
    }

    private List<File> scanSourceFiles() {
        final List<File> asciidoctorFiles;
        if (sourceDocumentExtensions == null || sourceDocumentExtensions.isEmpty()) {
            final DirectoryWalker directoryWalker = new AsciiDocDirectoryWalker(sourceDirectory.getAbsolutePath());
            asciidoctorFiles = directoryWalker.scan();
        } else {
            final DirectoryWalker directoryWalker = new CustomExtensionDirectoryWalker(sourceDirectory.getAbsolutePath(), sourceDocumentExtensions);
            asciidoctorFiles = directoryWalker.scan();
        }
        String absoluteSourceDirectory = sourceDirectory.getAbsolutePath();
        for (Iterator<File> iter = asciidoctorFiles.iterator(); iter.hasNext(); ) {
            File f = iter.next();
            do {
                // stop when we hit the source directory root
                if (absoluteSourceDirectory.equals(f.getAbsolutePath())) {
                    break;
                }
                // skip if the filename or directory begins with _
                if (f.getName().startsWith("_")) {
                    iter.remove();
                    break;
                }
            } while ((f = f.getParentFile()) != null);
        }
        return asciidoctorFiles;
    }

    private void synchronize() {
        for (final Synchronization synchronization : synchronizations) {
            synchronize(synchronization);
        }
    }

    protected void convertFile(Asciidoctor asciidoctor, Map<String, Object> options, File f) {
        asciidoctor.convertFile(f, options);
        logConvertedFile(f);
    }

    protected void logConvertedFile(File f) {
        getLog().info("Converted " + f.getAbsolutePath());
    }

    protected void synchronize(final Synchronization synchronization) {
        if (synchronization.getSource().isDirectory()) {
            try {
                FileUtils.copyDirectory(synchronization.getSource(), synchronization.getTarget());
            } catch (IOException e) {
                getLog().error(String.format("Can't synchronize %s -> %s", synchronization.getSource(), synchronization.getTarget()));
            }
        } else {
            try {
                FileUtils.copyFile(synchronization.getSource(), synchronization.getTarget());
            } catch (IOException e) {
                getLog().error(String.format("Can't synchronize %s -> %s", synchronization.getSource(), synchronization.getTarget()));
            }
        }
    }

    protected void ensureOutputExists() {
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                getLog().error("Can't create " + outputDirectory.getPath());
            }
        }
    }

    /**
     * Updates and OptionsBuilder instance with the options defined in the configuration.
     *
     * @param optionsBuilder AsciidoctorJ options to be updated.
     */
    protected void setOptionsOnBuilder(OptionsBuilder optionsBuilder) {
        optionsBuilder
                .backend(backend)
                .safe(SafeMode.UNSAFE)
                .headerFooter(headerFooter)
                .eruby(eruby)
                .mkDirs(true);

        // Following options are only set when the value is different than the default
        if (sourcemap)
            optionsBuilder.option("sourcemap", true);

        if (catalogAssets)
            optionsBuilder.option("catalog_assets", true);

        if (!templateCache)
            optionsBuilder.option("template_cache", false);

        if (doctype != null)
            optionsBuilder.docType(doctype);

        if (templateEngine != null)
            optionsBuilder.templateEngine(templateEngine);

        if (templateDirs != null)
            optionsBuilder.templateDirs(templateDirs.toArray(new File[]{}));
    }

    protected void setAttributesOnBuilder(AttributesBuilder attributesBuilder) {
        if (embedAssets) {
            attributesBuilder.linkCss(false);
            attributesBuilder.dataUri(true);
        }

        AsciidoctorHelper.addMavenProperties(project, attributesBuilder);
        AsciidoctorHelper.addAttributes(attributes, attributesBuilder);

        if (!attributesChain.isEmpty()) {
            getLog().info("Attributes: " + attributesChain);
            attributesBuilder.arguments(attributesChain);
        }

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

    public boolean isHeaderFooter() {
        return headerFooter;
    }

    public void setHeaderFooter(boolean headerFooter) {
        this.headerFooter = headerFooter;
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

    public List<Synchronization> getSynchronizations() {
        return synchronizations;
    }

    public void setSynchronizations(List<Synchronization> synchronizations) {
        this.synchronizations = synchronizations;
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

    private static class CustomExtensionDirectoryWalker extends AbstractDirectoryWalker {
        private final List<String> fileExtensions;

        public CustomExtensionDirectoryWalker(final String absolutePath, final List<String> fileExtensions) {
            super(absolutePath);
            this.fileExtensions = fileExtensions;
        }

        @Override
        protected boolean isAcceptedFile(final File filename) {
            final String name = filename.getName();
            for (final String extension : fileExtensions) {
                if (name.endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

}
