package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Collections;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.maven.site.parser.processors.test.Html.p;
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
            .isEqualTo(p("This is a preamble." + System.lineSeparator() + "With two lines."));
    }

    @Test
    void should_convert_preamble_with_markup() {
        String content = documentWithPreamble("This *is* _a_ simple `preamble`.");

        String html = process(content);

        assertThat(html)
            .isEqualTo(p("This <strong>is</strong> <em>a</em> simple <code>preamble</code>."));
    }

    @Test
    void should_convert_preamble_with_link() {
        final String link = "https://docs.asciidoctor.org/";
        String content = documentWithPreamble("There's link " + link + " in the preamble.");

        String html = process(content);

        assertThat(html)
            .isEqualTo(p("There&#8217;s link <a href=\"https://docs.asciidoctor.org/\" class=\"bare\">https://docs.asciidoctor.org/</a> in the preamble."));
    }

    @Test
    void should_convert_preamble_with_inline_image() {
        final String inlineImage = "image:images/tiger.png[Kitty]";
        String content = documentWithPreamble("An inline image " + inlineImage + " here!");

        String html = process(content);

        assertThat(html)
            .isEqualTo(p("An inline image <span class=\"image\"><img src=\"images/tiger.png\" alt=\"Kitty\"></span> here!"));
    }

    private String documentWithPreamble() {
        return documentWithPreamble("This is a preamble." +
            System.lineSeparator() +
            "With two lines.");
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
