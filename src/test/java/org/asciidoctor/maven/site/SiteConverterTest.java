package org.asciidoctor.maven.site;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.maven.site.SiteConverter.HeaderMetadata;
import org.asciidoctor.maven.site.SiteConverter.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

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

        HeaderMetadata headerMetadata = result.getHeaderMetadata();
        assertThat(headerMetadata.getTitle()).isEqualTo("Hello, AsciiDoc!");
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

        HeaderMetadata headerMetadata = result.getHeaderMetadata();
        assertThat(headerMetadata.getTitle()).isEqualTo("Hello, me!");
        assertThat(result.getHtml()).isNotBlank();
    }


    @ParameterizedTest
    @MethodSource("authorsProvider")
    void should_extract_author(String content, String expected) {
        SiteConverter siteConverter = new SiteConverter(asciidoctor);

        Options options = defaultOptions();
        Result result = siteConverter.process(content + "\n", options);

        HeaderMetadata headerMetadata = result.getHeaderMetadata();
        assertThat(headerMetadata.getAuthors())
                .containsExactly(expected);
        assertThat(result.getHtml()).isNotBlank();
    }

    private static Stream<Arguments> authorsProvider() {
        return Stream.of(
                Arguments.of("= Title\nAuthor YesMe", "Author YesMe"),
                Arguments.of("= Title\nAuthor Romero_Encinas", "Author Romero Encinas"),
                Arguments.of("= Title\n:author: Author Romero_Encinas", "Author Romero Encinas"),
                Arguments.of("= Title\nAuthor_Me <mail@mail.com>", "Author Me <mail@mail.com>")
        );
    }

    @Test
    void should_extract_author_from_attribute() {
        SiteConverter siteConverter = new SiteConverter(asciidoctor);

        String content = "= Hello, AsciiDoc!";
        Options options = optionsWithAttributes(Collections.singletonMap("author", "From Attr"));
        Result result = siteConverter.process(content + "\n", options);

        HeaderMetadata headerMetadata = result.getHeaderMetadata();
        assertThat(headerMetadata.getAuthors())
                .containsExactly("From Attr");
        assertThat(result.getHtml()).isNotBlank();
    }

    @Test
    void should_extract_multiple_authors() {
        SiteConverter siteConverter = new SiteConverter(asciidoctor);

        String content = "= Hello, AsciiDoc!\nfirstname1 lastname2; firstname3 middlename4 lastname5";
        Result result = siteConverter.process(content + "\n", defaultOptions());

        HeaderMetadata headerMetadata = result.getHeaderMetadata();
        assertThat(headerMetadata.getAuthors())
                .containsExactlyInAnyOrder("firstname1 lastname2", "firstname3 middlename4 lastname5");
        assertThat(result.getHtml()).isNotBlank();
    }

    @Test
    void should_extract_datetime_generated() {
        SiteConverter siteConverter = new SiteConverter(asciidoctor);

        String content = "= Hello, AsciiDoc!";
        Result result = siteConverter.process(content + "\n", defaultOptions());

        HeaderMetadata headerMetadata = result.getHeaderMetadata();
        assertThat(headerMetadata.getDateTime()).matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) ([+-]\\d{4})");
        assertThat(result.getHtml()).isNotBlank();
    }

    @Test
    void should_extract_datetime_from_attribute() {
        SiteConverter siteConverter = new SiteConverter(asciidoctor);

        String content = "= Hello, AsciiDoc!";
        Options options = optionsWithAttributes(Collections.singletonMap("docdatetime", "2024-11-22"));
        Result result = siteConverter.process(content + "\n", options);

        HeaderMetadata headerMetadata = result.getHeaderMetadata();
        assertThat(headerMetadata.getDateTime()).isEqualTo("2024-11-22");
        assertThat(result.getHtml()).isNotBlank();
    }

    private Options defaultOptions() {
        return Options.builder()
                .safe(SafeMode.UNSAFE)
                .backend("xhtml")
                .attributes(defaultAttributes())
                .build();
    }

    private static Attributes defaultAttributes() {
        return Attributes.builder()
                .attribute("idprefix", "@")
                .attribute("showtitle", "@")
                .build();
    }

    private Options optionsWithAttributes(Map<String, String> attributes) {
        Options options = defaultOptions();
        Attributes attr = defaultAttributes();
        attributes.forEach((k, v) -> attr.setAttribute(k, v));
        options.setAttributes(attr);
        return options;
    }
}
