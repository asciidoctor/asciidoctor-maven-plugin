package org.asciidoctor.maven.site.parser.processors;

import java.util.stream.Collectors;
import java.util.stream.Stream;

class Styles {

    public static final String CAPTION = Stream.of(
        "color: #7a2518",
        "margin-bottom: .25em"
    ).collect(Collectors.joining("; "));


    public static final String EXAMPLE = Stream.of(
        "background: #fffef7",
        "border-color: #e0e0dc",
        "border: 1px solid #e6e6e6",
        "box-shadow: 0 1px 4px #e0e0dc",
        "padding: 1.25em"
    ).collect(Collectors.joining("; "));
}
