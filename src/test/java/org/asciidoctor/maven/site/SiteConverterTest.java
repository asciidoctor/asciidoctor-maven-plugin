package org.asciidoctor.maven.site;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.maven.site.SiteConverter.Result;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SiteConverterTest {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    void should_extract_title() {
        SiteConverter siteConverter = new SiteConverter(asciidoctor);

        Options options = Options.builder().build();
        Result result = siteConverter.process("= Hello\n\n", options);

        assertThat(result.getTitle()).isEqualTo("Hello");
        assertThat(result.getHtml()).isEqualTo("Hello");
    }

}
