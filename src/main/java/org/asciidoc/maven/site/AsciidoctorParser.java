/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.asciidoc.maven.site;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import org.apache.maven.doxia.module.xhtml.XhtmlParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.Asciidoctor;
import org.codehaus.plexus.component.annotations.Component;

import org.codehaus.plexus.util.IOUtil;

/**
 *
 * @author jdlee
 */
@Component( role = Parser.class, hint = AsciidoctorParser.ROLE_HINT )
public class AsciidoctorParser extends XhtmlParser {

    /**
     * The role hint for the {@link MarkdownParser} Plexus component.
     */
    public static final String ROLE_HINT = "asciidoc";
    protected final Asciidoctor asciidoctorInstance = Asciidoctor.Factory.create();

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(Reader source, Sink sink) throws ParseException {
        try {
            super.parse(new StringReader("<html><body>" + 
                    asciidoctorInstance.render(IOUtil.toString(source), new HashMap())+ 
                    "</body></html>"), sink);
        } catch (IOException e) {
            throw new ParseException("Failed reading Asciidoc source document", e);
        }
    }
}
