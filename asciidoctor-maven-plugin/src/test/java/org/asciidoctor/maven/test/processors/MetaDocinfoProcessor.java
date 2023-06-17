package org.asciidoctor.maven.test.processors;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.DocinfoProcessor;

import java.util.Map;

public class MetaDocinfoProcessor extends DocinfoProcessor {

    public MetaDocinfoProcessor(Map<String, Object> config) {
        super(config);
        System.out.println(String.format("%s(%s) initialized", this.getClass().getSimpleName(), this.getClass().getSuperclass().getSimpleName()));
    }

    @Override
    public String process(Document document) {
        System.out.println("Processing " + this.getClass().getSimpleName());
        System.out.println("Processing: blocks found: " + document.getBlocks().size());
        return "<meta name=\"author\" content=\"asciidoctor\">";
    }
}
