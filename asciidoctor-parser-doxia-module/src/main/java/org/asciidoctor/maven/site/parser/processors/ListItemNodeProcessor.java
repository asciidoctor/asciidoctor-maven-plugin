package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeProcessorProvider;

/**
 * List items processor, including numbered and unnumbered.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class ListItemNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink                  Doxia {@link Sink}
     * @param nodeProcessorProvider
     */
    public ListItemNodeProcessor(Sink sink, NodeProcessorProvider nodeProcessorProvider) {
        super(sink, nodeProcessorProvider);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "list_item".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        final ListItem item = (ListItem) node;
        final Sink sink = getSink();
        final ListType listType = getListType(item);

        // description type does not require any action
        switch (listType) {
            case ordered:
                sink.numberedListItem();
                break;
            case unordered:
                sink.listItem();
                break;
        }

        final String text = item.getText();
        sink.rawText(text == null ? "" : text);

        node.getBlocks().forEach(this::next);

        switch (listType) {
            case ordered:
                sink.numberedListItem_();
                break;
            case unordered:
                sink.listItem_();
                break;
        }
    }

    private static ListType getListType(ListItem item) {
        final String marker = item.getMarker();
        if (marker == null) {
            return ListType.description;
        } else if (marker.startsWith("*") || marker.startsWith("-")) {
            return ListType.ordered;
        } else {
            return ListType.unordered;
        }
    }

    enum ListType {
        ordered, unordered, description
    }
}
