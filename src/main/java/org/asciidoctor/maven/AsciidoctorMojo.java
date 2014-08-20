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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.AbstractDirectoryWalker;
import org.asciidoctor.AsciiDocDirectoryWalker;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.DirectoryWalker;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.internal.RubyUtils;


/**
 * Basic maven plugin to render Asciidoc files using Asciidoctor, a ruby port.
 */
@Mojo(name = "process-asciidoc")
public class AsciidoctorMojo extends AbstractMojo {
    // copied from org.asciidoctor.AsciiDocDirectoryWalker.ASCIIDOC_REG_EXP_EXTENSION
    // should probably be configured in AsciidoctorMojo through @Parameter 'extension'
    protected static final String ASCIIDOC_REG_EXP_EXTENSION = ".*\\.a((sc(iidoc)?)|d(oc)?)$";

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDir", defaultValue = "${basedir}/src/main/asciidoc", required = true)
    protected File sourceDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDir", defaultValue = "${project.build.directory}/generated-docs", required = true)
    protected File outputDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "projectDirectory", defaultValue = "${basedir}", required = false, readonly = false)
    protected File projectDirectory;

    @Parameter(property = AsciidoctorMaven.PREFIX + "rootDir", defaultValue = "${basedir}", required = false, readonly = false)
    protected File rootDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "baseDir", required = false)
    protected File baseDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "gemPath", defaultValue = "", required = false)
    protected String gemPath = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "requires")
    protected List<String> requires = new ArrayList<String>();

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.ATTRIBUTES, required = false)
    protected Map<String, Object> attributes = new HashMap<String, Object>();

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.BACKEND, defaultValue = "docbook", required = true)
    protected String backend = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.COMPACT, required = false)
    protected boolean compact = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + Options.DOCTYPE, defaultValue = "article", required = true)
    protected String doctype = "article";

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

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDocumentName", required = false)
    protected String sourceDocumentName;

    @Parameter(property = AsciidoctorMaven.PREFIX + "synchronizations", required = false)
    protected List<Synchronization> synchronizations = new ArrayList<Synchronization>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "extensions")
    protected List<String> extensions = new ArrayList<String>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "embedAssets")
    protected boolean embedAssets = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "attributeMissing")
    protected String attributeMissing = "skip";

    @Parameter(property = AsciidoctorMaven.PREFIX + "attributeUndefined")
    protected String attributeUndefined = "drop-line";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ensureOutputExists();

        final Asciidoctor asciidoctorInstance = getAsciidoctorInstance(gemPath);

        if (requires.size() > 0) {
            for (String require : requires) {
                // FIXME AsciidoctorJ should provide a public API for requiring paths in the Ruby runtime
                RubyUtils.requireLibrary(JRubyRuntimeContext.get(), require);
            }
        }

        final OptionsBuilder optionsBuilder = OptionsBuilder.options().toDir(outputDirectory).compact(compact)
                .safe(SafeMode.UNSAFE).eruby(eruby).backend(backend).docType(doctype).headerFooter(headerFooter);

        final AttributesBuilder attributesBuilder = AttributesBuilder.attributes();

        if (sourceDirectory == null) {
            throw new MojoExecutionException("Required parameter 'asciidoctor.sourceDir' not set.");
        }

        optionsBuilder.baseDir(sourceDirectory).toDir(outputDirectory).destinationDir(outputDirectory).mkDirs(true);

        if (baseDir != null) {
            optionsBuilder.baseDir(baseDir);
        }

        setOptions(optionsBuilder);

        setAttributesOnBuilder(attributesBuilder);

        optionsBuilder.attributes(attributesBuilder.get());

        if (sourceDocumentName == null) {
            for (final File f : scanSourceFiles()) {
                renderFile(asciidoctorInstance, optionsBuilder.asMap(), f);
            }
        } else {
            renderFile(asciidoctorInstance, optionsBuilder.asMap(), new File(sourceDirectory, sourceDocumentName));
        }

        // #67 -- get all files that aren't adoc/ad/asciidoc and create synchronizations for them
        try {
            FileUtils.copyDirectory(sourceDirectory, outputDirectory, new NonAsciiDocExtensionFileFilter(), false);
        } catch (IOException e) {
            throw new MojoExecutionException("Error copying resources", e);
        }

        if (synchronizations != null && !synchronizations.isEmpty()) {
            synchronize();
        }
    }

    protected Asciidoctor getAsciidoctorInstance(String gemPath) throws MojoExecutionException {
        if (gemPath == null) {
            return Asciidoctor.Factory.create();
        }
        else {
            return Asciidoctor.Factory.create(gemPath);
        }
    }

    private List<File> scanSourceFiles() {
        final List<File> asciidoctorFiles;
        if (extensions == null || extensions.isEmpty()) {
            final DirectoryWalker directoryWalker = new AsciiDocDirectoryWalker(sourceDirectory.getAbsolutePath());
            asciidoctorFiles = directoryWalker.scan();
        } else {
            final DirectoryWalker directoryWalker = new CustomExtensionDirectoryWalker(sourceDirectory.getAbsolutePath(), extensions);
            asciidoctorFiles = directoryWalker.scan();
        }
        String absoluteSourceDirectory = sourceDirectory.getAbsolutePath();
        for (Iterator<File> iter = asciidoctorFiles.iterator(); iter.hasNext();) {
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

    protected void renderFile(Asciidoctor asciidoctorInstance, Map<String, Object> options, File f) {
        asciidoctorInstance.renderFile(f, options);
        logRenderedFile(f);
    }

    protected void logRenderedFile(File f) {
        getLog().info("Rendered " + f.getAbsolutePath());
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

    protected void setOptions(OptionsBuilder optionsBuilder) {
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
            if (attributeEntry.getValue() instanceof Boolean) {
                attributesBuilder.attribute(attributeEntry.getKey(), Attributes.toAsciidoctorFlag((Boolean) attributeEntry.getValue()));
                continue;
            }
            // Can't do anything about dates and times because all that logic is private in Attributes
            attributesBuilder.attribute(attributeEntry.getKey(), attributeEntry.getValue());
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

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
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

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
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

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(final List<String> extensions) {
        this.extensions = extensions;
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

    private static class CustomExtensionDirectoryWalker extends AbstractDirectoryWalker {
        private final List<String> extensions;

        public CustomExtensionDirectoryWalker(final String absolutePath, final List<String> extensions) {
            super(absolutePath);
            this.extensions = extensions;
        }

        @Override
        protected boolean isAcceptedFile(final File filename) {
            final String name = filename.getName();
            for (final String extension : extensions) {
                if (name.endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class NonAsciiDocExtensionFileFilter implements FileFilter {
        private final List<String> extensions;

        public NonAsciiDocExtensionFileFilter() {
            this.extensions = java.util.Arrays.asList("ad", "adoc", "asciidoc");
        }

        @Override
        public boolean accept(File pathname) {
            final String name = pathname.getName();
            for (final String extension : extensions) {
                if (name.endsWith(extension)) {
                    return false;
                }
            }
            return true;
        }
    }
}
