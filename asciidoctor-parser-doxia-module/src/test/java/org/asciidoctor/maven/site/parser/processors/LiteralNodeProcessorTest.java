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

@NodeProcessorTest(LiteralNodeProcessor.class)
class LiteralNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;


    @Test
    void should_convert_simple_literal() {
        String content = documentWithLiteralBlock();

        String html = process(content, 0);

        assertThat(html)
                .isEqualTo("<div><pre>This is a literal line.</pre></div>");
    }

    private String documentWithLiteralBlock() {
        return "= Document tile\n\n"
                + "== Section\n\n This is a literal line.\n";
    }

    private String process(String content, int level) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":literal"))
                .get(0);

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
