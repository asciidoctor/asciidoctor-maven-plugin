package org.asciidoctor.maven.test.processors;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YellBlockProcessor extends BlockProcessor {

    @SuppressWarnings("serial")
    private static Map<String, Object> configs = Map.of(
            "contexts", List.of(":listing"),
            "content_model", ":compound"
    );

    public YellBlockProcessor(String name, Map<String, Object> config) {
        super(name, configs);
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        final String upperLines = reader.readLines()
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining("\n"));

        return createBlock(parent, "paragraph", List.of(upperLines), attributes, new HashMap<>());
    }
}
