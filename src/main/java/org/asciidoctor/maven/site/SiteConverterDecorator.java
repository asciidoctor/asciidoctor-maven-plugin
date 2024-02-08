package org.asciidoctor.maven.site;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.RevisionInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Asciidoctor conversion wrapper for maven-site integration.
 * In addition to conversion, handles header metadata extraction.
 */
class SiteConverterDecorator {

    private final Asciidoctor asciidoctor;

    SiteConverterDecorator(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    Result process(String content, Options options) {
        Document document = asciidoctor.load(content, headerProcessingMetadata(options));

        String title = document.getTitle();
        List<String> authors = extractAuthors(document);
        String documentDateTime = extractDocumentDateTime(document, document.getAttributes());

        String html = asciidoctor.convert(content, options);
        return new Result(new HeaderMetadata(title, authors, documentDateTime), html);
    }

    private static Options headerProcessingMetadata(Options options) {
        Map<String, Object> optionsMap = options.map();
        OptionsBuilder builder = Options.builder();
        for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
            builder.option(entry.getKey(), entry.getValue());
        }

        builder.parseHeaderOnly(Boolean.TRUE);
        return builder.build();
    }

    private List<String> extractAuthors(Document document) {
        return document.getAuthors().stream()
                .map(Author::toString)
                .collect(Collectors.toList());
    }

    private String extractDocumentDateTime(Document document, Map<String, Object> attributes) {
        final RevisionInfo revisionInfo = document.getRevisionInfo();
        return Optional.ofNullable(revisionInfo.getDate())
                .orElse((String) attributes.get("docdatetime"));
    }

    final class Result {

        private final HeaderMetadata headerMetadata;
        private final String html;

        Result(HeaderMetadata headerMetadata, String html) {
            this.headerMetadata = headerMetadata;
            this.html = html;
        }

        HeaderMetadata getHeaderMetadata() {
            return headerMetadata;
        }

        String getHtml() {
            return html;
        }
    }

    final class HeaderMetadata {

        private final String title;
        private final List<String> authors;
        private final String dateTime;

        HeaderMetadata(String title, List<String> authors, String dateTime) {
            this.title = title;
            this.authors = authors;
            this.dateTime = dateTime;
        }

        String getTitle() {
            return title;
        }

        List<String> getAuthors() {
            return authors;
        }

        String getDateTime() {
            return dateTime;
        }
    }
}
