package org.asciidoctor.maven.site;

import java.util.Optional;

import org.apache.maven.doxia.sink.Sink;

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
