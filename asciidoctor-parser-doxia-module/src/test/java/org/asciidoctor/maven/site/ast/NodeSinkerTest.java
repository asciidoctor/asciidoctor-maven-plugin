package org.asciidoctor.maven.site.ast;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.BlockImpl;
import org.asciidoctor.jruby.ast.impl.DocumentImpl;
import org.asciidoctor.jruby.ast.impl.SectionImpl;
import org.asciidoctor.jruby.ast.impl.TableImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.StringWriter;
import java.util.Arrays;

import static org.asciidoctor.maven.site.ast.processors.test.ReflectionUtils.extractField;
import static org.asciidoctor.maven.site.ast.processors.test.TestNodeProcessorFactory.createSink;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validate node processors are registered.
 */
class NodeSinkerTest {

    private NodesSinker nodesSinker;
    private StringWriter sinkWriter;


    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        Sink sink = createSink();
        nodesSinker = new NodesSinker(sink);
        sinkWriter = extractField(sink, "writer");
    }

    @Test
    void should_init() {
        assertThat(nodesSinker).isNotNull();
    }


    @Test
    void should_not_fail_when_processing_invalid_node() {
        StructuralNode mockNode = mockNode("this-is-not-a-node");

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isEmpty();
    }

    @Test
    void should_process_document_node() {
        StructuralNode mockNode = mockNode("document");

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_preamble_literal() {
        StructuralNode mockNode = mockNode("literal", BlockImpl.class);

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_preamble_node() {
        StructuralNode mockNode = mockNode("preamble");
        StructuralNode literalBlock = mockNode("literal", BlockImpl.class);
        Mockito.when(mockNode.getBlocks()).thenReturn(Arrays.asList(literalBlock));

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_paragraph_node() {
        StructuralNode mockNode = mockNode("paragraph");
        Mockito.when(mockNode.getContent()).thenReturn("something");

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_section_node() {
        StructuralNode mockNode = mockNode("section", SectionImpl.class);
        Document mockDocument = mockNode("section", DocumentImpl.class);
        Mockito.when(mockDocument.getAttribute(Mockito.anyString())).thenReturn(null);
        Mockito.when(mockNode.getDocument()).thenReturn(mockDocument);

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_table_node() {
        StructuralNode mockNode = mockNode("table", TableImpl.class);

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_listing_node() {
        StructuralNode mockNode = mockNode("listing", BlockImpl.class);

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_image_node() {
        StructuralNode mockNode = mockNode("image", BlockImpl.class);

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_literal_node() {
        StructuralNode mockNode = mockNode("literal", BlockImpl.class);

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_ulist_node() {
        StructuralNode mockNode = mockNode("ulist", BlockImpl.class);
        ListItem mockListItem = mockNode("list_item", ListItem.class);
        Mockito.when(mockListItem.getMarker()).thenReturn("*");
        Mockito.when(mockNode.getBlocks()).thenReturn(Arrays.asList(mockListItem));

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    @Test
    void should_process_olist_node() {
        StructuralNode mockNode = mockNode("olist", BlockImpl.class);
        ListItem mockListItem = mockNode("list_item", ListItem.class);
        Mockito.when(mockListItem.getMarker()).thenReturn(".");
        Mockito.when(mockNode.getBlocks()).thenReturn(Arrays.asList(mockListItem));

        nodesSinker.processNode(mockNode);

        assertThat(sinkWriter.toString()).isNotBlank();
    }

    private static StructuralNode mockNode(String nodeName) {
        StructuralNode mockNode = Mockito.mock(StructuralNode.class);
        Mockito.when(mockNode.getNodeName()).thenReturn(nodeName);
        return mockNode;
    }

    private static <T> T mockNode(String nodeName, Class<? extends StructuralNode> clazz) {
        StructuralNode mockNode = Mockito.mock(clazz);
        Mockito.when(mockNode.getNodeName()).thenReturn(nodeName);
        return (T) mockNode;
    }
}
