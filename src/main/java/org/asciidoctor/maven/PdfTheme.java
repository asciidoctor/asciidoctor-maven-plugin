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

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public class PdfTheme {
    @Parameter(property = AsciidoctorMaven.PREFIX + "pdf-style")
    private String style;

    @Parameter(property = AsciidoctorMaven.PREFIX + "pdf-stylesdir")
    private File stylesDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "pdf-fontsdir")
    private File fontsDir;

    @Parameter(property = AsciidoctorMaven.PREFIX + "pdf-patch")
    private File themePatch;

    public String getStyle() {
        return style;
    }

    public void setStyle(final String style) {
        this.style = style;
    }

    public File getStylesDir() {
        return stylesDir;
    }

    public void setStylesDir(final File stylesDir) {
        this.stylesDir = stylesDir;
    }

    public File getFontsDir() {
        return fontsDir;
    }

    public void setFontsDir(final File fontsDir) {
        this.fontsDir = fontsDir;
    }

    public File getThemePatch() {
        return themePatch;
    }

    public void setThemePatch(final File themePatch) {
        this.themePatch = themePatch;
    }
}
