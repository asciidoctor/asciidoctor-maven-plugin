package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.asciidoctor.maven.site.parser.processors.TableNodeProcessorTest.DocumentBuilder.CaptionOptions.disableLabelForTable;
import static org.asciidoctor.maven.site.parser.processors.TableNodeProcessorTest.DocumentBuilder.CaptionOptions.disableLabelsGlobally;
import static org.asciidoctor.maven.site.parser.processors.TableNodeProcessorTest.DocumentBuilder.CaptionOptions.noCaption;
import static org.asciidoctor.maven.site.parser.processors.TableNodeProcessorTest.DocumentBuilder.CaptionOptions.simpleCaption;
import static org.asciidoctor.maven.site.parser.processors.TableNodeProcessorTest.DocumentBuilder.documentWithTable;
import static org.asciidoctor.maven.site.parser.processors.test.StringTestUtils.clean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * NOTE: HTML '<caption>' tag contains both what AsciiDoc calls 'label' and 'title'.
 * In AsciiDoc
 * - label is the prefix with numbering (eg. 'Table 1.').
 * - title is the descriptive text that users can add.
 * The tests refer to caption as the combination of both.
 */
@NodeProcessorTest(TableNodeProcessor.class)
class TableNodeProcessorTest {

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;


    @Test
    void should_convert_table_with_header() {
        String content = documentWithTable(true, noCaption, emptyList());

        String html = process(content);

        // Header for now is just first row with class=a
        assertThat(html)
                .isEqualTo("<table class=\"bodyTable\">" +
                        "<tr class=\"a\">" +
                        "<th>Name</th>" +
                        "<th>Language</th></tr>" +
                        "<tr class=\"b\">" +
                        "<td style=\"text-align: left;\">JRuby</td>" +
                        "<td>Java</td></tr>" +
                        "<tr class=\"a\">" +
                        "<td style=\"text-align: left;\">Rubinius</td>" +
                        "<td>Ruby</td></tr></table>");
    }

    @Test
    void should_convert_table_without_header() {
        String content = documentWithTable(false, noCaption, emptyList());

        String html = process(content);

        assertThat(html)
                .isEqualTo(clean("<table class=\"bodyTable\">" +
                        "<tr class=\"a\">" +
                        "<td style=\"text-align: left;\">JRuby</td>" +
                        "<td>Java</td></tr>" +
                        "<tr class=\"b\">" +
                        "<td style=\"text-align: left;\">Rubinius</td>" +
                        "<td>Ruby</td></tr></table>"));
    }

    @Test
    void should_convert_table_with_text_markup() {
        String content = documentWithTable(false, noCaption, Arrays.asList("*Opal*", "_JavaScript_"));

        String html = process(content);

        assertThat(html)
                .isEqualTo(expectedTableWithoutCaption());
    }

    @Test
    void should_convert_table_with_label_and_title() {
        String content = documentWithTable(false, simpleCaption, emptyList());

        String html = process(content);

        assertThat(html)
                .isEqualTo("<table class=\"bodyTable\"><caption>Table 1. Table caption&#8230;&#8203;or title</caption>" +
                        "<tr class=\"a\">" +
                        "<td style=\"text-align: left;\">JRuby</td>" +
                        "<td>Java</td></tr>" +
                        "<tr class=\"b\">" +
                        "<td style=\"text-align: left;\">Rubinius</td>" +
                        "<td>Ruby</td></tr></table>");
    }

    @Test
    void should_convert_table_with_label_disabled() {
        String content = documentWithTable(false, disableLabelForTable, emptyList());

        String html = process(content);

        assertThat(html)
                .startsWith(expectedNoLabelBeginning())
                .isEqualTo(expectedTableWithoutLabel());
    }

    @Test
    void should_convert_table_with_labels_disabled_globally() {
        String content = documentWithTable(false, disableLabelsGlobally, emptyList());

        String html = process(content);

        assertThat(html)
                .startsWith(expectedNoLabelBeginning())
                .isEqualTo(expectedTableWithoutLabel());
    }

    private static String expectedNoLabelBeginning() {
        return "<table class=\"bodyTable\"><caption>Table caption&#8230;&#8203;or title</caption>";
    }

    private static String expectedTableWithoutLabel() {
        return "<table class=\"bodyTable\"><caption>Table caption&#8230;&#8203;or title</caption>" +
                "<tr class=\"a\">" +
                "<td style=\"text-align: left;\">JRuby</td>" +
                "<td>Java</td></tr>" +
                "<tr class=\"b\">" +
                "<td style=\"text-align: left;\">Rubinius</td>" +
                "<td>Ruby</td></tr></table>";
    }

    private static String expectedTableWithoutCaption() {
        return "<table class=\"bodyTable\">" +
                "<tr class=\"a\">" +
                "<td style=\"text-align: left;\">JRuby</td>" +
                "<td>Java</td></tr>" +
                "<tr class=\"b\">" +
                "<td style=\"text-align: left;\">Rubinius</td>" +
                "<td>Ruby</td></tr>" +
                "<tr class=\"a\">" +
                "<td style=\"text-align: left;\"><strong>Opal</strong></td>" +
                "<td><em>JavaScript</em></td></tr></table>";
    }

    static class DocumentBuilder {

        static class CaptionOptions {

            final boolean include;
            final boolean disableForTable;
            final boolean disableGlobally;

            static final CaptionOptions noCaption = new CaptionOptions(false, false, false);
            static final CaptionOptions simpleCaption = new CaptionOptions(true, false, false);
            static final CaptionOptions disableLabelForTable = new CaptionOptions(true, true, false);
            static final CaptionOptions disableLabelsGlobally = new CaptionOptions(true, false, true);

            private CaptionOptions(boolean include, boolean disableForTable, boolean disableGlobally) {
                this.include = include;
                this.disableForTable = disableForTable;
                this.disableGlobally = disableGlobally;
            }

        }

        static String documentWithTable(boolean includeHeaderRow, CaptionOptions captionOptions, List<String> additionalRow) {
            return "= Document tile\n" +
                    (captionOptions.disableGlobally ? ":table-caption!:\n" : "") +
                    "\n" +
                    "== Section\n" +
                    (captionOptions.disableForTable ? "[caption=]\n" : "") +
                    (captionOptions.include ? ".Table caption...or title\n" : "") +
                    "|===\n" +
                    (includeHeaderRow ? "|Name |Language\n\n" : "") +
                    "|JRuby |Java\n" +
                    "|Rubinius |Ruby\n" +
                    (!additionalRow.isEmpty() ? additionalRow.stream().collect(Collectors.joining("|", " |", "")) : "") +
                    "|===";
        }
    }

    private String process(String content) {
        StructuralNode node = asciidoctor.load(content, Options.builder().build())
                .findBy(Collections.singletonMap("context", ":table"))
                .get(0);

        nodeProcessor.process(node);

        return clean(sinkWriter.toString());
    }
}
