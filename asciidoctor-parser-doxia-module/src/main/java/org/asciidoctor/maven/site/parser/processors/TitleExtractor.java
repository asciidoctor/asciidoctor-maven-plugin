package org.asciidoctor.maven.site.parser.processors;

import org.asciidoctor.ast.StructuralNode;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;

class TitleExtractor {

    static String getText(StructuralNode node) {
        // Caption is returned when a title is set in:
        // - Listings
        // - Image blocks
        final String caption = node.getCaption();
        return isBlank(caption) ? node.getTitle() : caption + node.getTitle();
    }

}
