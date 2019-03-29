package org.asciidoctor.maven.test.processors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class AutoregisteredProcessor implements ExtensionRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {

        JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        javaExtensionRegistry.preprocessor(ChangeAttributeValuePreprocessor.class);

    }

}