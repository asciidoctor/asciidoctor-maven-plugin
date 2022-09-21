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
public class RealParagraphNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @Test
    void should_convert() {

        String content = "= Tile\n\n" +
                "== Section\n\n" +
                "SomeText";

        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":paragraph")).get(0);

        nodeProcessor.process(node);

        String s = sinkWriter.toString();

        assertThat(s).isEqualTo("<p>SomeText</p>");
    }

}
