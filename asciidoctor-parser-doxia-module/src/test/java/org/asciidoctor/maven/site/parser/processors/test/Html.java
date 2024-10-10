package org.asciidoctor.maven.site.parser.processors.test;

public class Html {

    public static final String LIST_STYLE_TYPE_DECIMAL = "list-style-type: decimal;";

    public static String strong(String text) {
        return htmlElement("strong", text);
    }

    public static String italics(String text) {
        return htmlElement("em", text);
    }

    public static String monospace(String text) {
        return htmlElement("code", text);
    }

    public static String ul(String... elements) {
        return htmlElement("ul", String.join("", elements));
    }

    public static String ol(String style, String... elements) {
        return htmlElement("ol", style, String.join("", elements));
    }

    public static String li(String text) {
        return htmlElement("li", text);
    }

    public static String dt(String text) {
        return htmlElement("dt", text);
    }

    public static String dd(String text) {
        return htmlElement("dd", text);
    }

    static String htmlElement(String element, String text) {
        return htmlElement(element, null, text);
    }

    static String htmlElement(String element, String style, String text) {
        if (style == null) {
            return String.format("<%1$s>%2$s</%1$s>", element, text).trim();
        }
        return String.format("<%1$s style=\"%3$s\">%2$s</%1$s>", element, text, style).trim();
    }
}
