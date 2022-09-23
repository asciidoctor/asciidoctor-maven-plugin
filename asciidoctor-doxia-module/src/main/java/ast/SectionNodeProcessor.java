package ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.StructuralNode;

public class SectionNodeProcessor extends AbstractSinkNodeProcessor implements NodeProcessor {

    public SectionNodeProcessor(Sink sink) {
        super(sink);
    }

    @Override
    public boolean applies(StructuralNode node) {
        return "section".equals(node.getNodeName());
    }

    // TODO use asciidoctor id (would require writing plain HTML)
    @Override
    public void process(StructuralNode node) {
        sectionTitle(getSink(), node.getLevel(), node.getTitle());
    }

    private void sectionTitle(Sink sink, int level, String title) {
        switch (level) {
            case 0:
                // Kept for completeness, real document title is treated in
                // DocumentNodeProcessor
                sink.rawText("<h1>" + title + "</h1>");
                break;
            case 1:
                sink.sectionTitle1();
                sink.text(title);
                sink.sectionTitle1_();
                break;
            case 2:
                sink.sectionTitle2();
                sink.text(title);
                sink.sectionTitle2_();
                break;
            case 3:
                sink.sectionTitle3();
                sink.text(title);
                sink.sectionTitle3_();
                break;
            case 4:
                sink.sectionTitle4();
                sink.text(title);
                sink.sectionTitle4_();
                break;
            case 5:
                sink.sectionTitle5();
                sink.text(title);
                sink.sectionTitle5_();
                break;
            case 6:
                sink.sectionTitle6();
                sink.text(title);
                sink.sectionTitle6_();
                break;
        }
    }
}
