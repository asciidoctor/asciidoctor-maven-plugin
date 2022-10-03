package org.asciidoctor.maven.site.ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.processors.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class NodesSinker {

    private final List<NodeProcessor> nodeProcessors;

    public NodesSinker(Sink sink) {

        UnorderedListNodeProcessor unorderedListNodeProcessor = new UnorderedListNodeProcessor(sink);
        OrderedListNodeProcessor orderedListNodeProcessor = new OrderedListNodeProcessor(sink);

        ListItemNodeProcessor listItemNodeProcessor = new ListItemNodeProcessor(sink);
        listItemNodeProcessor.setNodeProcessors(Arrays.asList(unorderedListNodeProcessor, orderedListNodeProcessor));
        unorderedListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);
        orderedListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);

        nodeProcessors = Arrays.asList(
                new DocumentNodeProcessor(sink),
                new PreambleNodeProcessor(sink),
                new ParagraphNodeProcessor(sink),
                new SectionNodeProcessor(sink),
                unorderedListNodeProcessor,
                orderedListNodeProcessor,
                new TableNodeProcessor(sink),
                new ListingNodeProcessor(sink),
                new ImageNodeProcessor(sink),
                new LiteralNodeProcessor(sink)
        );
    }

    public void processNode(StructuralNode node) {
        processNode(node, 0);
    }

    private void processNode(StructuralNode node, int depth) {
        try {
            // Only one matches in current NodeProcessors implementation
            Optional<NodeProcessor> nodeProcessor = nodeProcessors.stream()
                    .filter(np -> np.applies(node))
                    .findFirst();
            if (nodeProcessor.isPresent()) {
                NodeProcessor processor = nodeProcessor.get();
                processor.process(node);
                if (!processor.isTerminal(node)) {
                    traverse(node, depth);
                }
            } else {
                traverse(node, depth);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not process node", e);
        }
    }

    private void traverse(StructuralNode node, int depth) {
        node.getBlocks()
                .forEach(b -> processNode(b, depth + 1));
    }
}
