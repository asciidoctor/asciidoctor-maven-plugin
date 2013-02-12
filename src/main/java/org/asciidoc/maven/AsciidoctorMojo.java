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

import java.io.*;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Basic maven plugin to render asciidoc files using asciidoctor, a ruby port.
 *
 * Uses jRuby to invoke a small script to process the asciidoc files.
 */
@Mojo(name = "process-asciidoc")
public class AsciidoctorMojo extends AbstractMojo {
    @Parameter(property = "sourceDir", defaultValue = "${basedir}/src/asciidoc", required = true)
    protected File sourceDirectory;

    @Parameter(property = "outputDir", defaultValue = "${project.build.directory}", required = true)
    protected File outputDirectory;

    @Parameter(property = "backend", defaultValue = "docbook", required = true)
    protected String backend;

    @Parameter(property = "cssStyles")
    protected File[] cssStyles;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        final ScriptEngine rubyEngine = engineManager.getEngineByName("jruby");
        final Bindings bindings = new SimpleBindings();

        bindings.put("srcDir", sourceDirectory.getAbsolutePath());
        bindings.put("outputDir", outputDirectory.getAbsolutePath());
        bindings.put("backend", backend);

        try {
            final InputStream script = getClass().getClassLoader().getResourceAsStream("execute_asciidoctor.rb");
            final InputStreamReader streamReader = new InputStreamReader(script);
            rubyEngine.eval(streamReader, bindings);
        } catch (ScriptException e) {
            getLog().error("Error running ruby script", e);
        }

        if ("html".equals(backend)) {
            getLog().debug("Applying CSS styles");
            applyCSSStyles();
        }
    }

    private void applyCSSStyles() throws MojoExecutionException {
        if (cssStyles != null) {

            for (File cssStyle : cssStyles) {
                try {
                    if (!FileUtils.directoryContains(outputDirectory, cssStyle)) {

                        FileUtils.copyFileToDirectory(cssStyle, outputDirectory);
                        getLog().debug("Copied " + cssStyle.getName() + " to " + outputDirectory);
                    }
                } catch (IOException e) {
                    throw new MojoExecutionException("Could not copy " + cssStyle.getAbsolutePath() + " to " + outputDirectory.getAbsolutePath(), e);
                }
            }

            final File[] htmls = outputDirectory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("html");
                }
            });

            getLog().info("Adding CSS Styles to " + htmls.length + " HTML pages");

            for (File html : htmls) {
                try {
                    final Document parse = Jsoup.parse(html, null);
                    final Element head = parse.head();
                    for (File cssStyle : cssStyles) {
                        getLog().debug("Adding CSS Style" + cssStyle.getAbsolutePath() + " to " + html.getAbsolutePath() + " HTML pages");

                        head.appendElement("link").attr("rel", "stylesheet").attr("href", FilenameUtils.getName(cssStyle.getAbsolutePath()));

                        FileUtils.writeStringToFile(html, parse.html());
                    }
                } catch (Exception e) {
                    throw new MojoExecutionException("An error occured while adding CSS styles to the HTML pages", e);
                }
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

    public File[] getCssStyles() {
        return cssStyles;
    }

    public void setCssStyles(File[] cssStyles) {
        this.cssStyles = cssStyles;
    }
}
