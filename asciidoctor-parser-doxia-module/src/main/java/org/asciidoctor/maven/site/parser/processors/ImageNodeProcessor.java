package org.asciidoctor.maven.site.parser.processors;

import javax.swing.text.html.HTML.Attribute;
import java.nio.file.FileSystems;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;

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
     * @param sink Doxia {@link Sink}
     */
    public ImageNodeProcessor(Sink sink) {
        super(sink);
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
        String imagePath = isBlank(imagesdir) ? target : formatPath(imagesdir, target);
        final SinkEventAttributeSet attributes = new SinkEventAttributeSet();
        attributes.addAttribute(Attribute.ALT, alt);
        getSink().figureGraphics(imagePath, attributes);
    }

    private String formatPath(String imagesdir, String target) {
        if (imagesdir.endsWith("/") || imagesdir.endsWith("\\")) {
            return imagesdir + target;
        } else {
            return imagesdir + FileSystems.getDefault().getSeparator() + target;
        }
    }
}
