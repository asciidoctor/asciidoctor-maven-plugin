package org.asciidoctor.maven.site.parser.processors.test;

import java.lang.reflect.Constructor;

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.NodeSinker;
import org.mockito.Mockito;

public class TestNodeProcessorFactory {

    @SneakyThrows
    public static <T extends NodeProcessor> Pair<T, Sink> create(Class<T> clazz) {
        final Sink siteRendererSink = createSink();
        final NodeSinker nodeSinker = new NodeSinker(siteRendererSink);

        Constructor<T> constructor = clazz.getConstructor(Sink.class, NodeSinker.class);
        return Pair.of(constructor.newInstance(siteRendererSink, nodeSinker), siteRendererSink);
    }

    public static Sink createSink() {
        return new SiteRendererSink(Mockito.mock(DocumentRenderingContext.class));
    }
}
