package ast;

import ast.test.NodeProcessorTest;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(SectionNodeProcessor.class)
public class SectionNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;


    @Test
    void should_convert_document_title() {
        String content = documentWithSections();

        String html = process(content, 0);

        assertThat(html)
                .isEqualTo("<h1>Document tile</h1>");
    }

    @Test
    void should_convert_section_level_2() {
        String content = documentWithSections();

        String html = process(content, 1);

        assertThat(html)
                .isEqualTo("<h2><a name=\"First_section_title\"></a>First section title</h2>");
    }

    @Test
    void should_convert_section_level_3() {
        String content = documentWithSections();

        String html = process(content, 2);

        assertThat(html)
                .isEqualTo("<h3><a name=\"Second_section_title\"></a>Second section title</h3>");
    }

    @Test
    void should_convert_section_level_4() {
        String content = documentWithSections();

        String html = process(content, 3);

        assertThat(html)
                .isEqualTo("<h4><a name=\"Third_section_title\"></a>Third section title</h4>");
    }

    @Test
    void should_convert_section_level_5() {
        String content = documentWithSections();

        String html = process(content, 4);

        assertThat(html)
                .isEqualTo("<h5><a name=\"Fourth_section_title\"></a>Fourth section title</h5>");
    }

    @Test
    void should_convert_section_level_6() {
        String content = documentWithSections();

        String html = process(content, 5);

        assertThat(html)
                .isEqualTo("<h6><a name=\"Fifth_section_title\"></a>Fifth section title</h6>");
    }

    private String documentWithSections() {
        return "= Document tile\n\n"
                + "== First section title\n\nFirst section body\n\n"
                + "=== Second section title\n\nSecond section body\n\n"
                + "==== Third section title\n\nThird section body\n\n"
                + "===== Fourth section title\n\nFourth section body\n\n"
                + "====== Fifth section title\n\nFifth section body\n\n";
    }

    private String process(String content, int level) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":section"))
                .stream()
                .filter(n -> n.getLevel() == level)
                .findFirst()
                .get();

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
