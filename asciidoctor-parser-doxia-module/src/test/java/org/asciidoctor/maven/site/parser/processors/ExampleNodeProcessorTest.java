package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.maven.site.parser.processors.test.Html.*;
import static org.asciidoctor.maven.site.parser.processors.test.StringTestUtils.removeLineBreaks;
import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(ExampleNodeProcessor.class)
class ExampleNodeProcessorTest {

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
                exampleContentDiv(p("SomeText"))));
    }

    @Test
    void should_convert_example_with_title() {
        String content = documentWithExample("SomeText", "The title", List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(exampleTitleDiv("Example 1. The title") + exampleContentDiv(p("SomeText"))));
    }

    @Test
    void should_convert_example_with_title_without_caption() {
        String content = documentWithExample("SomeText", "The title", List.of(":example-caption!:"));

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(exampleTitleDiv("The title") + exampleContentDiv(p("SomeText"))));
    }

    @Test
    void should_convert_with_nested_link() {
        final String link = "https://docs.asciidoctor.org/";
        String content = documentWithExample(link, null, List.of());

        String html = process(content);

        assertThat(html)
            .isEqualTo(div(
                exampleContentDiv(
                    p("<a href=\"https://docs.asciidoctor.org/\" class=\"bare\">https://docs.asciidoctor.org/</a>")
                )));
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
                exampleContentDiv(
                    "<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
                        tr("a", td("Header 1", Map.of("style", "text-align: left;")) + td("Header 2")) +
                        tr("b", td("Column 1, row 1", Map.of("style", "text-align: left;")) + td("Column 2, row 1")) +
                        "</table>"
                )));
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
                exampleContentDiv(ul(
                    li("Un" + ul(li("Dos"))),
                    li("Tres" + ul(li("Quatre")))
                ))));
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
                exampleContentDiv(
                    ul(
                        li("Un" + ul(li("Dos"))),
                        li("Tres" + ul(li("Quatre")))
                    ) +
                        p("<a href=\"https://docs.asciidoctor.org/\" class=\"bare\">https://docs.asciidoctor.org/</a>")
                )));
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

    @Nested
    class WithSimpleContentModel {

        @Test
        void should_convert_minimal_example() {
            String content = "= Tile\n\n" + "== Section\n\n" +
                "[example]\n" +
                "SomeText";

            String html = process(content);

            // Content is directly embedded instead of delegated to paragraph processor
            assertThat(html)
                .isEqualTo(div(exampleContentDiv("SomeText")));
        }

        @Test
        void should_convert_minimal_example_with_title() {
            String content = "= Tile\n\n" + "== Section\n\n" +
                ".Optional title\n" +
                "[example]\n" +
                "SomeText";

            String html = process(content);

            assertThat(html)
                .isEqualTo(div(exampleTitleDiv("Example 1. Optional title") + exampleContentDiv("SomeText")));
        }

        @Test
        void should_convert_minimal_example_with_link() {
            final String link = "https://docs.asciidoctor.org/";
            String content = "= Tile\n\n" + "== Section\n\n" +
                "[example]\n" +
                "SomeText, " + link;

            String html = process(content);

            assertThat(html)
                .isEqualTo(div(exampleContentDiv("SomeText, <a href=\"https://docs.asciidoctor.org/\" class=\"bare\">https://docs.asciidoctor.org/</a>")));
        }
    }

    private static String exampleTitleDiv(String text) {
        return div("color: #7a2518; margin-bottom: .25em", text);
    }

    private static String exampleContentDiv(String text) {
        return div("background: #fffef7; border-color: #e0e0dc; border: 1px solid #e6e6e6; box-shadow: 0 1px 4px #e0e0dc; margin-bottom: 1.25em; padding: 1.25em", text);
    }
}
