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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.asciidoc.maven.io.Zips;

import java.io.File;
import java.io.IOException;

@Mojo(name = "zip")
public class AsciidoctorZipMojo extends AsciidoctorMojo {
    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = AsciidoctorMaven.PREFIX + "attach", defaultValue = "true")
    protected boolean attach;

    @Parameter(property = AsciidoctorMaven.PREFIX + "zip", defaultValue = "true")
    protected boolean zip;

    @Parameter(property = AsciidoctorMaven.PREFIX + "zipDestination", defaultValue = "${project.build.directory}/${project.build.finalName}.zip")
    protected File zipDestination;

    @Parameter(property = AsciidoctorMaven.PREFIX + "zipClassifier", defaultValue = "asciidoctor")
    protected String zipClassifier;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        if (zip) {
            try {
                Zips.zip(outputDirectory, zipDestination);
            } catch (final IOException e) {
                getLog().error("Can't zip " + outputDirectory.getAbsolutePath(), e);
            }
            if (attach) {
                if (zipClassifier != null) {
                    projectHelper.attachArtifact(project, "zip", zipClassifier, zipDestination);
                } else {
                    projectHelper.attachArtifact(project, "zip", zipDestination);
                }
            }
        }
    }
}
