package org.asciidoctor.maven.site.parser.processors.test;

public class StringTestUtils {

    /**
     * Removes linebreaks to validate to avoid OS dependant issues.
     *
     * @param value string to clean
     */
    public static String removeLineBreaks(String value) {
        return value.replaceAll("(\r)?\n", "")
                .trim();
    }
}
