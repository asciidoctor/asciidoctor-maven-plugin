package org.asciidoctor.maven.site.ast.processors;

import java.nio.file.FileSystems;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.ast.NodeProcessor;

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
        getSink().rawText(String.format("<img src=\"%s\" alt=\"%s\">", imagePath, alt));
    }

    private String formatPath(String imagesdir, String target) {
        if (imagesdir.endsWith("/") || imagesdir.endsWith("\\")) {
            return imagesdir + target;
        } else {
            return imagesdir + FileSystems.getDefault().getSeparator() + target;
        }
    }
}
