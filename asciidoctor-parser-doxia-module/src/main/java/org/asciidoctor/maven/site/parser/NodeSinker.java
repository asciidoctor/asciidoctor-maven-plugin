package org.asciidoctor.maven.site.parser;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.processors.DescriptionListNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.DocumentNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.ImageNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.ListItemNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.ListingNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.LiteralNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.NoOpNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.OrderedListNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.ParagraphNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.PreambleNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.SectionNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.TableNodeProcessor;
import org.asciidoctor.maven.site.parser.processors.UnorderedListNodeProcessor;

/**
 * Factory and repository for NodeProcessors.
 *
 * @author abelsromero
 * @since 3.1.0
 */
public class NodeSinker {

    private final List<NodeProcessor> nodeProcessors;

    private final NodeProcessor noOpProcessor;

    public NodeSinker(Sink sink) {
        nodeProcessors = Arrays.asList(
            new DescriptionListNodeProcessor(sink, this),
            new DocumentNodeProcessor(sink, this),
            new ImageNodeProcessor(sink, this),
            new ListItemNodeProcessor(sink, this),
            new ListingNodeProcessor(sink, this),
            new ListingNodeProcessor(sink, this),
            new LiteralNodeProcessor(sink, this),
            new OrderedListNodeProcessor(sink, this),
            new ParagraphNodeProcessor(sink, this),
            new PreambleNodeProcessor(sink, this),
            new SectionNodeProcessor(sink, this),
            new TableNodeProcessor(sink, this),
            new UnorderedListNodeProcessor(sink, this)
        );
        noOpProcessor = new NoOpNodeProcessor(sink, this);
    }

    /**
     * Returns first NodeProcessor that can treat the node.
     **/
    private NodeProcessor get(StructuralNode node) {
        return nodeProcessors.stream()
            .filter(nodeProcessor -> nodeProcessor.applies(node))
            .findFirst()
            .orElse(noOpProcessor);
    }

    public void sink(StructuralNode node) {
        get(node).process(node);
    }
}
