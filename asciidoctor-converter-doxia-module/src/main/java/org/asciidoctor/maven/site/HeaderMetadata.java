package org.asciidoctor.maven.site;

import java.util.List;

class HeaderMetadata {

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
