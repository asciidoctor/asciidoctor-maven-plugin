package ast;

import ast.test.NodeProcessorTest;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(DocumentNodeProcessor.class)
public class DocumentNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @Test
    void should_convert_document_title() {
        String content = "= Document tile";

        String html = process(content, 0);

        assertThat(html)
                .isEqualTo("<h1>Document tile</h1>");
    }

    @Test
    void should_convert_document_title_with_markup() {
        String content = "= *Document* _tile_";

        String html = process(content, 0);

        assertThat(html)
                .isEqualTo("<h1><strong>Document</strong> <em>tile</em></h1>");
    }

    private String process(String content, int level) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build());
        nodeProcessor.process(node);
        return sinkWriter.toString();
    }
}
