package org.asciidoctor.maven.site.parser.processors;

import java.util.List;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.TableImpl;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeSinker;

import static javax.swing.text.html.HTML.Attribute.STYLE;
import static org.apache.maven.doxia.sink.Sink.JUSTIFY_LEFT;
import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * Table processor.
 *
 * @author abelsromero
 * @since 3.0.0
 */
public class TableNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    /**
     * Constructor.
     *
     * @param sink       Doxia {@link Sink}
     * @param nodeSinker
     */
    public TableNodeProcessor(Sink sink, NodeSinker nodeSinker) {
        super(sink, nodeSinker);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "table".equals(node.getNodeName());
    }

    @Override
    public void process(StructuralNode node) {
        final TableImpl tableNode = (TableImpl) node;

        final Sink sink = getSink();
        sink.table();
        sink.tableRows(new int[]{JUSTIFY_LEFT}, false);
        List<Row> header = tableNode.getHeader();
        if (!header.isEmpty()) {
            sink.tableRow();

            for (Row headerRow : header) {
                for (Cell cell : headerRow.getCells()) {
                    sink.tableHeaderCell();
                    sink.rawText(cell.getText());
                    sink.tableHeaderCell_();
                }
            }
            sink.tableRow_();
        }

        for (Row row : tableNode.getBody()) {
            sink.tableRow();
            for (Cell cell : row.getCells()) {
                sink.tableCell();
                sink.rawText(cell.getText());
                sink.tableCell_();
            }
            sink.tableRow_();
        }
        sink.tableRows_();

        processCaption(node, sink);

        sink.table_();
    }

    private void processCaption(StructuralNode node, Sink sink) {
        // 'null' when not set or '[caption=]'
        final String tableCaption = (String) node.getAttribute("table-caption");
        final String caption = node.getCaption();
        // disable single caption

        // if "[caption=]" -> remove caption
        // disable too, when ":table-caption!:"
        // final String title = node.getTitle();
        final String title = TitleCaptionExtractor.getText(node);
        if (isNotBlank(title)) {
            // Contrary to other cases where we use <div>, we use <caption>: same as Fluido and Asciidoctor
            // TODO Have a proper CSS stylesheet injected
            sink.tableCaption(SinkAttributes.of(STYLE, Styles.CAPTION + "; text-align: left"));
            // getCaption returns
            // - "" when '[caption=]'
            // - null when ':table-caption!:
            sink.text(title);
            sink.tableCaption_();
        }
    }
}
