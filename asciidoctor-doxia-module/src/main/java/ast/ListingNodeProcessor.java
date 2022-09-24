package ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.BlockImpl;

import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * Processes code blocks.
 * Create a block compatible with maven-fluido-skin's use code-prettify.
 */
public class ListingNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private static final String FLUIDO_SKIN_SOURCE_HIGHLIGHTER = "prettyprint";
    private static final String LINENUMS_ATTRIBUTE = "linenums";
    private static final String LINENUMS_OPTION_ATTRIBUTE = LINENUMS_ATTRIBUTE + "-option";

    private final ListItemNodeProcessor itemNodeProcessor;

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
        String style = (String) node.getAttribute("style");

        boolean isSourceBlock = isSourceBlock(language, style);

        if (isSourceBlock) {
            // source class triggers prettify auto-detection
            contentBuilder.append("<div class=\"source\">");

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

    // LINENUMS_OPTION_ATTRIBUTE is set with empty string value
    private boolean isLinenumsEnabled(StructuralNode node) {
        return LINENUMS_ATTRIBUTE.equals(node.getAttribute("linenums"))
                || node.getAttribute(LINENUMS_OPTION_ATTRIBUTE) != null;
    }

    private boolean isSourceBlock(String language, String style) {
        return isNotBlank(language) || "source".equals(style);
    }
}
