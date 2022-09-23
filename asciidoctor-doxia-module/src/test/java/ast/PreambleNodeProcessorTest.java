package ast;

import ast.test.NodeProcessorTest;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(PreambleNodeProcessor.class)
public class PreambleNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @Test
    void should_convert_preamble() {
        String content = documentWithPreamble();

        String html = process(content);

        assertThat(html)
                .isEqualTo("<div class=\"paragraph\">\n" +
                        "<p>This is a preamble.\n" +
                        "With two lines.</p>\n" +
                        "</div>");
    }

    @Test
    void should_convert_preamble_with_markup() {
        String content = documentWithPreamble("This *is* _a_ simple `preamble`.");

        String html = process(content);

        assertThat(html)
                .isEqualTo("<div class=\"paragraph\">\n" +
                        "<p>This <strong>is</strong> <em>a</em> simple <code>preamble</code>.</p>\n" +
                        "</div>");
    }

    private String documentWithPreamble() {
        return documentWithPreamble("This is a preamble.\nWith two lines.");
    }

    private String documentWithPreamble(String text) {
        return "= Document tile\n\n"
                + text + "\n\n"
                + "== Section\n\nSection body\n\n";
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":preamble"))
                .get(0);

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
