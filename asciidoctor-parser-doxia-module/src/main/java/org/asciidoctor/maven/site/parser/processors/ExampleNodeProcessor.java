package org.asciidoctor.maven.site.parser.processors;

import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeSinker;

import static org.apache.maven.doxia.sink.SinkEventAttributes.STYLE;
import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * Inline images are processed as paragraphs.
 *
 * @author abelsromero
 * @since 3.1.0
 */
public class ExampleNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink       Doxia {@link Sink}
     * @param nodeSinker
     */
    public ExampleNodeProcessor(Sink sink, NodeSinker nodeSinker) {
        super(sink, nodeSinker);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "example".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        // Add caption as a div (same as Asciidoctor):
        //  - For consistency
        //  - Using `figureCaption` requires wrapping the image in <figure> which adds indentation
        final Sink sink = getSink();

        sink.division();
        final String title = TitleCaptionExtractor.getText(node);
        if (isNotBlank(title)) {
            sink.division(SinkAttributes.of(STYLE, Styles.CAPTION));
            sink.rawText(title);
            sink.division_();
        }

        final List<StructuralNode> blocks = node.getBlocks();
        if (!blocks.isEmpty()) {
            divWrap(sink, () -> blocks.forEach(this::sink));
        } else {
            // For :content_model: simple (inline)
            // https://docs.asciidoctor.org/asciidoc/latest/blocks/example-blocks/#example-style-syntax
            final String content = (String) node.getContent();
            if (isNotBlank(content)) {
                divWrap(sink, () -> sink.rawText(content));
            }
        }

        sink.division_();
    }

    void divWrap(Sink sink, Runnable consumer) {
        sink.division(SinkAttributes.of(STYLE, Styles.EXAMPLE));
        consumer.run();
        sink.division_();
    }
}
