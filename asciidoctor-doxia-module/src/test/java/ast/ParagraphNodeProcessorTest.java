package ast;

import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ParagraphNodeProcessorTest {


    @Test
    void should_convert() throws NoSuchFieldException, IllegalAccessException {

        RenderingContext renderingContext = Mockito.mock(RenderingContext.class);
        SiteRendererSink siteRendererSink = new SiteRendererSink(renderingContext);
        final NodeProcessor nodeProcessor = new ParagraphNodeProcessor(siteRendererSink);

        String content = "= Tile\n\n" +
                "== Section\n\n" +
                "SomeText";

        StructuralNode node = Asciidoctor.Factory.create()
                .load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":paragraph")).get(0);

        nodeProcessor.process(node);

        siteRendererSink.flush();

        Field field = siteRendererSink.getClass().getDeclaredField("writer");
        field.setAccessible(true);
        StringWriter writer = (StringWriter) field.get(siteRendererSink);

        String s = writer.toString();

        assertThat(s).isEqualTo("<p>SomeText</p>");
    }

}
