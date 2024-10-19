package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.maven.site.parser.processors.test.Html.*;
import static org.asciidoctor.maven.site.parser.processors.test.StringTestUtils.removeLineBreaks;
import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(ExampleNodeProcessor.class)
class ExampleNodeProcessorTest {

    public static final String EXAMPLE_TITLE_OPENING = "<div style=\"color: #7a2518; margin-bottom: .25em\">";
    public static final String EXAMPLE_CONTENT_OPENING = "<div style=\"background: #fffef7; border-color: #e0e0dc; border: 1px solid #e6e6e6; box-shadow: 0 1px 4px #e0e0dc; padding: 1.25em\">";

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @Test
    void should_convert_empty_example() {
        String content = documentWithExample(null, null, List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(""));
    }

    @Test
    void should_convert_minimal_example() {
        String content = documentWithExample("SomeText", null, List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(
                EXAMPLE_CONTENT_OPENING +
                    p("SomeText") +
                    "</div>"));
    }

    @Test
    void should_convert_example_with_title() {
        String content = documentWithExample("SomeText", "The title", List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(
                EXAMPLE_TITLE_OPENING + "Example 1. The title</div>" +
                    EXAMPLE_CONTENT_OPENING +
                    p("SomeText") +
                    "</div>"));
    }

    @Test
    void should_convert_example_with_title_without_caption() {
        String content = documentWithExample("SomeText", "The title", List.of(":example-caption!:"));

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(
                EXAMPLE_TITLE_OPENING + "The title</div>" +
                    EXAMPLE_CONTENT_OPENING +
                    p("SomeText") +
                    "</div>"));
    }

    @Test
    void should_convert_with_nested_link() {
        final String link = "https://docs.asciidoctor.org/";
        String content = documentWithExample(link, null, List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(
                EXAMPLE_CONTENT_OPENING +
                    p("<a href=\"https://docs.asciidoctor.org/\" class=\"bare\">https://docs.asciidoctor.org/</a>") +
                    "</div>"));
    }

    @Test
    void should_convert_with_nested_table() {
        final String table = "|===\n" +
            "|Header 1 |Header 2\n" +
            "|Column 1, row 1\n" +
            "|Column 2, row 1\n" +
            "|===";
        String content = documentWithExample(table, null, List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(
                EXAMPLE_CONTENT_OPENING +
                    "<table class=\"bodyTable\">" +
                    "<tr class=\"a\"><td style=\"text-align: left;\">Header 1</td><td>Header 2</td></tr>" +
                    "<tr class=\"b\"><td style=\"text-align: left;\">Column 1, row 1</td><td>Column 2, row 1</td></tr>" +
                    "</table>" +
                    "</div>"));
    }

    @Test
    void should_convert_with_nested_list() {
        final String list = "* Un\n" +
            "** Dos\n" +
            "* Tres\n" +
            "** Quatre";
        String content = documentWithExample(list, null, List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(
                EXAMPLE_CONTENT_OPENING +
                    ul(
                        li("Un" + ul(li("Dos"))) +
                            li("Tres" + ul(li("Quatre")))
                    ) +
                    "</div>"));
    }

    @Test
    void should_convert_with_multiple_nested_elements() {
        final String list = "* Un\n" +
            "** Dos\n" +
            "* Tres\n" +
            "** Quatre";
        final String link = "https://docs.asciidoctor.org/";
        String content = documentWithExample(list + "\n\n" + link, null, List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(
                EXAMPLE_CONTENT_OPENING +
                    ul(
                        li("Un" + ul(li("Dos"))) +
                            li("Tres" + ul(li("Quatre")))
                    ) +
                    p("<a href=\"https://docs.asciidoctor.org/\" class=\"bare\">https://docs.asciidoctor.org/</a>") +
                    "</div>"));
    }

    private String documentWithExample(String text, String title, List<String> attributes) {
        return "= Tile\n\n" + "== Section\n\n" +
            (attributes.isEmpty() ? "" : attributes.stream().collect(Collectors.joining("\n"))) + "\n" +
            (title == null ? "" : "." + title) + "\n" +
            "====" + "\n" +
            (text == null ? "" : text) + "\n" +
            "====" + "\n";
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
            .findBy(Collections.singletonMap("context", ":example"))
            .get(0);

        nodeProcessor.process(node);

        return removeLineBreaks(sinkWriter.toString());
    }
}
