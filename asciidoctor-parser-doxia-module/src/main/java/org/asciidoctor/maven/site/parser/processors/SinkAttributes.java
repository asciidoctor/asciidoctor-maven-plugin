package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;

class SinkAttributes {

    static SinkEventAttributes of(String name, String value) {
        final var attributes = new SinkEventAttributeSet();
        attributes.addAttribute(name, value);
        return attributes;
    }
}
