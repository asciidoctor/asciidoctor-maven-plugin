package org.asciidoctor.maven.site.ast.processors;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.SectionImpl;
import org.asciidoctor.maven.site.ast.NodeProcessor;

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
        sectionTitle(getSink(), node.getLevel(), node.getTitle(), (Section) node);
    }

    private void sectionTitle(Sink sink, int level, String title, Section node) {
        final String formattedTitle = formatTitle(title, node);
        switch (level) {
            case 0:
                // Kept for completeness, real document title is treated in
                // DocumentNodeProcessor
                sink.rawText("<h1>" + formattedTitle + "</h1>");
                break;
            case 1:
                sink.sectionTitle1();
                sink.text(formattedTitle);
                sink.sectionTitle1_();
                break;
            case 2:
                sink.sectionTitle2();
                sink.text(formattedTitle);
                sink.sectionTitle2_();
                break;
            case 3:
                sink.sectionTitle3();
                sink.text(formattedTitle);
                sink.sectionTitle3_();
                break;
            case 4:
                sink.sectionTitle4();
                sink.text(formattedTitle);
                sink.sectionTitle4_();
                break;
            case 5:
                sink.sectionTitle5();
                sink.text(formattedTitle);
                sink.sectionTitle5_();
                break;
            case 6:
                sink.sectionTitle6();
                sink.text(formattedTitle);
                sink.sectionTitle6_();
                break;
        }
    }

    private String formatTitle(String title, Section node) {
        Boolean numbered = ((SectionImpl) node).getBoolean("numbered");
        if (numbered) {
            String sectnum = ((SectionImpl) node).getString("sectnum");
            return String.format("%s %s", sectnum, title);
        }
        return title;
    }
}
