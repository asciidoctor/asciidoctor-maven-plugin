package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Collections;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;
import static org.asciidoctor.maven.site.parser.processors.test.StringTestUtils.removeLineBreaks;
import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(ListingNodeProcessor.class)
class ListingNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @Test
    void should_convert_full_source_block() {
        String content = documentWithFullSourceBlock();

        String html = process(content);

        assertThat(html)
            .isEqualTo(expectedHtmlCodeBlock());
    }

    @Test
    void should_convert_full_source_block_with_caption() {
        final String title = "Java example";
        String content = buildDocument(title, "[source,java]");

        String html = process(content);

        assertThat(html)
            .isEqualTo(expectedHtmlCodeBlock(title));
    }

    @Test
    void should_convert_shorthand_source_block() {
        String content = documentWithShorthandSourceBlock();

        String html = process(content);

        assertThat(html)
            .isEqualTo(expectedHtmlCodeBlock());
    }

    @Test
    void should_convert_full_source_block_with_line_numbers_attribute() {
        String content = buildDocument("[source,java,linenums]");

        String html = process(content);

        assertThat(html)
            .startsWith("<div class=\"source\"><pre class=\"prettyprint linenums\"><code>");
    }

    @Test
    void should_convert_full_source_block_with_line_numbers_option() {
        String content = buildDocument("[source%linenums,java]");

        String html = process(content);

        assertThat(html)
            .startsWith("<div class=\"source\"><pre class=\"prettyprint linenums\"><code>");
    }

    @Test
    void should_convert_listing_style_block() {
        String content = documentWithListingStyleSourceBlock();

        String html = process(content);

        assertThat(html)
            .isEqualTo("<div><pre>class HelloWorldLanguage {" +
                "    public static void main(String[] args) {" +
                "        System.out.println(\"Hello, World!\");" +
                "    }" +
                "}</pre></div>");
    }

    private String documentWithFullSourceBlock() {
        return buildDocument("[source,java]");
    }

    private String documentWithShorthandSourceBlock() {
        return buildDocument("[,java]");
    }

    private String documentWithListingStyleSourceBlock() {
        return buildDocument("");
    }

    private static String buildDocument(String blockDefinition) {
        return buildDocument(null, blockDefinition);
    }

    private static String buildDocument(String title, String blockDefinition) {
        return "= Document tile\n\n"
            + "== Section\n\n"
            + buildTitle(title)
            + blockDefinition + "\n" +
            "----\n" +
            "class HelloWorldLanguage {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello, World!\");\n" +
            "    }\n" +
            "}\n" +
            "----\n";
    }

    private static String buildTitle(String title) {
        if (isNotBlank(title))
            return "." + title + "\n";
        return "";
    }

    private static String expectedHtmlCodeBlock() {
        return expectedHtmlCodeBlock(null);
    }

    private static String expectedHtmlCodeBlock(String title) {
        // Actual styling is added in JS by prettify
        return "<div class=\"source\">" +
            (isNotBlank(title) ? expectedTitle(title) : "") +
            "<pre class=\"prettyprint\"><code>class HelloWorldLanguage {" +
            "    public static void main(String[] args) {" +
            "        System.out.println(\"Hello, World!\");" +
            "    }" +
            "}</code></pre></div>";
    }

    private static String expectedTitle(String title) {
        return "<div style=\"color: #7a2518; margin-bottom: .25em\" >" + title + "</div>";
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
            .findBy(Collections.singletonMap("context", ":listing"))
            .get(0);

        nodeProcessor.process(node);

        return removeLineBreaks(sinkWriter.toString());
    }
}
