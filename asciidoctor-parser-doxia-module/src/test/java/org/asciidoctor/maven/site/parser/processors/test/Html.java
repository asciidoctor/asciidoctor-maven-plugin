package org.asciidoctor.maven.site.parser.processors.test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.asciidoctor.maven.site.parser.processors.test.Html.Attributes.CLASS;
import static org.asciidoctor.maven.site.parser.processors.test.Html.Attributes.STYLE;

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

    public static String div(String text) {
        return htmlElement("div", text);
    }

    public static String ul(String... elements) {
        return htmlElement("ul", String.join("", elements));
    }

    public static String ol(String style, String... elements) {
        return htmlElement("ol", String.join("", elements), Map.of(STYLE, style));
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

    public static String p(String text) {
        return htmlElement("p", text);
    }

    public static String tr(String className, String text) {
        return htmlElement("tr", text, Map.of(CLASS, className));
    }

    public static String td(String text) {
        return htmlElement("td", text, Map.of());
    }

    public static String td(String text, Map<String, String> attributes) {
        return htmlElement("td", text, attributes);
    }

    static String htmlElement(String element, String text) {
        return htmlElement(element, text, Map.of());
    }

    static String htmlElement(String element, String text, Map<String, String> attributes) {
        if (!attributes.isEmpty()) {
            String formattedAttributes = attributes.entrySet()
                .stream()
                .map(entry -> String.format("%s=\"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" "));
            return String.format("<%1$s %3$s>%2$s</%1$s>", element, text, formattedAttributes).trim();
        }

        return String.format("<%1$s>%2$s</%1$s>", element, text).trim();
    }

    public final class Attributes {
        public static final String STYLE = "style";
        public static final String CLASS = "class";
    }
}
