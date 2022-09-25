package org.asciidoctor.maven.site.ast;

import org.asciidoctor.ast.StructuralNode;

public interface NodeProcessor {

    /**
     * Whether the processor can process the node.
     *
     * @param node candidate node to process
     */
    boolean applies(StructuralNode node);

    /**
     * Tells whether the processor processes the current node and sub-nodes.
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
