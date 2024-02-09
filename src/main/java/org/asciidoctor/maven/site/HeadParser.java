package org.asciidoctor.maven.site;

import org.apache.maven.doxia.sink.Sink;

import java.util.Optional;

class HeadParser {

    private final Sink sink;

    HeadParser(Sink sink) {
        this.sink = sink;
    }

    void parse(SiteConverterDecorator.HeaderMetadata headerMetadata) {
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
