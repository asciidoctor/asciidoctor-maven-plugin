package ast;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.StructuralNode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class NodesSinker {

    private final Sink sink;
    private final List<NodeProcessor> nodeProcessors;

    public NodesSinker(Sink sink) {
        this.sink = sink;

        UnorderedListNodeProcessor unorderedListNodeProcessor = new UnorderedListNodeProcessor(sink);
        OrderedListNodeProcessor orderedListNodeProcessor = new OrderedListNodeProcessor(sink);

        ListItemNodeProcessor listItemNodeProcessor = new ListItemNodeProcessor(sink);
        listItemNodeProcessor.setNodeProcessors(Arrays.asList(unorderedListNodeProcessor, orderedListNodeProcessor));
        unorderedListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);
        orderedListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);

        nodeProcessors = Arrays.asList(
                new PreambleNodeProcessor(sink),
                new ParagraphNodeProcessor(sink),
                new SectionNodeProcessor(sink),
                unorderedListNodeProcessor,
                orderedListNodeProcessor,
                new TableNodeProcessor(sink),
                new ListingNodeProcessor(sink),
                new ImageNodeProcessor(sink)
        );
    }

    public void processNode(StructuralNode node, int depth) {
//            if (node instanceof Table) {
//                System.out.println("");
//            }
        String message = protectedApply(node, ContentNode::getNodeName) + " (" + node.getClass().getSimpleName() + ")";
        message += "\t\t\t\t context: " + protectedApply(node, ContentNode::getContext);
        message += "\t\t\t\t style: " + protectedApply(node, StructuralNode::getStyle);
        message += "\t\t\t\t level: " + protectedApply(node, n -> String.valueOf(n.getLevel()));

        println(message, depth);
        try {
            // only one matches in current implementation
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
            System.out.println(e);
        }
    }

    private void traverse(StructuralNode node, int depth) {
        node.getBlocks()
                .forEach(b -> processNode(b, depth + 1));
    }

    private String getContext(StructuralNode node) {
        return node.getContext();
    }

    private String protectedApply(StructuralNode node, Function<StructuralNode, String> function) {
        try {
            return function.apply(node);
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public void println(String message, int depth) {
        final String prefix = prefix(depth);
        System.out.println("[info] " + prefix + message);
    }

    private String prefix(int length) {
        return length <= 0 ? "" : StringUtils.repeat("  ", length + 1);
    }
}
