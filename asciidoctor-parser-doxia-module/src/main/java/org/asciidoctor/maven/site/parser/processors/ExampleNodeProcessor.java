package org.asciidoctor.maven.site.parser.processors;

import java.nio.file.FileSystems;
import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeSinker;

import static javax.swing.text.html.HTML.Attribute.STYLE;
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
        final String title = TitleExtractor.getText(node);
        if (isNotBlank(title)) {
            sink.division(SinkAttributes.of(STYLE, Styles.CAPTION));
            sink.text(title);
            sink.division_();
        }

        final List<StructuralNode> blocks = node.getBlocks();
        if (!blocks.isEmpty()) {
            sink.division(SinkAttributes.of(STYLE, Styles.EXAMPLE));
            blocks.forEach(this::sink);
            sink.division_();
        }

        sink.division_();

    }

    private String formatPath(String imagesdir, String target) {
        if (imagesdir.endsWith("/") || imagesdir.endsWith("\\")) {
            return imagesdir + target;
        } else {
            return imagesdir + FileSystems.getDefault().getSeparator() + target;
        }
    }
}
