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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.AsciiDocDirectoryWalker;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.DirectoryWalker;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;


/**
 * Basic maven plugin to render asciidoc files using asciidoctor, a ruby port.
 * <p/>
 * Uses jRuby to invoke a small script to process the asciidoc files.
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
    protected String templateEngine = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "imagesDir", required = false)
    protected String imagesDir = "images"; // use a string because otherwise html doc uses absolute path

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceHighlighter", required = false)
    protected String sourceHighlighter = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + Attributes.TITLE, required = false)
    protected String title = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "copyCss")
    protected boolean copyCss = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "dataUri")
    protected boolean dataUri = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "docTime")
    protected Date docTime;

    @Parameter(property = AsciidoctorMaven.PREFIX + Attributes.EXPERIMENTAL)
    protected boolean experimental;

    @Parameter(property = AsciidoctorMaven.PREFIX + "admonitionWithFontAwesome")
    protected boolean fontawesome = true;

    @Parameter(property = AsciidoctorMaven.PREFIX + Attributes.ICONS)
    protected String icons = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "iconsDir")
    protected String iconsDir = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "linkAttrs")
    protected boolean linkAttrs;

    @Parameter(property = AsciidoctorMaven.PREFIX + "linkCss")
    protected boolean linkCss;

    @Parameter(property = AsciidoctorMaven.PREFIX + "localDate")
    protected Date localDate;

    @Parameter(property = AsciidoctorMaven.PREFIX + "localTime")
    protected Date localTime;

    @Parameter(property = AsciidoctorMaven.PREFIX + "noStylesheetName")
    protected boolean notStylesheetName = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "originalAdmonitionIcons")
    protected boolean originalAdmonitionIconsWithImage = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "stylesDir")
    protected String stylesDir = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + "stylesheetName")
    protected String stylesheetName = "";

    @Parameter(property = AsciidoctorMaven.PREFIX + Attributes.TOC)
    protected boolean toc;

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDocumentName", required = false)
    protected File sourceDocumentName;

    protected List<Synchronization> synchronizations = new ArrayList<Synchronization>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "extensions")
    protected List<String> extensions = new ArrayList<String>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ensureOutputExists();

        final Asciidoctor asciidoctorInstance = getAsciidoctorInstance();

        final OptionsBuilder optionsBuilder = OptionsBuilder.options().toDir(outputDirectory).compact(compact)
                .headerFooter(headerFooter).safe(SafeMode.UNSAFE).templateEngine(templateEngine)
                .eruby(eruby).backend(backend).docType(doctype).headerFooter(headerFooter);

        if (templateDir != null) {
            optionsBuilder.templateDir(templateDir);
        }

        final AttributesBuilder attributesBuilder = AttributesBuilder.attributes().attributes(attributes)
                .sourceHighlighter(sourceHighlighter).title(title).imagesDir(imagesDir).copyCss(copyCss)
                .dataUri(dataUri).experimental(experimental).icons(icons).iconsDir(iconsDir)
                .linkAttrs(linkAttrs).linkCss(linkCss).stylesDir(stylesDir).styleSheetName(stylesheetName)
                .tableOfContents(toc);

        if (docTime != null) {
            attributesBuilder.docTime(docTime);
        }

        if (localDate != null) {
            attributesBuilder.localDate(localDate);
        }

        if (localTime != null) {
            attributesBuilder.localTime(localTime);
        }

        if (fontawesome) {
            attributesBuilder.icons(Attributes.FONTAWESOME_ADMONITION_ICONS);
        }
        if (notStylesheetName) {
            attributesBuilder.unsetStyleSheet();
        }

        if (originalAdmonitionIconsWithImage) {
            attributesBuilder.icons(Attributes.ICONS);
        }

        optionsBuilder.attributes(attributesBuilder.get());

        if (sourceDocumentName == null) {
            for (final File f : scanSourceFiles()) {
                renderFile(asciidoctorInstance, optionsBuilder.asMap(), f);
            }
        } else {
            renderFile(asciidoctorInstance, optionsBuilder.asMap(), sourceDocumentName);
        }

        if (synchronizations != null) {
            synchronize();
        }
    }

    protected Asciidoctor getAsciidoctorInstance() throws MojoExecutionException {
        return Asciidoctor.Factory.create();
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

    public boolean isCopyCss() {
        return copyCss;
    }

    public void setCopyCss(boolean copyCss) {
        this.copyCss = copyCss;
    }

    public boolean isDataUri() {
        return dataUri;
    }

    public void setDataUri(boolean dataUri) {
        this.dataUri = dataUri;
    }

    public Date getDocTime() {
        return docTime;
    }

    public void setDocTime(Date docTime) {
        this.docTime = docTime;
    }

    public boolean isExperimental() {
        return experimental;
    }

    public void setExperimental(boolean experimental) {
        this.experimental = experimental;
    }

    public boolean isFontawesome() {
        return fontawesome;
    }

    public void setFontawesome(boolean fontawesome) {
        this.fontawesome = fontawesome;
    }

    public String getIcons() {
        return icons;
    }

    public void setIcons(String icons) {
        this.icons = icons;
    }

    public String getIconsDir() {
        return iconsDir;
    }

    public void setIconsDir(String iconsDir) {
        this.iconsDir = iconsDir;
    }

    public boolean isLinkAttrs() {
        return linkAttrs;
    }

    public void setLinkAttrs(boolean linkAttrs) {
        this.linkAttrs = linkAttrs;
    }

    public boolean isLinkCss() {
        return linkCss;
    }

    public void setLinkCss(boolean linkCss) {
        this.linkCss = linkCss;
    }

    public Date getLocalDate() {
        return localDate;
    }

    public void setLocalDate(Date localDate) {
        this.localDate = localDate;
    }

    public Date getLocalTime() {
        return localTime;
    }

    public void setLocalTime(Date localTime) {
        this.localTime = localTime;
    }

    public boolean isNotStylesheetName() {
        return notStylesheetName;
    }

    public void setNotStylesheetName(boolean notStylesheetName) {
        this.notStylesheetName = notStylesheetName;
    }

    public boolean isOriginalAdmonitionIconsWithImage() {
        return originalAdmonitionIconsWithImage;
    }

    public void setOriginalAdmonitionIconsWithImage(boolean originalAdmonitionIconsWithImage) {
        this.originalAdmonitionIconsWithImage = originalAdmonitionIconsWithImage;
    }

    public String getStylesDir() {
        return stylesDir;
    }

    public void setStylesDir(String stylesDir) {
        this.stylesDir = stylesDir;
    }

    public String getStylesheetName() {
        return stylesheetName;
    }

    public void setStylesheetName(String stylesheetName) {
        this.stylesheetName = stylesheetName;
    }

    public boolean isToc() {
        return toc;
    }

    public void setToc(boolean toc) {
        this.toc = toc;
    }

    public File getSourceDocumentName() {
        return sourceDocumentName;
    }

    public void setSourceDocumentName(File sourceDocumentName) {
        this.sourceDocumentName = sourceDocumentName;
    }

    public List<Synchronization> getSynchronizations() {
        return synchronizations;
    }

    public void setSynchronizations(List<Synchronization> synchronizations) {
        this.synchronizations = synchronizations;
    }

    private static class CustomExtensionDirectoryWalker extends DirectoryWalker {
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
}
