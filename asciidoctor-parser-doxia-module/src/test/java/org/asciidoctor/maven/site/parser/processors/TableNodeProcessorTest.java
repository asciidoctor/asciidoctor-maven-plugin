package org.asciidoctor.maven.site.parser.processors;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.asciidoctor.maven.site.parser.processors.test.NodeProcessorTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.asciidoctor.maven.site.parser.processors.TableNodeProcessorTest.DocumentBuilder.CaptionOptions.*;
import static org.asciidoctor.maven.site.parser.processors.TableNodeProcessorTest.DocumentBuilder.documentWithTable;
import static org.asciidoctor.maven.site.parser.processors.test.Html.Attributes.STYLE;
import static org.asciidoctor.maven.site.parser.processors.test.Html.td;
import static org.asciidoctor.maven.site.parser.processors.test.Html.tr;
import static org.asciidoctor.maven.site.parser.processors.test.StringTestUtils.removeLineBreaks;
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

    private static final String CAPTION_STYLE = "color: #7a2518; margin-bottom: .25em; text-align: left";

    private Asciidoctor asciidoctor;
    private NodeProcessor nodeProcessor;
    private StringWriter sinkWriter;

    @Test
    void should_convert_empty_table() {
        String content = "= Document tile\n" +
            "\n" +
            "\n" +
            "== Section\n" +
            "|===\n" +
            "|===";

        String html = process(content);

        // Header for now is just first row with class=a
        assertThat(html)
            .isEmpty();
    }

    @Test
    void should_convert_table_with_header() {
        String content = documentWithTable(true, noCaption, emptyList());

        String html = process(content);

        // Header for now is just first row with class=a
        assertThat(html)
            .isEqualTo("<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
                "<tr class=\"a\">" +
                "<th>Name</th>" +
                "<th>Language</th></tr>" +
                "<tr class=\"b\">" +
                td("JRuby", Map.of("style", "text-align: left;")) +
                td("Java") + "</tr>" +
                "<tr class=\"a\">" +
                "<td style=\"text-align: left;\">Rubinius</td>" +
                "<td>Ruby</td></tr></table>");
    }

    @Test
    void should_convert_table_without_header() {
        String content = documentWithTable(false, noCaption, emptyList());

        String html = process(content);

        assertThat(html)
            .isEqualTo(removeLineBreaks("<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
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
    void should_convert_table_with_caption_and_title() {
        String content = documentWithTable(false, simpleCaption, emptyList());

        String html = process(content);

        assertThat(html)
            .isEqualTo("<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
                "<caption style=\"" + CAPTION_STYLE + "\">Table 1. Table caption&#8230;&#8203;or title</caption>" +
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

    @Nested
    class WhenCellContains {

        @Test
        void formatted_text() {
            final String formattedContent = "This *is* _a_ simple `cell`";
            String content = documentWithTable(false, noCaption, List.of(formattedContent, "Something else"));

            String html = process(content);

            assertThat(html)
                .isEqualTo("<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
                    tr("a", td("JRuby", textAlignLeft()) + td("Java")) +
                    tr("b", td("Rubinius", textAlignLeft()) + td("Ruby")) +
                    tr("a",
                        td("This <strong>is</strong> <em>a</em> simple <code>cell</code>", textAlignLeft()) +
                            td("Something else")) +
                    "</table>");
        }

        @Test
        void links() {
            final String link = "https://docs.asciidoctor.org/";
            String content = documentWithTable(false, noCaption, List.of("With links " + link + ".", "Something else"));

            String html = process(content);

            assertThat(html)
                .isEqualTo("<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
                    tr("a", td("JRuby", textAlignLeft()) + td("Java")) +
                    tr("b", td("Rubinius", textAlignLeft()) + td("Ruby")) +
                    tr("a",
                        td("With links <a href=\"https://docs.asciidoctor.org/\" class=\"bare\">https://docs.asciidoctor.org/</a>.", textAlignLeft()) +
                            td("Something else")) +
                    "</table>");
        }

        @Test
        void inline_images() {
            final String inlineImage = "image:images/tiger.png[Kitty]";
            String content = documentWithTable(false, noCaption, List.of("Something first", "With inline (" + inlineImage + ") images."));

            String html = process(content);

            assertThat(html)
                .isEqualTo("<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
                    tr("a", td("JRuby", textAlignLeft()) + td("Java")) +
                    tr("b", td("Rubinius", textAlignLeft()) + td("Ruby")) +
                    tr("a",
                        td("Something first", textAlignLeft()) +
                            td("With inline (<span class=\"image\"><img src=\"images/tiger.png\" alt=\"Kitty\"></span>) images.")) +
                    "</table>");
        }

        private Map<String, String> textAlignLeft() {
            return Map.of(STYLE, "text-align: left;");
        }
    }

    private static String expectedNoLabelBeginning() {
        return "<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
            "<caption style=\"" + CAPTION_STYLE + "\">Table caption&#8230;&#8203;or title</caption>";
    }

    private static String expectedTableWithoutLabel() {
        return "<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
            "<caption style=\"" + CAPTION_STYLE + "\">Table caption&#8230;&#8203;or title</caption>" +
            "<tr class=\"a\">" +
            "<td style=\"text-align: left;\">JRuby</td>" +
            "<td>Java</td></tr>" +
            "<tr class=\"b\">" +
            "<td style=\"text-align: left;\">Rubinius</td>" +
            "<td>Ruby</td></tr></table>";
    }

    private static String expectedTableWithoutCaption() {
        return "<table class=\"bodyTable\" style=\"background: #FFFFFF\">" +
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

            final boolean includeTitle;
            final boolean disableCaptionsForTable;
            final boolean disableCaptionsGlobally;

            static final CaptionOptions noCaption = new CaptionOptions(false, false, false);
            static final CaptionOptions simpleCaption = new CaptionOptions(true, false, false);
            static final CaptionOptions disableLabelForTable = new CaptionOptions(true, true, false);
            static final CaptionOptions disableLabelsGlobally = new CaptionOptions(true, false, true);

            private CaptionOptions(boolean include, boolean disableForTable, boolean disableGlobally) {
                this.includeTitle = include;
                this.disableCaptionsForTable = disableForTable;
                this.disableCaptionsGlobally = disableGlobally;
            }

        }

        static String documentWithTable(boolean includeTableHeader, CaptionOptions captionOptions, List<String> additionalRow) {
            return "= Document tile\n" +
                (captionOptions.disableCaptionsGlobally ? ":table-caption!:\n" : "") +
                "\n\n" +
                "== Section\n" +
                (captionOptions.disableCaptionsForTable ? "[caption=]\n" : "") +
                (captionOptions.includeTitle ? ".Table caption...or title\n" : "") +
                "|===\n" +
                (includeTableHeader ? "|Name |Language\n\n" : "") +
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

        String string = sinkWriter.toString();
        return removeLineBreaks(string);
    }
}
