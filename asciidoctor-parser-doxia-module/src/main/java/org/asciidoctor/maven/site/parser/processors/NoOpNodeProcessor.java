package org.asciidoctor.maven.site.parser.processors;

import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.AsciidoctorAstDoxiaParser;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeSinker;
import org.slf4j.LoggerFactory;

/**
 * Fallback NodeProcessor to collect nodes that have not dedicated processor.
 */
public class NoOpNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(AsciidoctorAstDoxiaParser.class);

    /**
     * Constructor.
     *
     * @param sink       Doxia {@link Sink}
     * @param nodeSinker
     */
    public NoOpNodeProcessor(Sink sink, NodeSinker nodeSinker) {
        super(sink, nodeSinker);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return false;
    }

    @Override
    public void process(StructuralNode node) {
        final List<StructuralNode> blocks = node.getBlocks();

        logger.warn("Fallback behaviour for node: {}", node.getNodeName());
        if (!blocks.isEmpty()) {
            blocks.forEach(this::sink);
        }
    }
}
