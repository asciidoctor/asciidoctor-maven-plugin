package org.asciidoctor.maven.site.parser.processors.test;

import java.io.StringWriter;
import java.lang.reflect.Field;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import static org.asciidoctor.maven.site.parser.processors.test.ReflectionUtils.extractField;
import static org.asciidoctor.maven.site.parser.processors.test.ReflectionUtils.findField;
import static org.asciidoctor.maven.site.parser.processors.test.ReflectionUtils.injectField;

public class JUnitNodeProcessorExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws IllegalAccessException, NoSuchFieldException {
        final Field asciidoctorField = findField(testInstance, Asciidoctor.class);
        if (asciidoctorField != null) {
            injectField(testInstance, asciidoctorField, Asciidoctor.Factory.create());
        }

        final Field nodeProcessorField = findField(testInstance, NodeProcessor.class);
        if (nodeProcessorField != null) {
            Pair<? extends NodeProcessor, Sink> np = TestNodeProcessorFactory.create(extractNodeProcessorType(testInstance));
            injectField(testInstance, nodeProcessorField, np.getLeft());

            final Field sinkField = findField(testInstance, Sink.class);
            if (sinkField != null) {
                injectField(testInstance, sinkField, np.getRight());
            }

            final Field sinkWriter = findField(testInstance, StringWriter.class);
            if (sinkWriter != null) {
                Sink sink = np.getRight();
                StringWriter writer = extractField(sink, "writer");
                injectField(testInstance, sinkWriter, writer);
            }
        }
    }

    private Class<? extends NodeProcessor> extractNodeProcessorType(Object instance) {
        return extractAnnotation(instance)
                .value();
    }

    private NodeProcessorTest extractAnnotation(Object instance) {
        return instance
                .getClass()
                .getAnnotation(NodeProcessorTest.class);
    }
}
