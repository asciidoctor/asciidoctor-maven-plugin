package org.asciidoctor.maven.site;

import javax.inject.Named;

import org.apache.maven.doxia.parser.module.AbstractParserModule;

/**
 * This class is the entry point for integration with the Maven Site Plugin
 * integration since Doxia 1.6 (i.e., maven-site-plugin 3.4 and above):
 * it defines source directory and file extensions to be added to
 * <a href="https://maven.apache.org/doxia/references/">Doxia provided modules</a>.
 *
 * @author jdlee
 */
@Named(AsciidoctorConverterDoxiaParser.ROLE_HINT)
public class AsciidoctorConverterDoxiaParserModule extends AbstractParserModule {

    /**
     * The source directory for AsciiDoc files.
     */
    public static final String SOURCE_DIRECTORY = AsciidoctorConverterDoxiaParser.ROLE_HINT;

    /**
     * The extensions for AsciiDoc files.
     */
    public static final String[] FILE_EXTENSIONS = new String[]{"adoc", "asciidoc"};

    /**
     * Build a new instance of {@link AsciidoctorConverterDoxiaParserModule}.
     */
    public AsciidoctorConverterDoxiaParserModule() {
        super(SOURCE_DIRECTORY, AsciidoctorConverterDoxiaParser.ROLE_HINT, FILE_EXTENSIONS);
    }
}
