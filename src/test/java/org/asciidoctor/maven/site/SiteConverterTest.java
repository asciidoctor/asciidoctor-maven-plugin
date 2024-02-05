package org.asciidoctor.maven.site;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.maven.site.SiteConverter.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;

class SiteConverterTest {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @ParameterizedTest
    @ValueSource(strings = {
            "= Hello, AsciiDoc!",
            "# Hello, AsciiDoc!",
            "Hello, AsciiDoc!\n================"
    })
    void should_extract_title_from_header(String title) {
        SiteConverter siteConverter = new SiteConverter(asciidoctor);

        Options options = defaultOptions();
        Result result = siteConverter.process(title + "\n", options);

        assertThat(result.getTitle()).isEqualTo("Hello, AsciiDoc!");
        assertThat(result.getHtml()).isNotBlank();
    }

    @Test
    void should_extract_title_from_attribute() {
        SiteConverter siteConverter = new SiteConverter(asciidoctor);

        Options options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .backend("xhtml")
                .attributes(Attributes.builder()
                        .attribute("idprefix", "@")
                        .attribute("showtitle", "@")
                        .attribute("who", "me")
                        .build())
                .build();
        Result result = siteConverter.process("= Hello, {who}!\n", options);

        assertThat(result.getTitle()).isEqualTo("Hello, me!");
        assertThat(result.getHtml()).isNotBlank();
    }

    private Options defaultOptions() {
        return Options.builder()
                .safe(SafeMode.UNSAFE)
                .backend("xhtml")
                .attributes(Attributes.builder()
                        .attribute("idprefix", "@")
                        .attribute("showtitle", "@")
                        .build())
                .build();
    }
}
