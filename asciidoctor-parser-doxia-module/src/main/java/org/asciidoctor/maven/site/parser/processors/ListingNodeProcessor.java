package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.BlockImpl;
import org.asciidoctor.maven.commons.StringUtils;
import org.asciidoctor.maven.site.parser.NodeProcessor;

import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * Processes code blocks.
 * Create a block compatible with maven-fluido-skin's use code-prettify.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class ListingNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private static final String FLUIDO_SKIN_SOURCE_HIGHLIGHTER = "prettyprint";
    private static final String LINENUMS_ATTRIBUTE = "linenums";
    private static final String LINENUMS_OPTION_ATTRIBUTE = LINENUMS_ATTRIBUTE + "-option";

    private final ListItemNodeProcessor itemNodeProcessor;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public ListingNodeProcessor(Sink sink) {
        super(sink);
        this.itemNodeProcessor = new ListItemNodeProcessor(sink);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "listing".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        final StringBuilder contentBuilder = new StringBuilder();
        String language = (String) node.getAttribute("language");
        String style = node.getStyle();

        boolean isSourceBlock = isSourceBlock(language, style);

        if (isSourceBlock) {
            // source class triggers prettify auto-detection
            contentBuilder.append("<div class=\"source\">");

            final String title = node.getTitle();
            if (StringUtils.isNotBlank(title)) {
                contentBuilder.append("<div style=\"color: #7a2518; margin-bottom: .25em;\" >" + title + "</div>");
            }

            contentBuilder.append("<pre class=\"")
                    .append(FLUIDO_SKIN_SOURCE_HIGHLIGHTER);
            if (isLinenumsEnabled(node))
                contentBuilder.append(" linenums");

            contentBuilder.append("\">");
            contentBuilder.append("<code>");
        } else {
            contentBuilder.append("<div>");
            contentBuilder.append("<pre>");
        }

        contentBuilder.append(((BlockImpl) node).getSource());

        if (isSourceBlock) {
            contentBuilder.append("</code>");
        }

        contentBuilder.append("</pre></div>");
        getSink().rawText(contentBuilder.toString());
    }

    private boolean isLinenumsEnabled(StructuralNode node) {
        // linenums attribute can be set with empty string value
        return LINENUMS_ATTRIBUTE.equals(node.getAttribute("linenums"))
                || node.getAttribute(LINENUMS_OPTION_ATTRIBUTE) != null;
    }

    private boolean isSourceBlock(String language, String style) {
        return isNotBlank(language) || "source".equals(style);
    }
}
