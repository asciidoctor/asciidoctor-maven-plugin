package org.asciidoctor.maven.site.ast;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.processors.DocumentNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.ImageNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.ListItemNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.ListingNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.LiteralNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.OrderedListNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.ParagraphNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.PreambleNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.SectionNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.TableNodeProcessor;
import org.asciidoctor.maven.site.ast.processors.UnorderedListNodeProcessor;

/**
 * Document processor.
 * Responsible for initializing the different Node Processors
 * and traverse the AST.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class NodesSinker {

    private final List<NodeProcessor> nodeProcessors;

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public NodesSinker(Sink sink) {

        UnorderedListNodeProcessor unorderedListNodeProcessor = new UnorderedListNodeProcessor(sink);
        OrderedListNodeProcessor orderedListNodeProcessor = new OrderedListNodeProcessor(sink);

        ListItemNodeProcessor listItemNodeProcessor = new ListItemNodeProcessor(sink);
        listItemNodeProcessor.setNodeProcessors(Arrays.asList(unorderedListNodeProcessor, orderedListNodeProcessor));
        unorderedListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);
        orderedListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);

        nodeProcessors = Arrays.asList(
                new DocumentNodeProcessor(sink),
                new ImageNodeProcessor(sink),
                new ListingNodeProcessor(sink),
                new LiteralNodeProcessor(sink),
                new ParagraphNodeProcessor(sink),
                new PreambleNodeProcessor(sink),
                new SectionNodeProcessor(sink),
                new TableNodeProcessor(sink),
                orderedListNodeProcessor,
                unorderedListNodeProcessor
        );
    }

    /**
     * Processes an Asciidoctor AST node.
     *
     * @param node Asciidoctor {@link StructuralNode}
     */
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
