package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.asciidoctor.maven.site.parser.processors.test.StringTestUtils.clean;
import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(OrderedListNodeProcessor.class)
class OrderedListNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private Sink sink;
    private StringWriter sinkWriter;


    @BeforeEach
    void setup() {
        ListItemNodeProcessor listItemNodeProcessor = new ListItemNodeProcessor(sink);
        ((OrderedListNodeProcessor) nodeProcessor).setItemNodeProcessor(listItemNodeProcessor);
        listItemNodeProcessor.setNodeProcessors(Arrays.asList(nodeProcessor));
    }

    @Test
    void should_convert_simple_list() {
        String content = buildDocumentWithSimpleList();

        String html = process(content);

        assertThat(html)
                .isEqualTo("<ol style=\"list-style-type: decimal;\">" +
                        "<li>ordered item 1</li>" +
                        "<li>ordered item 2</li></ol>");
    }

    @Test
    void should_convert_nested_list() {
        String content = buildDocumentWithNestedLists();

        String html = process(content);

        assertThat(html)
                .isEqualTo("<ol style=\"list-style-type: decimal;\">" +
                        "<li>ordered item 1" +
                        "<ol style=\"list-style-type: decimal;\">" +
                        "<li>ordered item 1 1</li></ol></li>" +
                        "<li>ordered item 1 2</li>" +
                        "<li>ordered item 2" +
                        "<ol style=\"list-style-type: decimal;\">" +
                        "<li>ordered item 2 1" +
                        "<ol style=\"list-style-type: decimal;\">" +
                        "<li>ordered item 2 1 1</li></ol></li></ol></li></ol>");
    }

    private static String buildDocumentWithSimpleList() {
        return "= Document tile\n\n"
                + "== Section\n\n"
                + ". ordered item 1\n"
                + ". ordered item 2\n";
    }

    private static String buildDocumentWithNestedLists() {
        return "= Document tile\n\n"
                + "== Section\n\n"
                + ". ordered item 1\n"
                + ".. ordered item 1 1\n"
                + ". ordered item 1 2\n"
                + ". ordered item 2\n"
                + ".. ordered item 2 1\n"
                + "... ordered item 2 1 1\n";
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":olist"))
                .get(0);

        nodeProcessor.process(node);

        return clean(sinkWriter.toString());
    }
}
