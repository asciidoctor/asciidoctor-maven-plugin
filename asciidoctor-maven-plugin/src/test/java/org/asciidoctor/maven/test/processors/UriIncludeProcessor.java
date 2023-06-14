package org.asciidoctor.maven.test.processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;

public class UriIncludeProcessor extends IncludeProcessor {

    public UriIncludeProcessor(Map<String, Object> config) {
        super(config);
        System.out.println(String.format("%s(%s) initialized", this.getClass().getSimpleName(), this.getClass().getSuperclass().getSimpleName()));
    }

    @Override
    public boolean handles(String target) {
        return target.matches("^https?://.*");
    }

    @Override
    public void process(Document document, PreprocessorReader reader, String target,
                        Map<String, Object> attributes) {
        System.out.println("Processing " + this.getClass().getSimpleName());
        final String content = readContent(target);
        reader.pushInclude(content, target, target, 1, attributes);
    }

    private String readContent(String target) {
        try (var openStream = new URL(target).openStream()) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openStream))) {
                return bufferedReader.lines()
                        .collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
