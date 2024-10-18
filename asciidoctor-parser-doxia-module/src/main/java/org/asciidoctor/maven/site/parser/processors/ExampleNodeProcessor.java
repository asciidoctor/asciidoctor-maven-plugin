package org.asciidoctor.maven.site.parser.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;

import javax.swing.text.html.HTML.Attribute;
import java.nio.file.FileSystems;

import static javax.swing.text.html.HTML.Attribute.ALT;
import static org.asciidoctor.maven.commons.StringUtils.isBlank;
import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * Inline images are processed as paragraphs.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class ExampleNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink Doxia {@link Sink}
     */
    public ExampleNodeProcessor(Sink sink) {
        super(sink);
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
//        sink.division();
        final String title = TitleExtractor.getText(node);
        if (isNotBlank(title)) {
            sink.division(SinkAttributes.of(Attribute.STYLE, Styles.CAPTION));
            sink.text(title);
            sink.division_();
        }
//        sink.division_();
    }

    private String formatPath(String imagesdir, String target) {
        if (imagesdir.endsWith("/") || imagesdir.endsWith("\\")) {
            return imagesdir + target;
        } else {
            return imagesdir + FileSystems.getDefault().getSeparator() + target;
        }
    }
}
