package org.asciidoctor.maven.site;

import java.util.Optional;

import org.apache.maven.doxia.sink.Sink;

/**
 * Injects Asciidoctor header information into Doxia's {@link Sink}.
 * This allows Doxia to build:
 * - breadcrumbs
 * - HTML head's meta elements
 *
 * @since 3.0.0
 */
public class HeadParser {

    private final Sink sink;

    public HeadParser(Sink sink) {
        this.sink = sink;
    }

    public void parse(HeaderMetadata headerMetadata) {
        sink.head();
        sink.title();
        sink.text(Optional.ofNullable(headerMetadata.getTitle()).orElse("[Untitled]"));
        sink.title_();

        for (String author : headerMetadata.getAuthors()) {
            sink.author();
            sink.text(author.toString());
            sink.author_();
        }

        sink.date();
        sink.text(headerMetadata.getDateTime());
        sink.date_();
        sink.head_();
    }
}
