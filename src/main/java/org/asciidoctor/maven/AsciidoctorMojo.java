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

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.asciidoctor.*;
import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.maven.extensions.AsciidoctorJExtensionRegistry;
import org.asciidoctor.maven.extensions.ExtensionConfiguration;
import org.asciidoctor.maven.extensions.ExtensionRegistry;
import org.asciidoctor.maven.monitor.SynchronizingFileAlterationListenerAdaptor;
import org.asciidoctor.maven.io.AsciidoctorFileScanner;
import org.sonatype.plexus.build.incremental.BuildContext;


/**
 * Basic maven plugin to render AsciiDoc files using Asciidoctor, a ruby port.
 *
 */
@Mojo( name = "process-asciidoc")
public class AsciidoctorMojo extends AbstractMojo {
    // copied from org.asciidoctor.AsciiDocDirectoryWalker.ASCIIDOC_REG_EXP_EXTENSION
    // should probably be configured in AsciidoctorMojo through @Parameter 'extension'
    protected static final String ASCIIDOC_REG_EXP_EXTENSION = ".*\\.a((sc(iidoc)?)|d(oc)?)$";

    // Default source directory
    protected static final String SOURCE_DIRECTORY = "src/main/asciidoc";
    // Default output directory
    protected static final String OUTPUT_DIRECTORY = "${project.build.directory}/generated-docs";

    protected static final String FILE_ENCODING = System.getProperty("file.encoding");

