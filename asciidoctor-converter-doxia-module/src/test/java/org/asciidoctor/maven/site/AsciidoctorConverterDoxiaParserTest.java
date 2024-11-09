package org.asciidoctor.maven.site;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import lombok.SneakyThrows;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.impl.AbstractTextSink;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.asciidoctor.maven.site.AsciidoctorConverterDoxiaParser.ROLE_HINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AsciidoctorConverterDoxiaParserTest {

    private static final String TEST_DOCS_PATH = "src/test/resources/";
    private static final String SAMPLE_ASCIIDOC = "sample.asciidoc";

    @Test
    void should_convert_html_without_any_configuration() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, SAMPLE_ASCIIDOC);
        final Sink sink = createSinkMock();

        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser();

        parser.parse(new FileReader(srcAsciidoc), sink);

        assertThat(((TextProviderSink) sink).text)
            .contains("<h1>Document Title</h1>")
            .contains("<div class=\"ulist\">")
            .contains("<div class=\"listingblock\">")
            .contains("require 'asciidoctor'")
            .contains("<div class=\"title\">Note</div>");
    }

    @Test
    void should_convert_html_with_an_attribute() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, SAMPLE_ASCIIDOC);
        Reader reader = new FileReader(srcAsciidoc);

        Sink sink = createSinkMock();
        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <icons>font</icons>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        parser.parse(reader, sink);

        assertThat(((TextProviderSink) sink).text)
            .contains("<i class=\"fa icon-note\" title=\"Note\"></i>");
    }

    @Test
    void should_convert_html_with_baseDir_option() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "main-document.adoc");
        final Sink sink = createSinkMock();

        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <baseDir>" + new File(srcAsciidoc.getParent()).getAbsolutePath() + "</baseDir>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        parser.parse(new FileReader(srcAsciidoc), sink);

        // 'include works'
        assertThat(((TextProviderSink) sink).text)
            .contains("<h1>Include test</h1>")
            .contains("println \"HelloWorld from Groovy on ${new Date()}\"");
    }

    @Test
    void should_convert_html_with_relative_baseDir_option() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "main-document.adoc");
        final Sink sink = createSinkMock();

        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <baseDir>" + TEST_DOCS_PATH + "</baseDir>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        parser.parse(new FileReader(srcAsciidoc), sink);

        // 'include works'
        assertThat(((TextProviderSink) sink).text)
            .contains("<h1>Include test</h1>")
            .contains("println \"HelloWorld from Groovy on ${new Date()}\"");
    }

    @Test
    void should_convert_html_with_templateDir_option() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, SAMPLE_ASCIIDOC);
        final Sink sink = createSinkMock();

        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <templateDirs>\n" +
                "      <dir>" + TEST_DOCS_PATH + "/templates</dir>\n" +
                "    </templateDirs>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        parser.parse(new FileReader(srcAsciidoc), sink);

        assertThat(((TextProviderSink) sink).text)
            .contains("<h1>Document Title</h1>")
            .contains("<p class=\"custom-template \">");
    }

    @Test
    void should_convert_html_with_attributes_and_baseDir_option() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "main-document.adoc");
        final Sink sink = createSinkMock();

        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
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

        parser.parse(new FileReader(srcAsciidoc), sink);

        assertThat(((TextProviderSink) sink).text)
            .contains("<h1>Include test</h1>")
            .contains("<h2 id=\"code\">1. Code</h2>")
            .contains("<h2 id=\"optional_section\">2. Optional section</h2>")
            .contains("println \"HelloWorld from Groovy on ${new Date()}\"")
            .contains("Hello World!!")
            .contains("<i class=\"fa icon-tip\" title=\"Tip\"></i>");
    }

    @Test
    void should_convert_html_in_locale_path() throws IOException, ParseException {
        final String localeSourceDir = TEST_DOCS_PATH + "with-locale/";
        final File srcAsciidoc = new File(localeSourceDir + "en/" + ROLE_HINT, "sample.adoc");
        Reader reader = new FileReader(srcAsciidoc);

        Sink sink = createSinkMock();
        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <siteDirectory>" + localeSourceDir + "</siteDirectory>\n" +
                "  <locales>en</locales>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <icons>font</icons>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        parser.parse(reader, sink);

        assertThat(((TextProviderSink) sink).text)
            .contains("This has been included");
    }

    @Test
    void should_process_empty_selfclosing_XML_attributes() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, SAMPLE_ASCIIDOC);
        final Sink sink = createSinkMock();

        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <sectnums/>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        parser.parse(new FileReader(srcAsciidoc), sink);

        assertThat(((TextProviderSink) sink).text)
            .contains("<h2 id=\"id_section_a\">1. Section A</h2>")
            .contains("<h3 id=\"id_section_a_subsection\">1.1. Section A Subsection</h3>");
    }

    @Test
    void should_process_empty_value_XML_attributes() throws FileNotFoundException, ParseException {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, SAMPLE_ASCIIDOC);
        final Sink sink = createSinkMock();

        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
            "<configuration>\n" +
                "  <asciidoc>\n" +
                "    <attributes>\n" +
                "      <sectnums></sectnums>\n" +
                "    </attributes>\n" +
                "  </asciidoc>\n" +
                "</configuration>");

        parser.parse(new FileReader(srcAsciidoc), sink);

        assertThat(((TextProviderSink) sink).text)
            .contains("<h2 id=\"id_section_a\">1. Section A</h2>")
            .contains("<h3 id=\"id_section_a_subsection\">1.1. Section A Subsection</h3>");
    }

    @Test
    void should_fail_when_logHandler_failIf_is_WARNING() {
        final File srcAsciidoc = new File(TEST_DOCS_PATH, "errors/document-with-missing-include.adoc");
        final Sink sink = createSinkMock();

        AsciidoctorConverterDoxiaParser parser = mockAsciidoctorDoxiaParser(
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

        Throwable throwable = catchThrowable(() -> parser.parse(new FileReader(srcAsciidoc), sink));

        // 'issues with WARN and ERROR are returned'
        assertThat(throwable)
            .isInstanceOf(org.apache.maven.doxia.parser.ParseException.class)
            .hasMessageContaining("Found 4 issue(s) of severity WARN or higher during conversion");
    }

    @SneakyThrows
    private MavenProject createMockMavenProject(String configuration) {
        MavenProject mockProject = Mockito.mock(MavenProject.class);
        when(mockProject.getBasedir())
            .thenReturn(new File("."));
        when(mockProject.getGoalConfiguration(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(configuration != null ? Xpp3DomBuilder.build(new StringReader(configuration)) : null);

        return mockProject;
    }

    @SneakyThrows
    private AsciidoctorConverterDoxiaParser mockAsciidoctorDoxiaParser() {
        return mockAsciidoctorDoxiaParser(null);
    }

    @SneakyThrows
    private AsciidoctorConverterDoxiaParser mockAsciidoctorDoxiaParser(String configuration) {
        return new AsciidoctorConverterDoxiaParser(
            createMockMavenProject(configuration),
            new SiteConversionConfigurationParser(new SiteBaseDirResolver()),
            new LogHandlerFactory(),
            new SiteConverterDecorator()
        );
    }

    private Sink createSinkMock() {
        return new TextProviderSink();
    }

    static class TextProviderSink extends AbstractTextSink {
        String text;

        @Override
        public void rawText(String text) {
            this.text = text;
        }
    }
}
