package org.asciidoctor.maven.site;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderMetadataTest {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @ParameterizedTest
    @ValueSource(strings = {
        "= Hello, AsciiDoc!",
        "# Hello, AsciiDoc!",
        "Hello, AsciiDoc!\n================"
    })
    void should_extract_title_from_header(String content) {
        Document document = document(content);

        final var headerMetadata = HeaderMetadata.from(document);

        assertThat(headerMetadata.getTitle()).isEqualTo("Hello, AsciiDoc!");
    }

    @Test
    void should_extract_title_from_attribute() {
        Options options = Options.builder()
            .safe(SafeMode.UNSAFE)
            .backend("xhtml")
            .attributes(Attributes.builder()
                .attribute("idprefix", "@")
                .attribute("showtitle", "@")
                .attribute("who", "me")
                .build())
            .build();
        Document document = document("= Hello, {who}!\n", options);

        final var headerMetadata = HeaderMetadata.from(document);

        assertThat(headerMetadata.getTitle()).isEqualTo("Hello, me!");
    }


    @ParameterizedTest
    @MethodSource("authorsProvider")
    void should_extract_author(String content, String expected) {
        Document document = document(content + "\n", defaultOptions());

        final var headerMetadata = HeaderMetadata.from(document);

        assertThat(headerMetadata.getAuthors())
            .containsExactly(expected);
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
        String content = "= Hello, AsciiDoc!";
        Options options = optionsWithAttributes(Collections.singletonMap("author", "From Attr"));
        Document document = document(content + "\n", options);

        final var headerMetadata = HeaderMetadata.from(document);

        assertThat(headerMetadata.getAuthors())
            .containsExactly("From Attr");
    }

    @Test
    void should_extract_multiple_authors() {
        String content = "= Hello, AsciiDoc!\nfirstname1 lastname2; firstname3 middlename4 lastname5";
        Document document = document(content + "\n", defaultOptions());

        final var headerMetadata = HeaderMetadata.from(document);

        assertThat(headerMetadata.getAuthors())
            .containsExactlyInAnyOrder("firstname1 lastname2", "firstname3 middlename4 lastname5");
    }

    @Test
    void should_extract_datetime_generated() {
        String content = "= Hello, AsciiDoc!";
        Document document = document(content + "\n", defaultOptions());

        final var headerMetadata = HeaderMetadata.from(document);

        assertThat(headerMetadata.getDateTime()).matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) .*");
    }

    @Test
    void should_extract_datetime_from_attribute() {
        String content = "= Hello, AsciiDoc!";
        Options options = optionsWithAttributes(Collections.singletonMap("docdatetime", "2024-11-22"));
        Document document = document(content + "\n", options);

        final var headerMetadata = HeaderMetadata.from(document);

        assertThat(headerMetadata.getDateTime()).isEqualTo("2024-11-22");
    }

    private Document document(String content) {
        return asciidoctor.load(content, defaultOptions());
    }

    private Document document(String content, Options options) {
        return asciidoctor.load(content, options);
    }

    private Options defaultOptions() {
        return Options.builder()
            .safe(SafeMode.UNSAFE)
            .backend("xhtml")
            .parseHeaderOnly(Boolean.TRUE)
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
