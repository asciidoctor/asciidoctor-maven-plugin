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
package org.asciidoctor.maven.site;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.maven.doxia.module.xhtml.XhtmlParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;

/**
 * This class is used by the Doxia framework to handle the actual parsing of the
 * AsciiDoc input files into HTML to be consumed/wrapped by the site generation
 * process.
 *
 * @author jdlee
 */
@Component(role = Parser.class, hint = AsciidoctorParser.ROLE_HINT)
public class AsciidoctorParser extends XhtmlParser {

    /**
     * The role hint for the {@link AsciiDoctorParser} Plexus component.
     */
    public static final String ROLE_HINT = "asciidoc";
    protected final Asciidoctor asciidoctorInstance = Asciidoctor.Factory.create();

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(Reader source, Sink sink) throws ParseException {
        try {
            String result = asciidoctorInstance.render(IOUtil.toString(source),
                        OptionsBuilder.options().headerFooter(true).safe(SafeMode.UNSAFE).backend("xhtml").asMap());
            // prevent site plugin from breaking font-based icon syntax
            result = result.replaceAll("<i class=\"fa icon-([^\"]+)\"([^>]*)></i>", "<span class=\"fa icon-$1\"$2></span>");
            super.parse(new StringReader(result), sink);
        } catch (IOException ex) {
            getLog().error(ex.getLocalizedMessage());
        }
    }
}
