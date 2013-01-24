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
    protected File siteDirectory;

    @Parameter(property = "outputDir", defaultValue = "${project.build.directory}", required = true)
    protected File outputDirectory;

    @Parameter(property = "backend", defaultValue = "docbook", required = true)
    protected String backend;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getSiteDirectory() {
        return siteDirectory;
    }

    public void setSiteDirectory(File siteDirectory) {
        this.siteDirectory = siteDirectory;
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
