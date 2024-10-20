package org.asciidoctor.maven.site.parser.processors;

import java.nio.file.FileSystems;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeSinker;

import static javax.swing.text.html.HTML.Attribute.ALT;
import static javax.swing.text.html.HTML.Attribute.STYLE;
import static org.asciidoctor.maven.commons.StringUtils.isBlank;
import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * Inline images are processed as paragraphs.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class ImageNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink       Doxia {@link Sink}
     * @param nodeSinker
     */
    public ImageNodeProcessor(Sink sink, NodeSinker nodeSinker) {
        super(sink, nodeSinker);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "image".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        final String target = (String) node.getAttribute("target");
        final String alt = (String) node.getAttribute("alt");

        final String imagesdir = (String) node.getAttribute("imagesdir");
        final String imagePath = isBlank(imagesdir) ? target : formatPath(imagesdir, target);

        // Add caption as a div (same as Asciidoctor):
        //  - For consistency
        //  - Using `figureCaption` requires wrapping the image in <figure> which adds indentation
        final Sink sink = getSink();
        sink.division();
        sink.figureGraphics(imagePath, !isBlank(alt) ? SinkAttributes.of(ALT, alt) : null);
        final String title = TitleCaptionExtractor.getText(node);
        if (isNotBlank(title)) {
            sink.division(SinkAttributes.of(STYLE, Styles.CAPTION));
            sink.rawText(title);
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
