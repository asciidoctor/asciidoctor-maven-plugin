package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Collections;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.maven.site.parser.processors.test.Html.removeLineBreaks;
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
            .isEqualTo("<div><h1>Document tile</h1></div>");
    }

    @Test
    void should_convert_section_level_2() {
        String content = documentWithSections();

        String html = process(content, 1);

        assertThat(html)
            .isEqualTo("<div>" +
                "<h2><a id=\"_first_section_title\"></a>First section title</h2>" +
                "<p>First section body</p>" +
                "<div>" +
                "<h3><a id=\"_second_section_title\"></a>Second section title</h3>" +
                "<p>Second section body</p>" +
                "<div>" +
                "<h4><a id=\"_third_section_title\"></a>Third section title</h4>" +
                "<p>Third section body</p>" +
                "<div>" +
                "<h5><a id=\"_fourth_section_title\"></a>Fourth section title</h5>" +
                "<p>Fourth section body</p>" +
                "<div>" +
                "<h5><a id=\"_fifth_section_title\"></a>Fifth section title</h5>" +
                "<p>Fifth section body</p></div></div></div></div></div>");
    }

    @Test
    void should_convert_section_level_3() {
        String content = documentWithSections();

        String html = process(content, 2);

        assertThat(html)
            .isEqualTo("<div>" +
                "<h3><a id=\"_second_section_title\"></a>Second section title</h3>" +
                "<p>Second section body</p>" +
                "<div>" +
                "<h4><a id=\"_third_section_title\"></a>Third section title</h4>" +
                "<p>Third section body</p>" +
                "<div>" +
                "<h5><a id=\"_fourth_section_title\"></a>Fourth section title</h5>" +
                "<p>Fourth section body</p>" +
                "<div>" +
                "<h5><a id=\"_fifth_section_title\"></a>Fifth section title</h5>" +
                "<p>Fifth section body</p></div></div></div></div>");
    }

    @Test
    void should_convert_section_level_4() {
        String content = documentWithSections();

        String html = process(content, 3);

        assertThat(html)
            .isEqualTo("<div>" +
                "<h4><a id=\"_third_section_title\"></a>Third section title</h4>" +
                "<p>Third section body</p>" +
                "<div>" +
                "<h5><a id=\"_fourth_section_title\"></a>Fourth section title</h5>" +
                "<p>Fourth section body</p>" +
                "<div>" +
                "<h5><a id=\"_fifth_section_title\"></a>Fifth section title</h5>" +
                "<p>Fifth section body</p></div></div></div>");
    }

    @Test
    void should_convert_section_level_5() {
        String content = documentWithSections();

        String html = process(content, 4);

        assertThat(html)
            .isEqualTo("<div>" +
                "<h5><a id=\"_fourth_section_title\"></a>Fourth section title</h5>" +
                "<p>Fourth section body</p>" +
                "<div>" +
                "<h5><a id=\"_fifth_section_title\"></a>Fifth section title</h5>" +
                "<p>Fifth section body</p></div></div>");
    }

    @Test
    void should_convert_section_level_6() {
        String content = documentWithSections();

        String html = process(content, 5);

        assertThat(html)
            .isEqualTo("<div>" +
                "<h5><a id=\"_fifth_section_title\"></a>Fifth section title</h5>" +
                "<p>Fifth section body</p></div>");
    }

    @Test
    void should_convert_section_with_sectionNumbers() {
        Attributes attributes = Attributes.builder()
            .sectionNumbers(true)
            .build();
        String content = documentWithSections();

        // With numbering
        assertThat(process(content, 1, attributes))
            .isEqualTo("<div>" +
                "<h2><a id=\"_first_section_title\"></a>1. First section title</h2>" +
                "<p>First section body</p>" +
                "<div>" +
                "<h3><a id=\"_second_section_title\"></a>1.1. Second section title</h3>" +
                "<p>Second section body</p>" +
                "<div>" +
                "<h4><a id=\"_third_section_title\"></a>1.1.1. Third section title</h4>" +
                "<p>Third section body</p>" +
                "<div>" +
                "<h5><a id=\"_fourth_section_title\"></a>Fourth section title</h5>" +
                "<p>Fourth section body</p>" +
                "<div>" +
                "<h5><a id=\"_fifth_section_title\"></a>Fifth section title</h5>" +
                "<p>Fifth section body</p></div></div></div></div></div>");
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
            .isEqualTo("<div>" +
                "<h2><a id=\"_first_section_title\"></a>1. First section title</h2>" +
                "<p>First section body</p>" +
                "<div>" +
                "<h3><a id=\"_second_section_title\"></a>1.1. Second section title</h3>" +
                "<p>Second section body</p>" +
                "<div>" +
                "<h4><a id=\"_third_section_title\"></a>1.1.1. Third section title</h4>" +
                "<p>Third section body</p>" +
                "<div>" +
                "<h5><a id=\"_fourth_section_title\"></a>1.1.1.1. Fourth section title</h5>" +
                "<p>Fourth section body</p>" +
                "<div>" +
                "<h5><a id=\"_fifth_section_title\"></a>1.1.1.1.1. Fifth section title</h5>" +
                "<p>Fifth section body</p></div></div></div></div></div>");
    }

    private String documentWithSections() {
        return "= Document tile\n\n"
            + "== First section title\n\nFirst section body\n\n"
            + "=== Second section title\n\nSecond section body\n\n"
            + "==== Third section title\n\nThird section body\n\n"
            + "===== Fourth section title\n\nFourth section body\n\n"
            + "====== Fifth section title\n\nFifth section body\n\n"
            + "== First section title\n\nFirst section body\n\n"
            + "=== Second section title\n\nSecond section body\n\n"
            ;
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
        return removeLineBreaks(sinkWriter.toString().trim());
    }

    private void reset(StringWriter sinkWriter) {
        final StringBuffer buffer = sinkWriter.getBuffer();
        buffer.setLength(0);
        buffer.trimToSize();
    }
}
