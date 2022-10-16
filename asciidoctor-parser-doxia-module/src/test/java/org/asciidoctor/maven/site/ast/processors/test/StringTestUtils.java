package org.asciidoctor.maven.site.ast.processors.test;

public class StringTestUtils {

    /**
     * Removes linebreaks to validate to avoid OS dependant issues.
     *
     * @param value string to clean
     */
    public static String clean(String value) {
        return value.replaceAll("\r\n", "")
                .replaceAll("\n", "")
                .trim();
    }
}
