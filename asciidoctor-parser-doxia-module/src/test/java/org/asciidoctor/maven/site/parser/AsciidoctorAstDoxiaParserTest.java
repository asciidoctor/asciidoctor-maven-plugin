package org.asciidoctor.maven.site.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import lombok.SneakyThrows;
import org.apache.maven.doxia.parser.AbstractTextParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.maven.site.LogHandlerFactory;
import org.asciidoctor.maven.site.SiteBaseDirResolver;
import org.asciidoctor.maven.site.SiteConversionConfigurationParser;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.asciidoctor.maven.site.parser.AsciidoctorAstDoxiaParser.ROLE_HINT;
import static org.asciidoctor.maven.site.parser.AsciidoctorAstDoxiaParserTest.TestMocks.mockAsciidoctorDoxiaParser;
import static org.asciidoctor.maven.site.parser.processors.test.ReflectionUtils.extractField;
import static org.asciidoctor.maven.site.parser.processors.test.StringTestUtils.removeLineBreaks;
import static org.asciidoctor.maven.site.parser.processors.test.TestNodeProcessorFactory.createSink;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AsciidoctorAstDoxiaParserTest {

    private static final String TEST_DOCS_PATH = "src/test/resources/";

    private Sink sink;
    private StringWriter sinkWriter;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        sink = createSink();
        sinkWriter = extractField(sink, "writer");
    }

    @Test
    void should_convert_html_without_any_configuration() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "sample.asciidoc");

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser();

        String result = parse(parser, srcAsciidoc);

        assertThat(result)
            .isEqualTo("<h1>Document Title</h1><p>Preamble paragraph.</p>" +
                "<div>" +
                "<h2><a id=\"id_section_a\"></a>Section A</h2>" +
                "<p><strong>Section A</strong> paragraph.</p>" +
                "<div>" +
                "<h3><a id=\"id_section_a_subsection\"></a>Section A Subsection</h3>" +
                "<p><strong>Section A</strong> 'subsection' paragraph.</p>" +
                "</div>" +
                "</div>" +
                "<div>" +
                "<h2><a id=\"id_section_b\"></a>Section B</h2>" +
                "<p><strong>Section B</strong> paragraph.</p>" +
                "<ul>" +
                "<li>Item 1</li>" +
                "<li>Item 2</li>" +
                "<li>Item 3</li></ul>" +
                "<div class=\"source\"><pre class=\"prettyprint\"><code>require 'asciidoctor'</code></pre></div>" +
                "</div>");
    }

    @Test
    void should_convert_html_with_an_attribute() throws ParseException {
        final String source = "= Document Title\n\n" +
            "== Section A\n\n" +
            "My attribute value is {custom-attribute}.\n";

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <custom-attribute>a_value</custom-attribute>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        String result = parse(parser, source);

        assertThat(result)
            .contains("<p>My attribute value is a_value.</p>");
    }

    @Test
    void should_convert_html_in_locale_path() throws IOException, ParseException {
        final String localeSourceDir = TEST_DOCS_PATH + "with-locale/";
        final File srcAsciidoc = new File(localeSourceDir + "en/" + ROLE_HINT, "sample.adoc");

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <siteDirectory>" + localeSourceDir + "</siteDirectory>\n" +
                "  <locales>en</locales>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <icons>font</icons>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        String result = parse(parser, srcAsciidoc);

        assertThat(result)
            .contains("This has been included");
    }

    @Test
    void should_process_empty_selfclosing_XML_attributes() throws ParseException {
        final String source = sectionsSample();

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <sectnums/>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        String result = parse(parser, source);

        assertThat(result)
            .contains("</a>1. Section A</h2>")
            .contains("</a>1.1. Section A Subsection</h3>")
            .contains("</a>2. Section B</h2>")
            .contains("</a>2.1. Section B Subsection</h3>");
    }

    @Test
    void should_process_config_with_sectnumlevels() throws ParseException {
        final String source = sectionsSample();

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <sectnums/>\n" +
                "      <sectnumlevels>1</sectnumlevels>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        String result = parse(parser, source);

        assertThat(result)
            .contains("</a>1. Section A</h2>")
            .contains("</a>Section A Subsection</h3>")
            .contains("</a>2. Section B</h2>")
            .contains("</a>Section B Subsection</h3>");
    }

    @Test
    void should_process_empty_value_XML_attributes() throws ParseException {
        final String source = sectionsSample();

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <sectnums></sectnums>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        String result = parse(parser, source);

        assertThat(result)
            .contains("</a>1. Section A</h2>")
            .contains("</a>1.1. Section A Subsection</h3>")
            .contains("</a>2. Section B</h2>")
            .contains("</a>2.1. Section B Subsection</h3>");
    }

    private static String sectionsSample() {
        return "= Document Title\n\n" +
            "== Section A\n\n" +
            "Section A paragraph.\n\n" +
            "=== Section A Subsection\n\n" +
            "Section A 'subsection' paragraph.\n\n" +
            "== Section B\n\n" +
            "*Section B* paragraph.\n\n" +
            "=== Section B Subsection\n\n" +
            "Section B 'subsection' paragraph.\n\n";
    }

    @Test
    void should_fail_when_logHandler_failIf_is_WARNING() {
        final String source = "= My Document\n\n" +
            "include::unexistingdoc.adoc[]\n\n" +
            "include::unexistingdoc.adoc[]";

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <logHandler>\n" +
                "      <failIf>\n" +
                "        <severity>WARN</severity>\n" +
                "      </failIf>\n" +
                "    </logHandler>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        Throwable throwable = catchThrowable(() -> parser.parse(new StringReader(source), sink));

        // 'issues with WARN and ERROR are returned'
        assertThat(throwable)
            .isInstanceOf(ParseException.class)
            .hasMessageContaining("Found 2 issue(s) of severity WARN or higher during conversion");
    }

    private String parse(AbstractTextParser parser, File source) throws FileNotFoundException, ParseException {
        parser.parse(new FileReader(source), sink);
        return removeLineBreaks(sinkWriter.toString());
    }

    private String parse(AbstractTextParser parser, String source) throws ParseException {
        parser.parse(new StringReader(source), sink);
        return removeLineBreaks(sinkWriter.toString());
    }

    static class TestMocks {

        @SneakyThrows
        static MavenProject createMockMavenProject(String configuration) {
            MavenProject mockProject = Mockito.mock(MavenProject.class);
            when(mockProject.getBasedir())
                .thenReturn(new File("."));
            when(mockProject.getGoalConfiguration(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(configuration != null ? Xpp3DomBuilder.build(new StringReader(configuration)) : null);

            return mockProject;
        }

        @SneakyThrows
        static AsciidoctorAstDoxiaParser mockAsciidoctorDoxiaParser() {
            return mockAsciidoctorDoxiaParser(null);
        }

        @SneakyThrows
        static AsciidoctorAstDoxiaParser mockAsciidoctorDoxiaParser(String configuration) {
            return new AsciidoctorAstDoxiaParser(
                createMockMavenProject(configuration),
                new SiteConversionConfigurationParser(new SiteBaseDirResolver()),
                new LogHandlerFactory()
            );
        }
    }
}
