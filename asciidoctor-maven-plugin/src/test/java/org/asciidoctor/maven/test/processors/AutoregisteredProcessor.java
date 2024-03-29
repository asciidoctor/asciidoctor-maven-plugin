package org.asciidoctor.maven.test.processors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class AutoregisteredProcessor implements ExtensionRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {
        asciidoctor.javaExtensionRegistry()
                .preprocessor(ChangeAttributeValuePreprocessor.class);
    }
}
