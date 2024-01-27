package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

import java.util.List;

/**
 * List items processor, including numbered and unnumbered.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class ListItemNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private List<NodeProcessor> nodeProcessors;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public ListItemNodeProcessor(Sink sink) {
        super(sink);

    }

    /**
     * Inject a list of {@link NodeProcessor}.
     *
     * @param nodeProcessors {@link List} of {@link NodeProcessor}
     */
    public void setNodeProcessors(List<NodeProcessor> nodeProcessors) {
        this.nodeProcessors = nodeProcessors;
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "list_item".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        final ListItem item = (ListItem) node;
        final Sink sink = getSink();
        if (isUnorderedListItem(item))
            sink.listItem();
        else
            sink.numberedListItem();

        sink.rawText(item.getText());

        for (StructuralNode subNode : node.getBlocks()) {
            for (NodeProcessor np : nodeProcessors) {
                if (np.applies(subNode)) {
                    np.process(subNode);
                }
            }
        }

        if (isUnorderedListItem(item))
            sink.listItem_();
        else
            sink.numberedListItem_();
    }

    private static boolean isUnorderedListItem(ListItem item) {
        final String marker = item.getMarker();
        return marker.startsWith("*") || marker.startsWith("-");
    }
}
