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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

/**
 * Asciidoctor conversion wrapper to extract metadata and
 */
class SiteConverter {

    private static final Pattern TITLE_PATTERN = Pattern.compile("^.*<h1>([^<]*)</h1>.*$", DOTALL | CASE_INSENSITIVE);

    private final Asciidoctor asciidoctor;

    SiteConverter(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    Result process(String content, Options options) {
        Document document = asciidoctor.load(content, headerProcessingMetadata(options));

        String title = document.getTitle();
        List<String> authors = extractAuthors(document);
        String documentDateTime = extractDocumentDateTime(document, document.getAttributes());

        String html = asciidoctor.convert(content, options);
        return new Result(title, authors, documentDateTime, html);
    }

    private static Options headerProcessingMetadata(Options options) {
        Map<String, Object> optionsMap = options.map();
        OptionsBuilder builder = Options.builder();
        for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
            builder.option(entry.getKey(), entry.getValue());
        }

        builder.parseHeaderOnly(Boolean.TRUE);
        Options build = builder.build();
        return build;
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
        private final String title;
        private final List<String> author;
        private final String dateTime;
        private final String html;

        Result(String title, List<String> author, String dateTime, String html) {
            this.title = title;
            this.author = author;
            this.dateTime = dateTime;
            this.html = html;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getAuthor() {
            return author;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getHtml() {
            return html;
        }
    }
}
