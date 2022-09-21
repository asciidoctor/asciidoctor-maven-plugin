package ast.test;

import ast.NodeProcessor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mockito;

import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class JUnitNodeProcessorExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        final Field asciidoctorField = findField(testInstance, Asciidoctor.class);
        if (asciidoctorField != null) {
            injectField(testInstance, asciidoctorField, Asciidoctor.Factory.create());
        }

        final Field nodeProcessorField = findField(testInstance, NodeProcessor.class);
        if (nodeProcessorField != null) {
            Pair<? extends NodeProcessor, Sink> np = nodeProcessor(extractNodeProcessorType(testInstance));
            injectField(testInstance, nodeProcessorField, np.getLeft());

            final Field sinkWriter = findField(testInstance, StringWriter.class);
            if (sinkWriter != null) {
                Sink sink = np.getRight();
                StringWriter writer = extractField(sink, "writer");
                injectField(testInstance, sinkWriter, writer);
            }
        }
    }

    private Pair<? extends NodeProcessor, Sink> nodeProcessor(Class<? extends NodeProcessor> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        RenderingContext renderingContext = Mockito.mock(RenderingContext.class);
        Sink siteRendererSink = new SiteRendererSink(renderingContext);
        Constructor<? extends NodeProcessor> constructor = clazz.getConstructor(Sink.class);
        return Pair.of(constructor.newInstance(siteRendererSink), siteRendererSink);
    }

    private Field findField(Object testInstance, Class<?> clazz) {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (clazz.equals(field.getType())) return field;
        }
        return null;
    }

    private void injectField(Object testInstance, Field field, Object value) throws IllegalAccessException {

        if (Modifier.isPrivate(field.getModifiers())) {
            field.setAccessible(true);
            field.set(testInstance, value);
            field.setAccessible(false);
        } else {
            field.set(testInstance, value);
        }
    }

    private Class<? extends NodeProcessor> extractNodeProcessorType(Object testInstance) {
        return extractAnnotation(testInstance)
                .value();
    }

    private NodeProcessorTest extractAnnotation(Object testInstance) {
        return testInstance
                .getClass()
                .getAnnotation(NodeProcessorTest.class);
    }

    private StringWriter extractField(Object sink, String writer) throws NoSuchFieldException, IllegalAccessException {
        Field field = sink.getClass().getDeclaredField("writer");
        // We don't care to alter the instance, only lives during the test
        field.setAccessible(true);
        return (StringWriter) field.get(sink);
    }
}
