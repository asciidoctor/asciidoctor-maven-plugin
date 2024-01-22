package org.asciidoctor.maven.commons;

/**
 * String utils method.
 *
 * @author abelsromero
 */
public class StringUtils {

    private StringUtils() {
    }

    /**
     * Whether a {@link CharSequence} it blank (null, empty or white characters).
     *
     * @param cs character sequence
     * @return {@literal true} if input is blank
     */
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

    /**
     * Whether a {@link CharSequence} it not blank (null, empty or white characters).
     *
     * @param cs character sequence
     * @return {@literal true} if input is not blank
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }
}
