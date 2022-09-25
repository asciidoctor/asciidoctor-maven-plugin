package org.asciidoctor.maven.site.ast.processors.test;

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.asciidoctor.maven.site.ast.NodeProcessor;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;

public class TestNodeProcessorFactory {

    @SneakyThrows
    public static <T extends NodeProcessor> Pair<T, Sink> create(Class<T> clazz) {
        RenderingContext renderingContext = Mockito.mock(RenderingContext.class);
        Sink siteRendererSink = new SiteRendererSink(renderingContext);
        Constructor<T> constructor = clazz.getConstructor(Sink.class);
        return Pair.of(constructor.newInstance(siteRendererSink), siteRendererSink);
    }
}
