package org.asciidoctor.maven.test.processors;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.InlineMacroProcessor;

public class ManpageInlineMacroProcessor extends InlineMacroProcessor {

    public ManpageInlineMacroProcessor(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }

    @Override
    public String process(AbstractBlock parent, String target, Map<String, Object> attributes) {

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("type", ":link");
        options.put("target", target + ".html");
        return createInline(parent, "anchor", target, attributes, options).convert();
    }

}
