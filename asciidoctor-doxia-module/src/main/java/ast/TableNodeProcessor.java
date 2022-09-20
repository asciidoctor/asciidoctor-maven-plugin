package ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.TableImpl;

import java.util.List;

import static org.apache.maven.doxia.sink.Sink.JUSTIFY_LEFT;

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
        // TODO
        final TableImpl tableNode = (TableImpl) node;

        final Sink sink = getSink();
        sink.table();
        // issue: creates an extra row
        sink.tableRows(new int[]{JUSTIFY_LEFT}, false);

        List<Row> header = tableNode.getHeader();
        // note: use rawText to allow injecting HTML
        // since asciidoctor returns styled text already
        if (!header.isEmpty()) {
            sink.tableRow();
//            sink.rawText("<thead>");
            for (Row headerRow : header) {
                for (Cell cell : headerRow.getCells()) {
                    sink.tableHeaderCell();
                    sink.rawText(cell.getText());
                    sink.tableHeaderCell_();
                }
            }
//            sink.rawText("</thead>");
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

        // TODO caption

        sink.table_();

        System.out.println("12312");
    }
}
