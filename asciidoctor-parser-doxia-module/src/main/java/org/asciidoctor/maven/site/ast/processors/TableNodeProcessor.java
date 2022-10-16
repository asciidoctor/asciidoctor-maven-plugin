package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.TableImpl;
import org.asciidoctor.maven.site.ast.NodeProcessor;

import java.util.List;

import static org.apache.maven.doxia.sink.Sink.JUSTIFY_LEFT;
import static org.asciidoctor.maven.commons.StringUtils.isBlank;
import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

public class TableNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    public TableNodeProcessor(Sink sink) {
        super(sink);
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
        // disable single caption

        final String title = node.getTitle();
        if (isNotBlank(title)) {
            node.getContentModel();
            sink.tableCaption();
            // It's safe: getCaption returns "" when '[caption=]' is set
            if (isBlank(node.getCaption()))
                sink.text(node.getTitle());
            else
                sink.text(node.getCaption() + node.getTitle());
            sink.tableCaption_();
        }
    }

}
