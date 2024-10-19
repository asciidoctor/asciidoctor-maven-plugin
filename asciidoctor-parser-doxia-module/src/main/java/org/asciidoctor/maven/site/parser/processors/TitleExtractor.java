package org.asciidoctor.maven.site.parser.processors;

import org.asciidoctor.ast.StructuralNode;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;

class TitleExtractor {

    // TODO is this being re-used properly: examples? tables?
    static String getText(StructuralNode node) {
        // Caption is returned when a title is set in:
        // - Image blocks
        // - Listings
        final String caption = node.getCaption();
        return isBlank(caption) ? node.getTitle() : String.format("%s %s", caption.trim(), node.getTitle());
    }

}
