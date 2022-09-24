package ast;

import ast.test.NodeProcessorTest;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(ListingNodeProcessor.class)
public class ListingNodeProcessorTest {

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
    void should_convert_shorthand_source_block() {
        String content = documentWithShorthandSourceBlock();

        String html = process(content);

        assertThat(html)
                .isEqualTo(expectedHtmlCodeBlock());
    }

    private static String expectedHtmlCodeBlock() {
        // Actual styling is added in JS by prettify
        return "<div class=\"source\"><pre class=\"prettyprint\"><code>class HelloWorldLanguage {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}</code></pre></div>";
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
                .isEqualTo("<div><pre>class HelloWorldLanguage {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Hello, World!\");\n" +
                        "    }\n" +
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
        return "= Document tile\n\n"
                + "= Section\n\n"
                + blockDefinition + "\n" +
                "----\n" +
                "class HelloWorldLanguage {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}\n" +
                "----\n";
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":listing"))
                .get(0);

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
