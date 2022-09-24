package ast;

import ast.test.NodeProcessorTest;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(ListItemNodeProcessor.class)
public class ListItemNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;


    @ParameterizedTest
    @ValueSource(strings = {"*", "-"})
    void should_convert_list_item(String marker) {
        String content = buildDocument(marker);

        String html = process(content);

        assertThat(html)
                .isEqualTo(htmlListItem());
    }

    @Test
    void should_convert_ordered_list_item() {
        String content = buildDocument(".");

        String html = process(content);

        assertThat(html)
                .isEqualTo(htmlListItem());
    }

    private static String htmlListItem() {
        return "<li>unordered item</li>";
    }

    private static String buildDocument(String marker) {
        return "= Document tile\n\n"
                + "= Section\n\n"
                + marker + " unordered item\n";
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":list_item"))
                .get(0);

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
