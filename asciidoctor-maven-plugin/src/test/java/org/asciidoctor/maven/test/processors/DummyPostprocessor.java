package org.asciidoctor.maven.test.processors;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;

import java.util.Map;

public class DummyPostprocessor extends Postprocessor {

    public DummyPostprocessor(Map<String, Object> config) {
        super(config);
        System.out.println(String.format("%s(%s) initialized", this.getClass().getSimpleName(), this.getClass().getSuperclass().getSimpleName()));
    }

    @Override
    public String process(Document document, String output) {
        System.out.println("Processing " + this.getClass().getSimpleName());
        System.out.println("Processing: blocks found: " + document.getBlocks().size());
        System.out.println("Processing: output size: " + output.length());
        return output;
    }
}
