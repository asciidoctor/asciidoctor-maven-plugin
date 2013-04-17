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

import java.util.Map;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.AsciidocDirectoryWalker;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;

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
    protected Map<String,String> attributes = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ensureOutputExists();

        final Asciidoctor instance = Asciidoctor.Factory.create();
        final List<File> asciidocFiles = new AsciidocDirectoryWalker(sourceDirectory.getAbsolutePath()).scan();

        final OptionsBuilder optionsBuilder = OptionsBuilder.options().toDir(outputDirectory);
        final AttributesBuilder attributesBuilder = AttributesBuilder.attributes().backend(backend);
        optionsBuilder.attributes(attributesBuilder.asMap());


// TODO: add backend, doctype and attributes
        for (final File f : asciidocFiles) {
            instance.renderFile(f.getAbsolutePath(), optionsBuilder.asMap());
        }
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

// TODO: getter / setter doctype

    public Map<String,String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String,String> attributes) {
        this.attributes = attributes;
    }
}
