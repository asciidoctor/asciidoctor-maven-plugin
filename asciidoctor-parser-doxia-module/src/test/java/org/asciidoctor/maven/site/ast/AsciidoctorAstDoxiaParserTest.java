package org.asciidoctor.maven.site.ast;

import lombok.SneakyThrows;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;

import static org.asciidoctor.maven.site.ast.processors.test.ReflectionUtils.extractField;
import static org.asciidoctor.maven.site.ast.processors.test.TestNodeProcessorFactory.createSink;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AsciidoctorAstDoxiaParserTest {

    private static final String TEST_DOCS_PATH = "src/test/resources/";

    private Sink sink;
    private StringWriter sinkWriter;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        sink = createSink();
        sinkWriter = extractField(sink, "writer");
    }

    @Test
    public void should_convert_html_without_any_configuration() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "sample.asciidoc");

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser();

        parser.parse(new FileReader(srcAsciidoc), sink);

        assertThat(sinkWriter.toString())
                .isEqualTo("<h1>Document Title</h1><p>Preamble paragraph.</p>\n" +
                        "<h2><a name=\"Section_A\"></a>Section A</h2>\n" +
                        "<p><strong>Section A</strong> paragraph.</p>\n" +
                        "<h3><a name=\"Section_A_Subsection\"></a>Section A Subsection</h3>\n" +
                        "<p><strong>Section A</strong> <em>subsection</em> paragraph.</p>\n" +
                        "<h2><a name=\"Section_B\"></a>Section B</h2>\n" +
                        "<p><strong>Section B</strong> paragraph.</p>\n" +
                        "<ul>\n" +
                        "<li>Item 1</li>\n" +
                        "<li>Item 2</li>\n" +
                        "<li>Item 3</li></ul><div class=\"source\"><pre class=\"prettyprint\"><code>require 'asciidoctor'</code></pre></div>");
    }

    @Test
    public void should_convert_html_with_an_attribute() throws ParseException {
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

        parser.parse(new StringReader(source), sink);

        assertThat(sinkWriter.toString())
                .contains("<p>My attribute value is a_value.</p>");
    }

    @Test
    public void should_process_empty_selfclosing_XML_attributes() throws ParseException {
        final String source = sectionsSample();

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <attributes>\n" +
                        "      <sectnums/>\n" +
                        "    </attributes>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        parser.parse(new StringReader(source), sink);

        assertThat(sinkWriter.toString())
                .contains("</a>1. Section A</h2>")
                .contains("</a>1.1. Section A Subsection</h3>")
                .contains("</a>2. Section B</h2>")
                .contains("</a>2.1. Section B Subsection</h3>");
    }

    @Test
    public void should_process_config_with_sectnumlevels() throws ParseException {
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

        parser.parse(new StringReader(source), sink);

        assertThat(sinkWriter.toString())
                .contains("</a>1. Section A</h2>")
                .contains("</a>Section A Subsection</h3>")
                .contains("</a>2. Section B</h2>")
                .contains("</a>Section B Subsection</h3>");
    }

    @Test
    public void should_process_empty_value_XML_attributes() throws ParseException {
        final String source = sectionsSample();

        AsciidoctorAstDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <attributes>\n" +
                        "      <sectnums></sectnums>\n" +
                        "    </attributes>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        parser.parse(new StringReader(source), sink);

        assertThat(sinkWriter.toString())
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
    public void should_fail_when_logHandler_failIf_is_WARNING() {
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

    @SneakyThrows
    private javax.inject.Provider<MavenProject> createMavenProjectMock(String configuration) {
        MavenProject mockProject = Mockito.mock(MavenProject.class);
        when(mockProject.getBasedir())
                .thenReturn(new File("."));
        when(mockProject.getGoalConfiguration(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(configuration != null ? Xpp3DomBuilder.build(new StringReader(configuration)) : null);

        return () -> mockProject;
    }

    @SneakyThrows
    private AsciidoctorAstDoxiaParser mockAsciidoctorDoxiaParser() {
        AsciidoctorAstDoxiaParser parser = new AsciidoctorAstDoxiaParser();
        setVariableValueInObject(parser, "mavenProjectProvider", createMavenProjectMock(null));
        return mockAsciidoctorDoxiaParser(null);
    }

    @SneakyThrows
    private AsciidoctorAstDoxiaParser mockAsciidoctorDoxiaParser(String configuration) {
        AsciidoctorAstDoxiaParser parser = new AsciidoctorAstDoxiaParser();
        setVariableValueInObject(parser, "mavenProjectProvider", createMavenProjectMock(configuration));
        return parser;
    }
}
