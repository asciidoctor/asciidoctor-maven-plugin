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
package org.asciidoc.maven.site;

import org.apache.maven.doxia.module.site.AbstractSiteModule;
import org.apache.maven.doxia.module.site.SiteModule;
import org.codehaus.plexus.component.annotations.Component;

/**
 * This class is the entry point for the site plugin integration.
 *
 * @author jdlee
 */
@Component(role = SiteModule.class, hint = AsciidoctorParser.ROLE_HINT)
public class AsciidoctorSiteModule extends AbstractSiteModule {

    /**
     * The source directory for AsciiDoc files.
     */
    public static final String SOURCE_DIRECTORY = "asciidoc";
    /**
     * The extension for AsciiDoc files.
     */
    public static final String FILE_EXTENSION = "ad";

    /**
     * Build a new instance of {@link AsciidoctorSiteModule}.
     */
    public AsciidoctorSiteModule() {
        super(SOURCE_DIRECTORY, FILE_EXTENSION, AsciidoctorParser.ROLE_HINT);
    }
}
