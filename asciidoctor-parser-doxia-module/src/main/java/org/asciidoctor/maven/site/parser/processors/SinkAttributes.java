package org.asciidoctor.maven.site.parser.processors;

import javax.swing.text.html.HTML.Attribute;

import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;

class SinkAttributes {

    static SinkEventAttributes of(Attribute name, String value) {
        final var attributes = new SinkEventAttributeSet();
        attributes.addAttribute(name, value);
        return attributes;
    }

}
