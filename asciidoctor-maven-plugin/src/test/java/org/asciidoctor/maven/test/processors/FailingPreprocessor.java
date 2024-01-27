package org.asciidoctor.maven.test.processors;

import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

public class FailingPreprocessor extends Preprocessor {

    public FailingPreprocessor(Map<String, Object> config) {
        super(config);
        System.out.println(String.format("%s(%s) initialized", this.getClass().getSimpleName(), this.getClass().getSuperclass().getSimpleName()));
    }

    @Override
    public void process(Document document, PreprocessorReader reader) {
        System.out.println("Processing " + this.getClass().getSimpleName());
        System.out.println("Processing: blocks found: " + document.getBlocks().size());
        throw new RuntimeException("That's all folks");
    }

}
