package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Collections;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(PreambleNodeProcessor.class)
class PreambleNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @Test
    void should_convert_preamble() {
        String content = documentWithPreamble();

        String html = process(content);

        assertThat(html)
            .isEqualTo("<p>This is a preamble.\nWith two lines.</p>");
    }

    @Test
    void should_convert_preamble_with_markup() {
        String content = documentWithPreamble("This *is* _a_ simple `preamble`.");

        String html = process(content);

        assertThat(html)
            .isEqualTo("<p>This <strong>is</strong> <em>a</em> simple <code>preamble</code>.</p>");
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
