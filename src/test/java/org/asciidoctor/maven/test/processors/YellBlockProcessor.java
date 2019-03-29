package org.asciidoctor.maven.test.processors;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Reader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YellBlockProcessor extends BlockProcessor {

    @SuppressWarnings("serial")
    private static Map<String, Object> configs = new HashMap<String, Object>() {{
        put("contexts", Arrays.asList(":listing"));
        put("content_model", ":compound");
    }};

    public YellBlockProcessor(String name, Map<String, Object> config) {
        super(name, configs);
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        List<String> lines = reader.readLines();
        String upperLines = null;
        for (String line : lines) {
            if (upperLines == null) {
                upperLines = line.toUpperCase();
            } else {
                upperLines = upperLines + "\n" + line.toUpperCase();
            }
        }

        return createBlock(parent, "paragraph", Arrays.asList(upperLines), attributes, new HashMap<>());
    }

}
