package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.BlockImpl;
import org.asciidoctor.maven.site.parser.NodeProcessor;

/**
 * Literal (aka. monospace) text processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class LiteralNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private final ListItemNodeProcessor itemNodeProcessor;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public LiteralNodeProcessor(Sink sink) {
        super(sink);
        this.itemNodeProcessor = new ListItemNodeProcessor(sink);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "literal".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        final StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("<div><pre>");

        contentBuilder.append(((BlockImpl) node).getSource());

        contentBuilder.append("</pre></div>");
        getSink().rawText(contentBuilder.toString());
    }
}
