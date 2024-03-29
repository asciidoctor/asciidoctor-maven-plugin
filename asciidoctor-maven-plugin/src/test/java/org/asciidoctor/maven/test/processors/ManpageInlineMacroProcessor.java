package org.asciidoctor.maven.test.processors;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;

public class ManpageInlineMacroProcessor extends InlineMacroProcessor {

    public ManpageInlineMacroProcessor(String macroName) {
        super(macroName);
    }

    @Override
    public String process(ContentNode parent, String target, Map<String, Object> attributes) {

        final Map<String, Object> options = new HashMap<>();
        options.put("type", ":link");
        options.put("target", target + ".html");
        return createPhraseNode(parent, "anchor", target, attributes, options).convert();
    }
}
