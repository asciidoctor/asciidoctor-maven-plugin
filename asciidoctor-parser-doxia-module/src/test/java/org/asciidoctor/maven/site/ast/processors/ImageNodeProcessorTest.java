package org.asciidoctor.maven.site.ast.processors;

import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;
import org.asciidoctor.maven.site.ast.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(ImageNodeProcessor.class)
class ImageNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;


    @Test
    void should_convert_document_with_image() {
        String content = documentWithImage();

        String html = process(content, 0);

        assertThat(html)
                .isEqualTo("<img src=\"images/tiger.png\" alt=\"Kitty\">");
    }

    @Test
    void should_convert_document_with_image_and_imagesdir_attribute() {
        String content = documentWithImage(Collections.singletonMap("imagesdir", "prefix-path"));

        String html = process(content, 0);

        final String separator = FileSystems.getDefault().getSeparator();
        assertThat(html)
                .isEqualTo("<img src=\"prefix-path" + separator + "images/tiger.png\" alt=\"Kitty\">");
    }

    private String documentWithImage() {
        return documentWithImage(Collections.emptyMap());
    }

    private String documentWithImage(Map<String, String> attributes) {
        return "= Document tile\n\n"
                + formatAttributes(attributes)
                + "== Section\n\n"
                + "image::images/tiger.png[Kitty]";
    }

    private static String formatAttributes(Map<String, String> attributes) {
        return attributes.entrySet().stream()
                .map(entry -> String.format(":%s: %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n", "\n", "\n"));
    }

    private String process(String content, int level) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":image"))
                .get(0);

        nodeProcessor.process(node);

        return sinkWriter.toString();
    }
}
