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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 */
@Mojo(name = "process-asciidoc")
public class AsciidoctorMojo extends AbstractMojo {
    @Parameter(property = "sourceDir", defaultValue = "${basedir}/src/asciidoc", required = true)
    protected File sourceDirectory;

    @Parameter(property = "outputDir", defaultValue = "${project.build.directory}", required = true)
    protected File outputDirectory;

    @Parameter(property = "backend", defaultValue = "docbook", required = true)
    protected String backend;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        final ScriptEngine rubyEngine = engineManager.getEngineByName("jruby");
        final Bindings bindings = new SimpleBindings();

        bindings.put("srcDir", sourceDirectory.getAbsolutePath());
        bindings.put("outputDir", outputDirectory.getAbsolutePath());
        bindings.put("backend", backend);

        try {
            final InputStream script = AbstractMojo.class.getClassLoader().getResourceAsStream("execute_asciidoctor.rb");
            final InputStreamReader streamReader = new InputStreamReader(script);
            rubyEngine.eval(streamReader, bindings);
        } catch (ScriptException e) {
            getLog().error("Error running ruby script", e);
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
}
