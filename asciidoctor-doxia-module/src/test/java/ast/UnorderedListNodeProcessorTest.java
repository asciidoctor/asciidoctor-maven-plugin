package ast;

import ast.test.NodeProcessorTest;
import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(UnorderedListNodeProcessor.class)
public class UnorderedListNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private Sink sink;
    private StringWriter sinkWriter;


    @BeforeEach
    void setup() {
        ListItemNodeProcessor listItemNodeProcessor = new ListItemNodeProcessor(sink);
        ((UnorderedListNodeProcessor) nodeProcessor).setItemNodeProcessor(listItemNodeProcessor);
        listItemNodeProcessor.setNodeProcessors(Arrays.asList(nodeProcessor));
    }

    @Test
    void should_convert_simple_list() {
        String content = buildDocumentWithSimpleList();

        String html = process(content);

        assertThat(html)
                .isEqualTo("<ul>\n" +
                        "<li>unordered item 1</li>\n" +
                        "<li>unordered item 2</li></ul>");
    }

    @Test
    void should_convert_nested_list() {
        String content = buildDocumentWithNestedLists();

        String html = process(content);

        assertThat(html)
                .isEqualTo("<ul>\n" +
                        "<li>unordered item 1\n" +
                        "<ul>\n" +
                        "<li>unordered item 1 1</li>\n" +
                        "<li>unordered item 1 2</li>" +
                        "</ul>" +
                        "</li>\n" +
                        "<li>unordered item 2\n" +
                        "<ul>\n" +
                        "<li>unordered item 2 1\n" +
                        "<ul>\n" +
                        "<li>unordered item 2 1 1</li></ul></li></ul>" +
                        "</li>" +
                        "</ul>");
    }

    private static String buildDocumentWithSimpleList() {
        return "= Document tile\n\n"
                + "== Section\n\n"
                + "* unordered item 1\n"
                + "* unordered item 2\n";
    }

    private static String buildDocumentWithNestedLists() {
        return "= Document tile\n\n"
                + "== Section\n\n"
                + "* unordered item 1\n"
                + "** unordered item 1 1\n"
                + "** unordered item 1 2\n"
                + "* unordered item 2\n"
                + "** unordered item 2 1\n"
                + "*** unordered item 2 1 1\n";
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":ulist"))
                .get(0);

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
