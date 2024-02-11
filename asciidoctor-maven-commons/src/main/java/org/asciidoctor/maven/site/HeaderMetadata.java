package org.asciidoctor.maven.site;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.RevisionInfo;

/**
 * Extract required metadata from Asciidoctor to be used for maven-site pages.
 */
public class HeaderMetadata {

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

    public static HeaderMetadata from(Document document) {
        final String title = document.getTitle();
        final List<String> authors = extractAuthors(document);
        final String documentDateTime = extractDocumentDateTime(document, document.getAttributes());
        return new HeaderMetadata(title, authors, documentDateTime);
    }

    private static List<String> extractAuthors(Document document) {
        return document.getAuthors().stream()
            .map(Author::toString)
            .collect(Collectors.toList());
    }

    private static String extractDocumentDateTime(Document document, Map<String, Object> attributes) {
        final RevisionInfo revisionInfo = document.getRevisionInfo();
        return Optional.ofNullable(revisionInfo.getDate())
            .orElse((String) attributes.get("docdatetime"));
    }
}
