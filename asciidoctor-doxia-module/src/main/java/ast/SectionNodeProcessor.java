package ast;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.SectionImpl;

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
        String id = node.getId();
        String style = node.getStyle();
        String name = ((SectionImpl) node).getSectionName();
        String numeral = ((SectionImpl) node).getNumeral();

        sectionTitle(getSink(), node.getLevel(), node.getTitle());
    }

    private void sectionTitle(Sink sink, int level, String title) {
        switch (level) {
            case 0:
                sink.sectionTitle();
                sink.text(title);
                sink.sectionTitle_();
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
