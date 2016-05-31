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

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Yamls {
    public Object read(final File theme) throws MojoExecutionException {
        FileReader reader = null;
        try {
            reader = new FileReader(theme);
            final YamlReader loader = new YamlReader(reader);
            return loader.read();
        } catch (final FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (final YamlException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public Object mergeYamls(final Object ref, final Object diff) {
        final Map<String, Object> map1 = Map.class.cast(ref);
        final Map<String, Object> map2 = Map.class.cast(diff);
        final Map<String, Object> out = new HashMap<String, Object>();
        out.putAll(map1);
        for (final Map.Entry<String, Object> entry : map2.entrySet()) {
            if (out.containsKey(entry.getKey()) && Map.class.isInstance(entry.getValue())) {
                out.put(entry.getKey(), mergeYamls(map1.get(entry.getKey()), entry.getValue()));
            } else {
                out.put(entry.getKey(), entry.getValue());
            }
        }
        return out;
    }

    public void write(final File tempTheme, final Object theme) throws MojoExecutionException {
        YamlWriter writer;
        try {
            writer = new YamlWriter(new FileWriter(tempTheme));
            writer.write(theme);
            writer.close();
        } catch (final YamlException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
