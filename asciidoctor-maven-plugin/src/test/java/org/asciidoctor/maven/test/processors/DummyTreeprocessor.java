package org.asciidoctor.maven.test.processors;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;

public class DummyTreeprocessor extends Treeprocessor {

    public DummyTreeprocessor() {
        super(new HashMap<>());
    }

    public DummyTreeprocessor(Map<String, Object> config) {
        super(config);
        System.out.println(String.format("%s(%s) initialized", this.getClass().getSimpleName(), this.getClass().getSuperclass().getSimpleName()));
    }

    @Override
    public Document process(Document document) {
        System.out.println("Processing " + this.getClass().getSimpleName());
        System.out.println("Processing: blocks found: " + document.getBlocks().size());
        return document;
    }
}
