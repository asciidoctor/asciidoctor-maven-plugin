package org.asciidoctor.maven.site;

import lombok.SneakyThrows;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.impl.AbstractTextSink;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AsciidoctorDoxiaParserTest {

    private static final String TEST_DOCS_PATH = "src/test/resources/";

    @Test
    public void should_convert_html_without_any_configuration() throws FileNotFoundException, ParseException {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "sample.asciidoc");
        final Sink sink = createSinkMock();

        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser();

        // when
        parser.parse(new FileReader(srcAsciidoc), sink);

        // then
        assertThat(((TextProviderSink) sink).text)
                .contains("<h1>Document Title</h1>")
                .contains("<div class=\"ulist\">")
                .contains("<div class=\"listingblock\">")
                .contains("require 'asciidoctor'")
                .contains("<div class=\"title\">Note</div>");
    }

    @Test
    public void should_convert_html_with_an_attribute() throws FileNotFoundException, ParseException {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "sample.asciidoc");
        Reader reader = new FileReader(srcAsciidoc);

        Sink sink = createSinkMock();
        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <attributes>\n" +
                        "      <icons>font</icons>\n" +
                        "    </attributes>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        // when
        parser.parse(reader, sink);

        // then
        assertThat(((TextProviderSink) sink).text)
                .contains("<i class=\"fa icon-note\" title=\"Note\"></i>");
    }

    @Test
    public void should_convert_html_with_baseDir_option() throws FileNotFoundException, ParseException {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "main-document.adoc");
        final Sink sink = createSinkMock();

        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <baseDir>" + new File(srcAsciidoc.getParent()).getAbsolutePath() + "</baseDir>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        // when
        parser.parse(new FileReader(srcAsciidoc), sink);

        // then: 'include works'
        assertThat(((TextProviderSink) sink).text)
                .contains("<h1>Include test</h1>")
                .contains("println \"HelloWorld from Groovy on ${new Date()}\"");
    }

    @Test
    public void should_convert_html_with_relative_baseDir_option() throws FileNotFoundException, ParseException {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "main-document.adoc");
        final Sink sink = createSinkMock();

        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <baseDir>" + TEST_DOCS_PATH + "</baseDir>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        // when
        parser.parse(new FileReader(srcAsciidoc), sink);

        // then: 'include works'
        assertThat(((TextProviderSink) sink).text)
                .contains("<h1>Include test</h1>")
                .contains("println \"HelloWorld from Groovy on ${new Date()}\"");
    }

    @Test
    public void should_convert_html_with_templateDir_option() throws FileNotFoundException, ParseException {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "sample.asciidoc");
        final Sink sink = createSinkMock();

        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <templateDirs>\n" +
                        "      <dir>" + TEST_DOCS_PATH + "/templates</dir>\n" +
                        "    </templateDirs>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        // when
        parser.parse(new FileReader(srcAsciidoc), sink);

        // then
        assertThat(((TextProviderSink) sink).text)
                .contains("<h1>Document Title</h1>")
                .contains("<p class=\"custom-template \">");
    }

    @Test
    public void should_convert_html_with_attributes_and_baseDir_option() throws FileNotFoundException, ParseException {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "main-document.adoc");
        final Sink sink = createSinkMock();

        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <baseDir>" + new File(srcAsciidoc.getParent()).getAbsolutePath() + "</baseDir>\n" +
                        "    <attributes>\n" +
                        "      <sectnums></sectnums>\n" +
                        "      <icons>font</icons>\n" +
                        "      <my-label>Hello World!!</my-label>\n" +
                        "    </attributes>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        // when
        parser.parse(new FileReader(srcAsciidoc), sink);

        // then
        assertThat(((TextProviderSink) sink).text)
                .contains("<h1>Include test</h1>")
                .contains("<h2 id=\"code\">1. Code</h2>")
                .contains("<h2 id=\"optional_section\">2. Optional section</h2>")
                .contains("println \"HelloWorld from Groovy on ${new Date()}\"")
                .contains("Hello World!!")
                .contains("<i class=\"fa icon-tip\" title=\"Tip\"></i>");
    }

    @Test
    public void should_process_empty_selfclosing_XML_attributes() throws FileNotFoundException, ParseException {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "sample.asciidoc");
        final Sink sink = createSinkMock();

        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <attributes>\n" +
                        "      <sectnums/>\n" +
                        "    </attributes>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        // when
        parser.parse(new FileReader(srcAsciidoc), sink);

        // then
        assertThat(((TextProviderSink) sink).text)
                .contains("<h2 id=\"id_section_a\">1. Section A</h2>")
                .contains("<h3 id=\"id_section_a_subsection\">1.1. Section A Subsection</h3>");
    }

    @Test
    public void should_process_empty_value_XML_attributes() throws FileNotFoundException, ParseException {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "sample.asciidoc");
        final Sink sink = createSinkMock();

        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <attributes>\n" +
                        "      <sectnums></sectnums>\n" +
                        "    </attributes>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        // when
        parser.parse(new FileReader(srcAsciidoc), sink);

        // then
        assertThat(((TextProviderSink) sink).text)
                .contains("<h2 id=\"id_section_a\">1. Section A</h2>")
                .contains("<h3 id=\"id_section_a_subsection\">1.1. Section A Subsection</h3>");
    }

    @Test
    public void should_fail_when_logHandler_failIf_is_WARNING() {
        // given
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "errors/document-with-missing-include.adoc");
        final Sink sink = createSinkMock();

        AsciidoctorDoxiaParser parser = mockAsciidoctorDoxiaParser(
                "<configuration>\n" +
                        "  <asciidoc>\n" +
                        "    <logHandler>\n" +
                        "      <!-- <outputToConsole>false</outputToConsole> -->\n" +
                        "      <failIf>\n" +
                        "        <severity>WARN</severity>\n" +
                        "      </failIf>\n" +
                        "    </logHandler>\n" +
                        "  </asciidoc>\n" +
                        "</configuration>");

        // when
        Throwable throwable = catchThrowable(() -> parser.parse(new FileReader(srcAsciidoc), sink));

        // then: 'issues with WARN and ERROR are returned'
        assertThat(throwable)
                .isInstanceOf(org.apache.maven.doxia.parser.ParseException.class)
                .hasMessageContaining("Found 4 issue(s) of severity WARN or higher during conversion");
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
    private AsciidoctorDoxiaParser mockAsciidoctorDoxiaParser() {
        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser();
        setVariableValueInObject(parser, "mavenProjectProvider", createMavenProjectMock(null));
        return parser;
    }

    @SneakyThrows
    private AsciidoctorDoxiaParser mockAsciidoctorDoxiaParser(String configuration) {
        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser();
        setVariableValueInObject(parser, "mavenProjectProvider", createMavenProjectMock(configuration));
        return parser;
    }

    private Sink createSinkMock() {
        return new TextProviderSink();
    }

    class TextProviderSink extends AbstractTextSink {
        public String text;

        @Override
        public void rawText(String text) {
            this.text = text;
        }
    }
}
