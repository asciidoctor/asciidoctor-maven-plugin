package org.asciidoctor.maven.site.parser.processors;

import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;

/**
 * Description list processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class DescriptionListNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private ListItemNodeProcessor itemNodeProcessor;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public DescriptionListNodeProcessor(Sink sink) {
        super(sink);
    }

    /**
     * Inject a {@link ListItemNodeProcessor}.
     *
     * @param nodeProcessor {@link ListItemNodeProcessor}
     */
    public void setItemNodeProcessor(ListItemNodeProcessor nodeProcessor) {
        this.itemNodeProcessor = nodeProcessor;
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "dlist".equals(node.getNodeName());
    }

    @Override
    public boolean isTerminal(StructuralNode node) {
        return true;
    }

    @Override
    public void process(StructuralNode node) {

        final List<DescriptionListEntry> items = ((DescriptionList) node).getItems();
        final Sink sink = getSink();

        if (!items.isEmpty()) {
            sink.definitionList();
            for (DescriptionListEntry item : items) {
                // About the model, see https://asciidoctor.zulipchat.com/#narrow/stream/279642-users/topic/.E2.9C.94.20Description.20List.20AST.20structure/near/419353063
                final ListItem term = item.getTerms().get(0);
                sink.definedTerm();
                sink.rawText(term.getText());
                sink.definedTerm_();

                final ListItem description = item.getDescription();
                sink.definition();
                if (description.getBlocks().isEmpty()) {
                    sink.rawText(description.getText());
                } else {
                    itemNodeProcessor.process(description);
                }
                sink.definition_();
            }
            sink.definitionList_();
        }
    }
}
