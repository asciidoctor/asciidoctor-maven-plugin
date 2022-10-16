package org.asciidoctor.maven.site.ast;

import org.asciidoctor.ast.StructuralNode;

/**
 * Basic unit for content generation.
 * <p>
 * A NodeProcessor is responsible for generating the output (HTML)
 * for one AST node. In Asciidoctor terms, it's a Converter for
 * a specific Node type (but can be accommodated to convert multiples).
 *
 * @author abelsromero
 */
public interface NodeProcessor {

    /**
     * Whether the processor can process the node.
     *
     * @param node candidate node to process
     */
    boolean applies(StructuralNode node);

    /**
     * Whether sub-nodes should be processed recursively.
     *
     * @param node candidate node to process
     */
    default boolean isTerminal(StructuralNode node) {
        return false;
    }

    /**
     * Processes the node.
     *
     * @param node node to process
     */
    void process(StructuralNode node);

}
