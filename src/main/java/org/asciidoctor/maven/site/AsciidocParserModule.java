package org.asciidoctor.maven.site;

import org.apache.maven.doxia.parser.module.AbstractParserModule;
import org.apache.maven.doxia.parser.module.ParserModule;
import org.codehaus.plexus.component.annotations.Component;

/**
 * This class is the entry point for the site plugin integration for Doxia 1.6+ for files with extension 'asciidoc'.
 *
 * @author paranoiabla
 */
@Component(role = ParserModule.class, hint = AsciidoctorParser.ROLE_HINT)
public class AsciidocParserModule extends AbstractParserModule {

    /**
     * The source directory for AsciiDoc files.
     */
    public static final String SOURCE_DIRECTORY = AsciidoctorParser.ROLE_HINT;

    /**
     * The extension for AsciiDoc files.
     */
    public static final String FILE_EXTENSION = "asciidoc";

    /**
     * Build a new instance of {@link AsciidocParserModule}.
     */
    public AsciidocParserModule() {
        super(SOURCE_DIRECTORY, FILE_EXTENSION, AsciidoctorParser.ROLE_HINT);
    }
}
