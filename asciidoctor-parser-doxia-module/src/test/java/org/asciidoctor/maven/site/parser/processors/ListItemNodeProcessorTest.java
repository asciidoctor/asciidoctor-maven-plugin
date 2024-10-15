package org.asciidoctor.maven.site.parser.processors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(ListItemNodeProcessor.class)
class ListItemNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @ParameterizedTest
    @ValueSource(strings = {"*", "-"})
    void should_convert_list_item(String marker) {
        String content = new DocumentBuilder()
                .listItem(marker)
                .toString();

        String html = process(content);

        assertThat(html)
                .isEqualTo(htmlListItem());
    }

    @Test
    void should_convert_ordered_list_item() {
        String content = new DocumentBuilder()
                .listItem(".")
                .toString();

        String html = process(content);

        assertThat(html)
                .isEqualTo(htmlListItem());
    }

    @ParameterizedTest
    @ValueSource(strings = {"*", "-"})
    void should_convert_ordered_list_item_with_formatting() {
        String content = new DocumentBuilder()
                .formattedListItem("*")
                .toString();

        String html = process(content);

        assertThat(html)
                .isEqualTo(htmlListItemWithFormatting());
    }

    @ParameterizedTest
    @ValueSource(strings = {"*", "-"})
    void should_convert_ordered_list_item_with_link(String marker) {
        String content = new DocumentBuilder()
                .linkListItem(marker)
                .toString();

        String html = process(content);

        assertThat(html)
                .isEqualTo(htmlListItemWithLink());
    }

    private static String htmlListItem() {
        return "<li>list item</li>";
    }

    private static String htmlListItemWithFormatting() {
        return "<li><strong>list</strong> <em>item</em></li>";
    }

    private static String htmlListItemWithLink() {
        return "<li>list <a href=\"https://something-random.org/\">item</a></li>";
    }

    class DocumentBuilder {

        private final StringBuilder sb = new StringBuilder();

        DocumentBuilder() {
            sb.append("= Document tile\n\n");
            sb.append("== Section\n\n");
        }

        DocumentBuilder listItem(String marker) {
            sb.append(marker + " list item\n");
            return this;
        }

        DocumentBuilder formattedListItem(String marker) {
            sb.append(marker + " *list* _item_\n");
            return this;
        }

        DocumentBuilder linkListItem(String marker) {
            sb.append(marker + " list https://something-random.org/[item]\n");
            return this;
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":list_item"))
                .get(0);

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
