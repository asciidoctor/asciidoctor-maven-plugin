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

package org.asciidoc.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.AsciiDocDirectoryWalker;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.DirectoryWalker;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic maven plugin to render asciidoc files using asciidoctor, a ruby port.
 *
 * Uses jRuby to invoke a small script to process the asciidoc files.
 */
@Mojo(name = "process-asciidoc")
public class AsciidoctorMojo extends AbstractMojo {
    @Parameter(property = "sourceDir", defaultValue = "${basedir}/src/main/asciidoc", required = true)
    protected File sourceDirectory;

    @Parameter(property = "outputDir", defaultValue = "${project.build.directory}/generated-docs", required = true)
    protected File outputDirectory;

    @Parameter(property = "backend", defaultValue = "docbook", required = true)
    protected String backend;

    @Parameter(property = "doctype", defaultValue = "article", required = true)
    protected String doctype;

    @Parameter(property = "attributes", required = false)
    protected Map<String,String> attributes = new HashMap<String, String>();

    @Parameter(property = "compact", required = false)
    protected boolean compact = false;

    @Parameter(property = "headerFooter", required = false)
    protected boolean headerFooter = false;

    @Parameter(property = "templateDir", required = false)
    protected String templateDir;

    @Parameter(property = "templateEngine", required = false)
    protected String templateEngine;

    @Parameter(property = "imagesDir", required = false)
    protected File imagesDir = new File(sourceDirectory, "images");

    @Parameter(property = "sourceHighlighter", required = false)
    protected String sourceHighlighter;

    @Parameter(property = "title", required = false)
    protected String title;

    @Parameter(property = "sourceDocumentName", required = false)
    protected File sourceDocumentName;

    @Parameter(property = "extension", required = false)
    protected String extension;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ensureOutputExists();

        final Asciidoctor asciidoctorInstance = Asciidoctor.Factory.create();

        final OptionsBuilder optionsBuilder = OptionsBuilder.options().toDir(outputDirectory).compact(compact)
                .headerFooter(headerFooter).safe(SafeMode.UNSAFE).templateDir(templateDir).templateEngine(templateEngine);
        final AttributesBuilder attributesBuilder = AttributesBuilder.attributes().backend(backend).docType(doctype)
                .imagesDir(imagesDir).sourceHighlighter(sourceHighlighter).title(title);

        // FIXME: There needs to be a better way to do this -- talk to Alex
        final Map<String, Object> attributesMap = attributesBuilder.asMap();
        attributesMap.putAll(attributes);

        optionsBuilder.attributes(attributesMap);

        final Map<String, Object> options = optionsBuilder.asMap();

        if (sourceDocumentName == null) {
            for (final File f : scanSourceFiles()) {
                renderFile(asciidoctorInstance, options, f);
            }
        } else {
            renderFile(asciidoctorInstance, options, sourceDocumentName);
        }

    }

    private List<File> scanSourceFiles() {
        final List<File> asciidoctorFiles;
        if (extension == null) {
            final DirectoryWalker directoryWalker = new AsciiDocDirectoryWalker(sourceDirectory.getAbsolutePath());
            asciidoctorFiles = directoryWalker.scan();
        } else {
            final DirectoryWalker directoryWalker = new CustomExtensionDirectoryWalker(sourceDirectory.getAbsolutePath(), extension);
            asciidoctorFiles = directoryWalker.scan();
        }
        return asciidoctorFiles;
    }

    private void renderFile(Asciidoctor asciidoctorInstance, Map<String, Object> options, File f) {
        asciidoctorInstance.renderFile(f, options);
        logRenderedFile(f);
    }

    private void logRenderedFile(File f) {
        getLog().info("Rendered " + f.getAbsolutePath());
    }

    private void ensureOutputExists() {
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

    public Map<String,String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String,String> attributes) {
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

    public String getTemplateDir() {
        return templateDir;
    }

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public String getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    public File getImagesDir() {
        return imagesDir;
    }

    public void setImagesDir(File imagesDir) {
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

    private static class CustomExtensionDirectoryWalker extends DirectoryWalker {
        private final String extension;

        public CustomExtensionDirectoryWalker(final String absolutePath, final String extension) {
            super(absolutePath);
            this.extension = extension;
        }

        @Override
        protected boolean isAcceptedFile(final File filename) {
            return filename.getName().endsWith(extension);
        }
    }
}
