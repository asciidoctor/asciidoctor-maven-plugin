package org.asciidoctor.maven.site;

import org.apache.maven.doxia.parser.module.AbstractParserModule;
import org.apache.maven.doxia.parser.module.ParserModule;
import org.codehaus.plexus.component.annotations.Component;

/**
 * This class is the entry point for integration with the Maven Site Plugin
 * integration since Doxia 1.6 (i.e., maven-site-plugin 3.4 and above):
 * it defines source directory and file extensions to be added to
 * <a href="https://maven.apache.org/doxia/references/">Doxia provided modules</a>.
 *
 * @author jdlee
 */
@Component(role = ParserModule.class, hint = AsciidoctorDoxiaParser.ROLE_HINT)
public class AsciidoctorDoxiaParserModule extends AbstractParserModule {

    /**
     * The source directory for AsciiDoc files.
     */
    public static final String SOURCE_DIRECTORY = AsciidoctorDoxiaParser.ROLE_HINT;

    /**
     * The extensions for AsciiDoc files.
     */
    public static final String[] FILE_EXTENSIONS = new String[]{"adoc", "asciidoc"};

    /**
     * Build a new instance of {@link AsciidoctorDoxiaParserModule}.
     */
    public AsciidoctorDoxiaParserModule() {
        super(SOURCE_DIRECTORY, AsciidoctorDoxiaParser.ROLE_HINT, FILE_EXTENSIONS);
    }
}
