package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.SectionImpl;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Section title processor.
 * Supports 'sectnum' and 'sectnum' attributes.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class SectionNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private final Logger logger = LoggerFactory.getLogger(SectionNodeProcessor.class);

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public SectionNodeProcessor(Sink sink) {
        super(sink);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "section".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        sectionTitle(getSink(), node.getLevel(), node.getTitle(), (Section) node);
    }

    private void sectionTitle(Sink sink, int level, String title, Section node) {
        final String formattedTitle = formatTitle(title, node);

        if (level == 0) {
            // Kept for completeness, real document title is treated in
            // DocumentNodeProcessor
            sink.sectionTitle1();
            sink.text(formattedTitle);
            sink.sectionTitle1_();
        } else {
            // Asciidoctor supports up o 6 levels, but Xhtml5BaseSink only up to 5
            int siteLevel = level + 1;
            if (level >= 5) {
                // TODO generate code manually or request change
                logger.warn("Site module does not support level 6 sections. Re-writing as 5");
                siteLevel = 5;
            }
            sink.sectionTitle(siteLevel, null);
            anchor(sink, node);
            sink.text(formattedTitle);
            sink.sectionTitle_(siteLevel);
        }
    }

    private void anchor(Sink sink, Section node) {
        sink.anchor(node.getId());
        sink.anchor_();
    }

    private String formatTitle(String title, Section node) {
        final Boolean numbered = node.isNumbered();
        final Long sectnumlevels = getSectnumlevels(node);
        final int level = node.getLevel();
        if (numbered && level <= sectnumlevels) {
            String sectnum = ((SectionImpl) node).getString("sectnum");
            return String.format("%s %s", sectnum, title);
        }
        return title;
    }

    private Long getSectnumlevels(Section node) {
        Object sectnumlevels = node.getDocument().getAttribute("sectnumlevels");

        if (sectnumlevels != null) {
            // Injecting from Maven configuration
            if (sectnumlevels instanceof String) {
                return Long.valueOf((String) sectnumlevels);
            }
            // For tests, using AttributesBuilder
            if (sectnumlevels instanceof Long) {
                return (Long) sectnumlevels;
            }
        }
        // Asciidoctor default = 3 (====)
        // https://github.com/asciidoctor/asciidoctor/blob/4bab183b9f3fad538f86071b2106f3b8185d3832/lib/asciidoctor/converter/html5.rb#L387
        return 3L;
    }
}
