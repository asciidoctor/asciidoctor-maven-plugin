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
package org.asciidoctor.maven.pdf;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class YamlsTest {
    @Test
    public void run() throws MojoExecutionException, IOException {
        final File theme = new File("src/test/resources/yaml/theme1.yml");
        final File diff = new File("src/test/resources/yaml/theme-diff.yml");
        final Yamls yamls = new Yamls();

        final Object theme1 = yamls.read(theme);
        final Object patch = yamls.read(diff);
        final Object merged = yamls.mergeYamls(theme1, patch);

        final File tempTheme = new File("target/" + getClass().getSimpleName() + "-merged.yml");
        yamls.write(tempTheme, merged);

        assertEquals(
                IOUtils.toString(new File("src/test/resources/yaml/expected.yml").toURI().toURL()).replaceAll("\r", ""),
                IOUtils.toString(tempTheme.toURI().toURL()).replaceAll("\r", ""));
    }
}
