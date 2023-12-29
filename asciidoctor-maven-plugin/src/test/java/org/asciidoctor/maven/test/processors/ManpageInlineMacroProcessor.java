package org.asciidoctor.maven.test.processors;

import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.InlineMacroProcessor;

import java.util.HashMap;
import java.util.Map;

public class ManpageInlineMacroProcessor extends InlineMacroProcessor {

    public ManpageInlineMacroProcessor(String macroName) {
        super(macroName);
    }

    @Override
    public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        final Map<String, Object> options = new HashMap<>();
        options.put("type", ":link");
        options.put("target", target + ".html");
        return createPhraseNode(parent, "anchor", target, attributes, options);
    }
}
