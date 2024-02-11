package org.asciidoctor.maven.site.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.processors.DescriptionListNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.DocumentNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.ImageNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.ListItemNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.ListingNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.LiteralNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.OrderedListNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.ParagraphNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.PreambleNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.SectionNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.TableNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.UnorderedListNodeProcessor;

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
        DescriptionListNodeProcessor descriptionListNodeProcessor = new DescriptionListNodeProcessor(sink);

        ListItemNodeProcessor listItemNodeProcessor = new ListItemNodeProcessor(sink);
        listItemNodeProcessor.setNodeProcessors(Arrays.asList(unorderedListNodeProcessor, orderedListNodeProcessor, descriptionListNodeProcessor));
        unorderedListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);
        orderedListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);
        descriptionListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);

        nodeProcessors = Arrays.asList(
            new DocumentNodeProcessor(sink),
            new ImageNodeProcessor(sink),
            new ListingNodeProcessor(sink),
            new LiteralNodeProcessor(sink),
            new ParagraphNodeProcessor(sink),
            new PreambleNodeProcessor(sink),
            new SectionNodeProcessor(sink),
            new TableNodeProcessor(sink),
            descriptionListNodeProcessor,
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