    /**
     * Attributes
     */
    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDir", defaultValue = OUTPUT_DIRECTORY, required = true)
    protected File outputDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "preserveDirectories", defaultValue = "false", required = false)
    protected boolean preserveDirectories = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "relativeBaseDir", defaultValue = "false", required = false)
    protected boolean relativeBaseDir = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "projectDirectory", defaultValue = "${basedir}", required = false, readonly = false)
    protected File projectDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "rootDir", defaultValue = "${basedir}", required = false, readonly = false)
    protected File rootDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "baseDir", required = false)
    protected File baseDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "skip", required = false)
    protected boolean skip = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "gemPath", defaultValue = "", required = false)
    protected String gemPath = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "requires")
    protected List<String> requires = new ArrayList<String>();

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.ATTRIBUTES, required = false)
    protected Map<String, Object> attributes = new HashMap<String, Object>();

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.BACKEND, defaultValue = "docbook", required = true)
    protected String backend = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.DOCTYPE, required = false)
    protected String doctype;

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.ERUBY, required = false)
    protected String eruby = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "headerFooter", required = false)
    protected boolean headerFooter = true;

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateDir", required = false)
    protected File templateDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateEngine", required = false)
    protected String templateEngine;

    @Parameter(property = AsciidoctorMaven.PREFIX + "imagesDir", required = false)
    protected String imagesDir = "images"; // use a string because otherwise html doc uses absolute path

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceHighlighter", required = false)
    protected String sourceHighlighter = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + Attributes.TITLE, required = false)
    protected String title = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "synchronizations", required = false)
    protected List<Synchronization> synchronizations = new ArrayList<Synchronization>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "extensions")
    protected List<ExtensionConfiguration> extensions = new ArrayList<ExtensionConfiguration>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "embedAssets")
    protected boolean embedAssets = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "attributeMissing")
    protected String attributeMissing = "skip";

    @Parameter(property = AsciidoctorMaven.PREFIX + "attributeUndefined")
    protected String attributeUndefined = "drop-line";

    // List of AsciiDoc files or directories to render
    @Parameter(property = AsciidoctorMaven.PREFIX + "sources")
    protected List<Resource> sources;

    // List of resources to copy to the output directory (e.g., images, css). By default everything is copied
    @Parameter(property = AsciidoctorMaven.PREFIX + "sources")
    protected List<Resource> resources;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true )
    protected MavenSession session;

    @Component
    protected MavenResourcesFiltering outputResourcesFiltering;

    @Component
    protected BuildContext buildContext;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            getLog().info("AsciiDoc processing is skipped.");
            return;
        }

        ensureOutputExists();

        // Validate resources to avoid ugly errors later on
        if (resources != null) {
            for (Resource resource: resources) {
                if (resource.getDirectory() == null || resource.getDirectory().isEmpty()) {
                    throw new MojoExecutionException("Found empty resource directory");
                }
            }
        }

        // Asciidoctor initialization
        final Asciidoctor asciidoctor = getAsciidoctorInstance(gemPath);

        asciidoctor.requireLibraries(requires);

        final OptionsBuilder optionsBuilder = OptionsBuilder.options()
                .backend(backend)
                .safe(SafeMode.UNSAFE)
                .headerFooter(headerFooter)
                .eruby(eruby)
                .mkDirs(true);

        setOptions(optionsBuilder);

        final AttributesBuilder attributesBuilder = AttributesBuilder.attributes();

        setAttributesOnBuilder(attributesBuilder);

        optionsBuilder.attributes(attributesBuilder);

        ExtensionRegistry extensionRegistry = new AsciidoctorJExtensionRegistry(asciidoctor);
        for (ExtensionConfiguration extension: extensions) {
            try {
                extensionRegistry.register(extension.getClassName(), extension.getBlockName());
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        // Render AsciiDoc documents
        AsciidoctorFileScanner scanner = new AsciidoctorFileScanner(buildContext);
        if (sources == null) {
            sources = Arrays.asList(new Resource());
        }
        for (Resource source: sources) {
            if (source.getDirectory() == null || source.getDirectory().isEmpty()) {
                source.setDirectory(SOURCE_DIRECTORY);
            }
            for (final File sourceFile: scanner.scan(source)) {
                setDestinationPaths(optionsBuilder, sourceFile, new File(source.getDirectory()));
                renderFile(asciidoctor, optionsBuilder.asMap(), sourceFile);
            }
        }

        // Copy output resources
        // By default everything in the sources directories is copied
        if (resources == null  || resources.isEmpty()) {
            resources = new ArrayList<Resource>();
            // we don't want to copy files considered sources
            for (Resource source: sources) {
                Resource resource = new Resource();
                resource.setDirectory(source.getDirectory());
                if (source.getIncludes() != null) {
                    if (resource.getExcludes() == null || resource.getExcludes().isEmpty()) {
                        resource.setExcludes(new ArrayList<String>());
                    }
                }
                resource.getExcludes().addAll(source.getIncludes());
                resources.add(resource);
            }
        }
        // All resources must exclude AsciiDoc documents and folders beginning with underscore
        for (Resource resource: resources) {
            if (resource.getExcludes() == null || resource.getExcludes().isEmpty()) {
                resource.setExcludes(new ArrayList<String>());
            }
            List<String> excludes = new ArrayList<String>();
            for (String value: AsciidoctorFileScanner.IGNORED_FOLDERS_AND_FILES) {
                excludes.add(value);
            }
            for (String value: AsciidoctorFileScanner.DEFAULT_FILE_EXTENSIONS) {
                excludes.add(value);
            }
            // in case someone wants to include some of the default excluded files (.e.g., AsciiDoc docs)
            excludes.removeAll(resource.getIncludes());
            resource.getExcludes().addAll(excludes);
        }

        try {
            // TODO check if file encoding is set as property in the project.
            //      Right now it's not used at all, but could be used to apply resource filters/replacements
            MavenResourcesExecution resourcesExecution =
                    new MavenResourcesExecution(resources, outputDirectory, project, FILE_ENCODING,
                            Collections.<String>emptyList(), Collections.<String>emptyList(), session);
            resourcesExecution.setIncludeEmptyDirs(true);
            outputResourcesFiltering.filterResources(resourcesExecution);
        } catch (MavenFilteringException e) {
            throw new MojoExecutionException("Could not copy resources", e);
        }

        if (synchronizations != null && !synchronizations.isEmpty()) {
            synchronize();
        }
    }


    /**
     * Updates optionsBuilder object's baseDir and destination(s) accordingly to the options.
     *
     * @param optionsBuilder  AsciidoctorJ options to be updated
     * @param sourceFile      AsciiDoc source file to process
     * @param sourceDirectory Source directory as defined in the configuration of a {@link Resource}
     */
    private void setDestinationPaths(OptionsBuilder optionsBuilder, final File sourceFile, final File sourceDirectory)
            throws MojoExecutionException {
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
                String propostalPath = sourceFile.getParentFile().getCanonicalPath().substring(sourceDirectory.getCanonicalPath().length());
                File relativePath = new File(outputDirectory.getCanonicalPath() + propostalPath);
                optionsBuilder.toDir(relativePath).destinationDir(relativePath);
            } else {
                optionsBuilder.toDir(outputDirectory).destinationDir(outputDirectory);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to locate output directory", e);
        }
    }

    protected Asciidoctor getAsciidoctorInstance(String gemPath) throws MojoExecutionException {
        Asciidoctor asciidoctor = null;
        if (gemPath == null || gemPath.isEmpty()) {
            asciidoctor = Asciidoctor.Factory.create();
        }
        else {
            // Replace Windows path separator to avoid paths with mixed \ and /.
            // This happens for instance when setting: <gemPath>${project.build.directory}/gems-provided</gemPath>
            // because the project's path is converted to string.
            String normalizedGemPath = (File.separatorChar == '\\') ? gemPath.replaceAll("\\\\", "/") : gemPath;
            asciidoctor = Asciidoctor.Factory.create(normalizedGemPath);
        }

        String gemHome = JRubyRuntimeContext.get().evalScriptlet("ENV['GEM_HOME']").toString();
        String gemHomeExpected = (gemPath == null || "".equals(gemPath)) ? "" : gemPath.split(java.io.File.pathSeparator)[0];

        if (!"".equals(gemHome) && !gemHomeExpected.equals(gemHome)) {
            getLog().warn("Using inherited external environment to resolve gems (" + gemHome + "), i.e. build is platform dependent!");
        }

        return asciidoctor;
    }

    private void synchronize() {
        for (final Synchronization synchronization : synchronizations) {
            SynchronizingFileAlterationListenerAdaptor.synchronize(synchronization, getLog());
        }
    }

    protected void renderFile(Asciidoctor asciidoctor, Map<String, Object> options, File f) {
        asciidoctor.renderFile(f, options);
        logRenderedFile(f);
    }

    protected void logRenderedFile(File f) {
        getLog().info("Rendered " + f.getAbsolutePath());
    }

    protected void ensureOutputExists() {
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                getLog().error("Can't create " + outputDirectory.getPath());
            }
        }
    }

    protected void setOptions(OptionsBuilder optionsBuilder) {
        if (doctype != null) {
            optionsBuilder.docType(doctype);
        }

        if (templateEngine != null) {
            optionsBuilder.templateEngine(templateEngine);
        }

        if (templateDir != null) {
            optionsBuilder.templateDir(templateDir);
        }
    }

    protected void setAttributesOnBuilder(AttributesBuilder attributesBuilder) throws MojoExecutionException {
        if (sourceHighlighter != null) {
            attributesBuilder.sourceHighlighter(sourceHighlighter);
        }

        if (embedAssets) {
            attributesBuilder.linkCss(false);
            attributesBuilder.dataUri(true);
        }

        if (imagesDir != null) {
            attributesBuilder.imagesDir(imagesDir);
        }

        if ("skip".equals(attributeMissing) || "drop".equals(attributeMissing) || "drop-line".equals(attributeMissing)) {
            attributesBuilder.attributeMissing(attributeMissing);
        } else {
            throw new MojoExecutionException(attributeMissing + " is not valid. Must be one of 'skip', 'drop' or 'drop-line'");
        }

        if ("drop".equals(attributeUndefined) || "drop-line".equals(attributeUndefined)) {
            attributesBuilder.attributeUndefined(attributeUndefined);
        } else {
            throw new MojoExecutionException(attributeUndefined + " is not valid. Must be one of 'drop' or 'drop-line'");
        }

        // TODO Figure out how to reliably set other values (like boolean values, dates, times, etc)
        for (Map.Entry<String, Object> attributeEntry : attributes.entrySet()) {
            Object val = attributeEntry.getValue();
            // NOTE Maven interprets an empty value as null, so we need to explicitly convert it to empty string (see #36)
            // NOTE In Asciidoctor, an empty string represents a true value
            if (val == null || "true".equals(val)) {
                attributesBuilder.attribute(attributeEntry.getKey(), "");
            }
            // NOTE a value of false is effectively the same as a null value, so recommend the use of the string "false"
            else if ("false".equals(val)) {
                attributesBuilder.attribute(attributeEntry.getKey(), null);
            }
            // NOTE Maven can't assign a Boolean value from the XML-based configuration, but a client may
            else if (val instanceof Boolean) {
                attributesBuilder.attribute(attributeEntry.getKey(), Attributes.toAsciidoctorFlag((Boolean) val));
            }
            else {
                // Can't do anything about dates and times because all that logic is private in Attributes
                attributesBuilder.attribute(attributeEntry.getKey(), val);
            }
        }
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
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

    public File getTemplateDir() {
        return templateDir;
    }

    public void setTemplateDir(File templateDir) {
        this.templateDir = templateDir;
    }

    public String getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String getImagesDir() {
        return imagesDir;
    }

    public void setImagesDir(String imagesDir) {
        this.imagesDir = imagesDir;
    }

    public String getSourceHighlighter() {
        return sourceHighlighter;
    }

    public void setSourceHighlighter(String sourceHighlighter) {
        this.sourceHighlighter = sourceHighlighter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEruby() {
        return eruby;
    }

    public void setEruby(String eruby) {
        this.eruby = eruby;
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

    public String getAttributeMissing() {
        return attributeMissing;
    }

    public void setAttributeMissing(String attributeMissing) {
        this.attributeMissing = attributeMissing;
    }

    public String getAttributeUndefined() {
        return attributeUndefined;
    }

    public void setAttributeUndefined(String attributeUndefined) {
        this.attributeUndefined = attributeUndefined;
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

    public void setPreserveDirertories(boolean preserveDirertories) {
        this.preserveDirectories = preserveDirertories;
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

    public List<Resource> getSources() {
        return sources;
    }

    public void setSources(List<Resource> sources) {
        this.sources = sources;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

}
