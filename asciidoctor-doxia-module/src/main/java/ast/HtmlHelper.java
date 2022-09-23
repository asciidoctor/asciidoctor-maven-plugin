package ast;

public class HtmlHelper {

    static String h1(String text) {
        return tag("h1", text);
    }

    private static String tag(String tag, String text) {
        return String.format("<%1$s>%2$s</%1$s>", tag, text);
    }
}
