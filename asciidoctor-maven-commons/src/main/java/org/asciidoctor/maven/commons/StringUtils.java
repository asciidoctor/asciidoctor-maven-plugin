package org.asciidoctor.maven.commons;

/**
 * String utils method.
 *
 * @author abelsromero
 */
public class StringUtils {

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; i++) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
