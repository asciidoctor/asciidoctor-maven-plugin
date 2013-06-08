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
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.DirectoryWalker;
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

    @Parameter(property = AsciidoctorMaven.PREFIX + "backend", defaultValue = "docbook", required = true)
    protected String backend;

    @Parameter(property = AsciidoctorMaven.PREFIX + "doctype", defaultValue = "article", required = true)
    protected String doctype;

    @Parameter(property = AsciidoctorMaven.PREFIX + "attributes", required = false)
    protected Map<String, Object> attributes = new HashMap<String, Object>();

    @Parameter(property = AsciidoctorMaven.PREFIX + "compact", required = false)
    protected boolean compact = false;

    @Parameter(property = AsciidoctorMaven.PREFIX + "headerFooter", required = false)
    protected boolean headerFooter = true;

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateDir", required = false)
    protected String templateDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "templateEngine", required = false)
    protected String templateEngine;

    @Parameter(property = AsciidoctorMaven.PREFIX + "imagesDir", required = false)
    protected String imagesDir = "images"; // use a string because otherwise html doc uses absolute path

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceHighlighter", required = false)
    protected String sourceHighlighter;

    @Parameter(property = AsciidoctorMaven.PREFIX + "title", required = false)
    protected String title;

    @Parameter(property = AsciidoctorMaven.PREFIX + "sourceDocumentName", required = false)
    protected File sourceDocumentName;

    @Parameter
    protected List<Synchronization> synchronizations;

    @Parameter
    protected List<String> extensions;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ensureOutputExists();

        final Asciidoctor asciidoctorInstance = getAsciidoctorInstance();

        final OptionsBuilder optionsBuilder = OptionsBuilder.options().toDir(outputDirectory).compact(compact)
                .headerFooter(headerFooter).safe(SafeMode.UNSAFE).templateDir(templateDir).templateEngine(templateEngine);
        final AttributesBuilder attributesBuilder = AttributesBuilder.attributes().backend(backend).docType(doctype)
                .sourceHighlighter(sourceHighlighter).title(title);

        for (String key : attributes.keySet()) {
            Object val = attributes.get(key);
            if (val == null) {
                attributes.put(key, "");
            } else if (val.equals(false)) {
                attributes.put(key, null);
            }
        }

        // FIXME: There needs to be a better way to do this -- talk to Alex
        final Map<String, Object> attributesMap = attributesBuilder.asMap();
        attributesMap.putAll(attributes);

        optionsBuilder.attributes(attributesMap);

        // temp hack, see https://github.com/asciidoctor/asciidoctor-java-integration/issues/26
        final Map<String, Object> options = optionsBuilder.asMap();

        options.put("imagesdir", imagesDir);

        if (sourceDocumentName == null) {
            for (final File f : scanSourceFiles()) {
                renderFile(asciidoctorInstance, options, f);
            }
        } else {
            renderFile(asciidoctorInstance, options, sourceDocumentName);
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
