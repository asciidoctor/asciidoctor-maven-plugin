package org.asciidoctor.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.asciidoctor.maven.AsciidoctorAsserter.assertThat;
import static org.asciidoctor.maven.test.TestUtils.ResourceBuilder.excludeAll;
import static org.asciidoctor.maven.test.TestUtils.mockAsciidoctorMojo;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;

/**
 * Opinionated tests to validate Asciidoctor behaviours in end-to-end scenarios.
 */
class AsciidoctorIntegrationTest {

    private static final String DEFAULT_SOURCE_DIRECTORY = "target/test-classes/src/asciidoctor";


    @Test
    void should_leave_references_when_missing_attribute_is_skip() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.attributes = Map.of("attribute-missing", "skip");
        mojo.sourceDocumentName = "attribute-missing.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "attribute-missing.html")
                .contains("Here is a line that has an attribute that is {missing}!");
    }

    @Test
    void should_remove_references_when_missing_attribute_is_drop() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.attributes = Map.of("attribute-missing", "drop");
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "attribute-missing.adoc";
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "attribute-missing.html")
                .contains("Here is a line that has an attribute that is !")
                .doesNotContain("{name}");
    }

    @Test
    void should_remove_line_with_references_when_missing_attribute_is_drop_line() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.attributes = Map.of("attribute-missing", "drop-line");
        mojo.sourceDocumentName = "attribute-missing.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "attribute-missing.html")
                .doesNotContain("Here is a line that has an attribute that is")
                .doesNotContain("{set: name!}");
    }

    @Test
    void should_remove_expression_when_attribute_undefined_is_drop() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.attributes = Map.of("attribute-undefined", "drop");
        mojo.sourceDocumentName = "attribute-undefined.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "attribute-undefined.html")
                .contains("Here is a line that has an attribute that is !")
                .doesNotContain("{set: name!}");
    }

    @Test
    void should_remove_line_when_attribute_undefined_is_drop_line() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.attributes = Map.of("attribute-undefined", "drop-line");
        mojo.sourceDocumentName = "attribute-undefined.adoc";
        mojo.outputDirectory = outputDir;
        mojo.sourceDirectory = srcDir;
        mojo.execute();

        // then
        assertThat(outputDir, "attribute-undefined.html")
                .doesNotContain("Here is a line that has an attribute that is")
                .doesNotContain("{set: name!}");
    }

    @Test
    void should_apply_code_highlighting_with_pygments_coderay() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor");
        File outputDir = newOutputTestDirectory("sourceHighlighting-coderay");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.attributes = Map.of("source-highlighter", "coderay");
        mojo.sourceDocumentName = "main-document.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "main-document.html")
                .contains("<pre class=\"CodeRay highlight\">");
    }

    @Test
    void should_apply_code_highlighting_with_pygments_highlightjs() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor");
        File outputDir = newOutputTestDirectory("sourceHighlighting-highlightjs");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.attributes = Map.of("source-highlighter", "highlight.js");
        mojo.sourceDocumentName = "main-document.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "main-document.html")
                .contains("<pre class=\"highlightjs highlight\">")
                .contains("if (!hljs.initHighlighting.called) {");
    }

    @Test
    void should_apply_code_highlighting_with_prettify() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor");
        File outputDir = newOutputTestDirectory("sourceHighlighting-prettify");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.attributes = Map.of("source-highlighter", "prettify");
        mojo.sourceDocumentName = "main-document.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "main-document.html")
                .contains("<pre class=\"prettyprint highlight\">")
                .contains("prettify");
    }

    @Disabled("Not supported in Asciidoctorj (gem not embedded)")
    @Test
    void should_apply_code_highlighting_with_pygments() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor");
        File outputDir = newOutputTestDirectory("sourceHighlighting-pygments");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.attributes = Map.of(
                "source-highlighter", "pygments",
                "pygments-style", "monokai",
                "pygments-linenums-mode", "inline");
        mojo.sourceDocumentName = "main-document.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "main-document.html")
                .contains("<pre class=\"pygments highlight\">");
    }

    @Test
    void should_apply_code_highlighting_when_set_in_document_header() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor");
        File outputDir = newOutputTestDirectory("sourceHighlighting-header");
        String documentName = "sample-with-source-highlighting";

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.sourceDocumentName = documentName + ".adoc";
        mojo.resources = excludeAll();
        mojo.execute();

        // then
        assertThat(outputDir, documentName + ".html")
                .contains("<pre class=\"CodeRay highlight\">");
    }

    @Test
    void should_not_add_CSS_when_code_highlighting_value_is_not_valid() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor");
        File outputDir = newOutputTestDirectory("sourceHighlighting-nonExistent");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.attributes = Map.of("source-highlighter", "nonExistent");
        mojo.sourceDocumentName = "main-document.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "main-document.html")
                .containsOnlyOnce("<style>");
    }

    @Test
    void should_include_local_source_file() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("include");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDocumentName = "main-document.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "main-document.html")
                .contains("This is the parent document")
                .contains("This is an included file.");
    }

    @Test
    void should_include_github_files_when_allowUriRead_is_true() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("remote-includes");
        String documentName = "github-include.adoc";

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.sourceDocumentName = documentName;
        mojo.attributes = Map.of("allow-uri-read", "true");
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.resources = excludeAll();
        mojo.execute();

        // then
        assertThat(outputDir, "github-include.html")
                .contains("modelVersion");
    }

    @Test
    void should_not_include_github_files_when_allowUriRead_is_false() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("remote-includes");
        String documentName = "github-include.adoc";

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.attributes = Map.of("allow-uri-read", "false");
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = documentName;
        mojo.outputDirectory = outputDir;
        mojo.resources = excludeAll();
        mojo.execute();

        // then
        assertThat(outputDir, "github-include.html")
                .contains("link:https://raw.githubusercontent.com/cometd/cometd/4.0.x/pom.xml[role=include]");
    }

    @Test
    void should_add_docinfo_contents_and_not_copy_them_as_resources() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "sample-with-doc-info.asciidoc";
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        Assertions.assertThat(outputDir.list())
                .contains("sample-with-doc-info.html")
                .doesNotContain("sample-with-doc-info-docinfo.html")
                .doesNotContain("sample-with-doc-info-docinfo-footer.html")
                .doesNotContain("sample-with-doc-info-docinfo.xml")
                .doesNotContain("sample-with-doc-info-docinfo-footer.xml");

        assertThat(outputDir, "sample-with-doc-info.html")
                .contains("This is the sample-with-doc-info file.")
                .contains("This is the docinfo html file.")
                .contains("This is the docinfo html footer.");
    }

    @Test
    void should_honor_doctype_set_in_document() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "book.adoc";
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        mojo.attributes = Map.of("linkcss", "", "copycss!", "");
        mojo.execute();

        // then
        assertThat(outputDir, "book.html")
                .contains("<body class=\"book\">");
    }
}
