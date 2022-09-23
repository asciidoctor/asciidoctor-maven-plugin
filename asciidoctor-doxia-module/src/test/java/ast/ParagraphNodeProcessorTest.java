package ast;

import ast.test.NodeProcessorTest;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(ParagraphNodeProcessor.class)
public class ParagraphNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @Test
    void should_convert_minimal_paragraph() {
        String content = documentWithParagraph("SomeText");

        String html = process(content);

        assertThat(html)
                .isEqualTo("<p>SomeText</p>");
    }

    @Test
    void should_convert_paragraph_with_bold_markup() {
        String content = documentWithParagraph("Some *text*");

        String html = process(content);

        assertThat(html)
                .isEqualTo("<p>Some <strong>text</strong></p>");
    }

    @Test
    void should_convert_paragraph_with_italics_markup() {
        String content = documentWithParagraph("Some _text_");

        String html = process(content);

        assertThat(html)
                .isEqualTo("<p>Some <em>text</em></p>");
    }

    @Test
    void should_convert_paragraph_with_monospace_markup() {
        String content = documentWithParagraph("Some `text`");

        String html = process(content);

        assertThat(html)
                .isEqualTo("<p>Some <code>text</code></p>");
    }

    private String documentWithParagraph(String text) {
        return "= Tile\n\n" + "== Section\n\n" + text;
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":paragraph"))
                .get(0);

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
