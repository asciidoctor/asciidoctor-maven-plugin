package org.asciidoctor.maven.site.ast.processors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;
import org.asciidoctor.maven.site.ast.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(SectionNodeProcessor.class)
class SectionNodeProcessorTest {

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

    @Test
    void should_convert_section_with_sectionNumbers() {
        Attributes attributes = Attributes.builder()
                .sectionNumbers(true)
                .build();
        String content = documentWithSections();

        // With numbering
        assertThat(process(content, 1, attributes))
                .isEqualTo("<h2><a name=\"a1._First_section_title\"></a>1. First section title</h2>");
        assertThat(process(content, 2, attributes))
                .isEqualTo("<h3><a name=\"a1.1._Second_section_title\"></a>1.1. Second section title</h3>");
        assertThat(process(content, 3, attributes))
                .isEqualTo("<h4><a name=\"a1.1.1._Third_section_title\"></a>1.1.1. Third section title</h4>");

        // Without numbering by default
        assertThat(process(content, 4, attributes))
                .isEqualTo("<h5><a name=\"Fourth_section_title\"></a>Fourth section title</h5>");
        assertThat(process(content, 5, attributes))
                .isEqualTo("<h6><a name=\"Fifth_section_title\"></a>Fifth section title</h6>");
    }

    @Test
    void should_convert_section_with_sectionNumbers_and_sectNumLevels() {
        Attributes attributes = Attributes.builder()
                .sectionNumbers(true)
                .sectNumLevels(5)
                .build();
        String content = documentWithSections();

        // With numbering
        assertThat(process(content, 1, attributes))
                .isEqualTo("<h2><a name=\"a1._First_section_title\"></a>1. First section title</h2>");
        assertThat(process(content, 2, attributes))
                .isEqualTo("<h3><a name=\"a1.1._Second_section_title\"></a>1.1. Second section title</h3>");
        assertThat(process(content, 3, attributes))
                .isEqualTo("<h4><a name=\"a1.1.1._Third_section_title\"></a>1.1.1. Third section title</h4>");
        assertThat(process(content, 4, attributes))
                .isEqualTo("<h5><a name=\"a1.1.1.1._Fourth_section_title\"></a>1.1.1.1. Fourth section title</h5>");
        assertThat(process(content, 5, attributes))
                .isEqualTo("<h6><a name=\"a1.1.1.1.1._Fifth_section_title\"></a>1.1.1.1.1. Fifth section title</h6>");
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
        return process(content, level, Attributes.builder().build());
    }

    private String process(String content, int level, Attributes attributes) {
        StructuralNode node = asciidoctor.load(content, Options.builder().attributes(attributes).build())
                .findBy(Collections.singletonMap("context", ":section"))
                .stream()
                .filter(n -> n.getLevel() == level)
                .findFirst()
                .get();

        reset(sinkWriter);
        nodeProcessor.process(node);
        return sinkWriter.toString().trim();
    }

    private void reset(StringWriter sinkWriter) {
        final StringBuffer buffer = sinkWriter.getBuffer();
        buffer.setLength(0);
        buffer.trimToSize();
    }
}
