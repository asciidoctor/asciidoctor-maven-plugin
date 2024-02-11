package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.doxia.sink.Sink;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.maven.site.parser.processors.test.Html.LIST_STYLE_TYPE_DECIMAL;
import static org.asciidoctor.maven.site.parser.processors.test.Html.dd;
import static org.asciidoctor.maven.site.parser.processors.test.Html.dt;
import static org.asciidoctor.maven.site.parser.processors.test.Html.italics;
import static org.asciidoctor.maven.site.parser.processors.test.Html.li;
import static org.asciidoctor.maven.site.parser.processors.test.Html.monospace;
import static org.asciidoctor.maven.site.parser.processors.test.Html.ol;
import static org.asciidoctor.maven.site.parser.processors.test.Html.strong;
import static org.asciidoctor.maven.site.parser.processors.test.Html.ul;
import static org.asciidoctor.maven.site.parser.processors.test.StringTestUtils.clean;
import static org.assertj.core.api.Assertions.assertThat;

@NodeProcessorTest(DescriptionListNodeProcessor.class)
class DescriptionListNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private Sink sink;
    private StringWriter sinkWriter;


    @BeforeEach
    void setup() {
        ListItemNodeProcessor listItemNodeProcessor = new ListItemNodeProcessor(sink);
        ((DescriptionListNodeProcessor) nodeProcessor).setItemNodeProcessor(listItemNodeProcessor);

        OrderedListNodeProcessor oListNodeProcessor = new OrderedListNodeProcessor(sink);
        oListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);

        UnorderedListNodeProcessor uListNodeProcessor = new UnorderedListNodeProcessor(sink);
        uListNodeProcessor.setItemNodeProcessor(listItemNodeProcessor);

        listItemNodeProcessor.setNodeProcessors(Arrays.asList(nodeProcessor, oListNodeProcessor, uListNodeProcessor));
    }

    @Test
    void should_convert_simple_list() {
        String content = buildDocumentWithSimpleList();

        String html = process(content);

        // TODO: document We are not adding additional <div> / <p>, unlike Asciidoctor
        assertThat(html)
            .isEqualTo("<dl>" +
                dt("CPU") + dd("The brain of the computer.") +
                dt("RAM") + dd("Temporarily stores information the CPU uses during operation.") +
                "</dl>");
    }

    @Test
    void should_convert_simple_list_with_formatting() {
        String content = buildDocumentWithSimpleListWithFormatting();

        String html = process(content);

        assertThat(html)
            .isEqualTo("<dl>" +
                dt(strong("CPU")) + dd("The brain of " + italics("the computer") + ".") +
                dt(monospace("RAM")) + dd(strong("Temporarily stores information") + " the CPU uses during operation.") +
                "</dl>");
    }

    @Test
    void should_convert_simple_list_with_nested_list() {
        String content = buildDocumentWithNestedLists();

        String html = process(content);

        assertThat(html)
            .isEqualTo("<dl>" +
                dt("Dairy") +
                "<dd>" +
                ul(li("Milk"), li("Eggs")) +
                "</dd>" +
                dt("Bakery") +
                "<dd>" +
                ol(LIST_STYLE_TYPE_DECIMAL, li("Bread")) +
                "</dd>" +
                "</dl>"
            );
    }

    @Test
    void should_convert_nested_description_lists() {
        String content = buildDocumentWithNestedDescriptionLists();

        String html = process(content);

        assertThat(html)
            .isEqualTo("<dl>" +
                dt("Operating Systems") +
                "<dd>" +
                "<dl>" +

                dt("Linux") +
                "<dd>" +
                ol(LIST_STYLE_TYPE_DECIMAL,
                    li("Fedora" + ul(li("Desktop"))),
                    li("Ubuntu" + ul(li("Desktop"), li("Server")))
                ) +
                "</dd>" +
                dt("BSD") +
                "<dd>" +
                ol(LIST_STYLE_TYPE_DECIMAL, li("FreeBSD"), li("NetBSD")) +
                "</dd>" +
                "</dl>" +

                "</dd>" +
                "</dl>"
            );
    }

    private static String buildDocumentWithSimpleList() {
        return "= Document tile\n\n"
            + "== Section\n\n"
            + "CPU:: The brain of the computer.\n"
            + "RAM:: Temporarily stores information the CPU uses during operation.\n";
    }

    private static String buildDocumentWithSimpleListWithFormatting() {
        return "= Document tile\n\n"
            + "== Section\n\n"
            + "*CPU*:: The brain of _the computer_.\n"
            + "`RAM`:: *Temporarily stores information* the CPU uses during operation.\n";
    }

    private static String buildDocumentWithNestedLists() {
        return "= Document tile\n\n"
            + "== Section\n\n"
            + "Dairy::\n"
            + "* Milk\n"
            + "* Eggs\n"
            + "Bakery::\n"
            + ". Bread\n";
    }

    private static String buildDocumentWithNestedDescriptionLists() {
        return "= Document tile\n\n"
            + "== Section\n\n"
            + "Operating Systems::\n"
            + "  Linux:::\n"
            + "    . Fedora\n"
            + "      * Desktop\n"
            + "    . Ubuntu\n"
            + "      * Desktop\n"
            + "      * Server\n"
            + "  BSD:::\n"
            + "    . FreeBSD\n"
            + "    . NetBSD\n";
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
            .findBy(Collections.singletonMap("context", ":dlist"))
            .get(0);

        nodeProcessor.process(node);

        return clean(sinkWriter.toString());
    }
}
