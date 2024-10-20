package org.asciidoctor.maven.site.parser.processors;

import org.asciidoctor.ast.StructuralNode;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;

/**
 * Utility to extract composed title and caption text.
 *
 * @author abelsromero
 * @since 3.1.0
 */
class TitleCaptionExtractor {

    // Not used in SectionNodeProcessor to avoid extra node processing
    static String getText(StructuralNode node) {
        // Caption is returned when a title is set in:
        // - Image blocks
        // - Listings
        final String caption = node.getCaption();
        return isBlank(caption) ? node.getTitle() : String.format("%s %s", caption.trim(), node.getTitle());
    }
}
