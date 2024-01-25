package org.asciidoctor.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.asciidoctor.maven.io.ConsoleHolder;
import org.asciidoctor.maven.log.FailIf;
import org.asciidoctor.maven.log.LogHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.asciidoctor.log.Severity.ERROR;
import static org.asciidoctor.log.Severity.WARN;
import static org.asciidoctor.maven.test.TestUtils.mockAsciidoctorMojo;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


class AsciidoctorMojoLogHandlerTest {

    private static final String DEFAULT_SOURCE_DIRECTORY = "target/test-classes/src/asciidoctor";

    @Test
    void should_not_fail_when_logHandler_is_not_set() throws MojoFailureException, MojoExecutionException {
        // setup
        String sourceDocument = "errors/document-with-missing-include.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        mojo.execute();

        // then: process completes but the document contains errors
        AsciidoctorAsserter.assertThat(outputDir, "document-with-missing-include.html")
                .contains("<p>Unresolved directive in document-with-missing-include.adoc - include::unexistingdoc.adoc[]</p>");
    }

    @Test
    public void should_show_Asciidoctor_messages_as_info_by_default() throws MojoFailureException, MojoExecutionException {
        // setup
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        String sourceDocument = "errors/document-with-missing-include.adoc";

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        mojo.execute();

        // then
        List<String> asciidoctorMessages = Arrays.stream(consoleHolder.getOutput().split("\n"))
                .filter(line -> line.contains("asciidoctor:"))
                .collect(Collectors.toList());

        assertThat(asciidoctorMessages)
                .hasSize(4);
        assertThat(asciidoctorMessages.get(0))
                .contains(fixOsSeparator("[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 3: include file not found:"));
        assertThat(asciidoctorMessages.get(1))
                .contains(fixOsSeparator("[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 5: include file not found:"));
        assertThat(asciidoctorMessages.get(2))
                .contains(fixOsSeparator("[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 9: include file not found:"));
        assertThat(asciidoctorMessages.get(3))
                .contains(fixOsSeparator("[info] asciidoctor: WARN: errors/document-with-missing-include.adoc: line 25: no callout found for <1>"));

        // cleanup
        consoleHolder.release();
    }

    @Test
    public void should_not_fail_and_log_errors_as_INFO_when_outputToConsole_is_set() throws MojoFailureException, MojoExecutionException {
        // setup
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        String sourceDocument = "errors/document-with-missing-include.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        final LogHandler logHandler = new LogHandler();
        logHandler.setOutputToConsole(true);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        mojo.execute();

        // then: output file exists & shows include error
        AsciidoctorAsserter.assertThat(outputDir, "document-with-missing-include.html")
                .contains("<p>Unresolved directive in document-with-missing-include.adoc - include::unexistingdoc.adoc[]</p>");

        // and: all messages (ERR & WARN) are logged as info
        List<String> outputLines = getOutputInfoLines(consoleHolder);
        assertThat(outputLines)
                .hasSize(4);
        assertThat(outputLines.get(0))
                .startsWith(fixOsSeparator("[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 3: include file not found:"));
        assertThat(outputLines.get(1))
                .startsWith(fixOsSeparator("[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 5: include file not found:"));
        assertThat(outputLines.get(2))
                .startsWith(fixOsSeparator("[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 9: include file not found:"));
        assertThat(outputLines.get(3))
                .startsWith(fixOsSeparator("[info] asciidoctor: WARN: errors/document-with-missing-include.adoc: line 25: no callout found for <1>"));

        // cleanup
        consoleHolder.release();
    }

    @Disabled
    @Test
    void should_not_fail_and_log_errors_as_INFO_when_outputToConsole_is_set_and_doc_contains_messages_without_cursor_and_verbose_is_enabled() throws MojoFailureException, MojoExecutionException {
        // setup
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        String sourceDocument = "errors/document-with-invalid-reference.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        final LogHandler logHandler = new LogHandler();
        logHandler.setOutputToConsole(true);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        mojo.attributes = Map.of("toc", null);
        mojo.enableVerbose = true;
        mojo.execute();

        // then: output file exists & shows include error
        assertThat(new File(outputDir, "document-with-invalid-reference.html"))
                .isNotEmpty();

        // and: all messages (WARN) are logged as info
        List<String> outputLines = getOutputInfoLines(consoleHolder);
        assertThat(outputLines)
                .containsExactly(
                        "[info] asciidoctor: WARN: invalid reference: ../path/some-file.adoc",
                        "[info] asciidoctor: WARN: invalid reference: section-id"
                );

        // cleanup
        consoleHolder.release();

    }

    @Disabled
    @Test
    void should_not_fail_and_log_verbose_errors_when_gempath_is_set() throws MojoFailureException, MojoExecutionException {
        // setup
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        String sourceDocument = "errors/document-with-invalid-reference.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        final LogHandler logHandler = new LogHandler();
        logHandler.setOutputToConsole(true);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        mojo.enableVerbose = true;
        mojo.gemPath = System.getProperty("java.io.tmpdir");
        mojo.execute();

        // then: output file exists & shows include error
        assertThat(new File(outputDir, "document-with-invalid-reference.html"))
                .isNotEmpty();

        // and: all messages (WARN) are logged as info
        assertThat(getOutputInfoLines(consoleHolder))
                .containsExactly(
                        "[info] asciidoctor: WARN: invalid reference: ../path/some-file.adoc",
                        "[info] asciidoctor: WARN: invalid reference: section-id"
                );

        // cleanup
        consoleHolder.release();
    }

    @Test
    void should_fail_when_logHandler_failIf_is_WARNING() {
        // setup
        String sourceDocument = "errors/document-with-missing-include.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        final LogHandler logHandler = new LogHandler();
        FailIf failIf = new FailIf();
        failIf.setSeverity(WARN);
        logHandler.setFailIf(failIf);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        Throwable throwable = catchThrowable(mojo::execute);

        // then: issues with WARN and ERROR are returned
        assertThat(throwable)
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("Found 4 issue(s) of severity WARN or higher during conversion");
    }

    @Test
    void should_fail_when_logHandler_failIf_is_ERROR() {
        // setup
        String sourceDocument = "errors/document-with-missing-include.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        final LogHandler logHandler = new LogHandler();
        FailIf failIf = new FailIf();
        failIf.setSeverity(ERROR);
        logHandler.setFailIf(failIf);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        Throwable throwable = catchThrowable(mojo::execute);

        // then
        assertThat(throwable)
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("Found 3 issue(s) of severity ERROR or higher during conversion");
    }

    @Test
    void should_not_fail_if_containsText_does_not_match_any_message() throws MojoFailureException, MojoExecutionException {
        // setup
        String sourceDocument = "errors/document-with-missing-include.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        final LogHandler logHandler = new LogHandler();
        FailIf failIf = new FailIf();
        failIf.setContainsText("here is some random text");
        logHandler.setFailIf(failIf);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        mojo.execute();

        // then
        AsciidoctorAsserter.assertThat(outputDir, "document-with-missing-include.html")
                .contains("<p>Unresolved directive in document-with-missing-include.adoc - include::unexistingdoc.adoc[]</p>");
    }

    @Test
    void should_fail_when_containsText_matches() {
        // setup
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        String sourceDocument = "errors/document-with-missing-include.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        final LogHandler logHandler = new LogHandler();
        FailIf failIf = new FailIf();
        failIf.setContainsText("include file not found");
        logHandler.setFailIf(failIf);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        Throwable throwable = catchThrowable(mojo::execute);

        // then
        assertThat(throwable)
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("Found 3 issue(s) containing 'include file not found'");
        assertThat(new File(outputDir, "document-with-missing-include.html"))
                .exists();

        // and: all messages (ERR & WARN) are logged as error
        List<String> errorLines = getErrorLines(consoleHolder);
        assertThat(errorLines)
                .hasSize(3);
        assertThat(errorLines.get(0))
                .contains(fixOsSeparator("[error] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 3: include file not found:"));
        assertThat(errorLines.get(1))
                .contains(fixOsSeparator("[error] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 5: include file not found:"));
        assertThat(errorLines.get(2))
                .contains(fixOsSeparator("[error] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 9: include file not found:"));

        // cleanup
        consoleHolder.release();
    }

    @Test
    void should_fail_and_filter_errors_that_match_both_severity_and_text() {
        // setup
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        String sourceDocument = "errors/document-with-missing-include.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        final LogHandler logHandler = new LogHandler();
        FailIf failIf = new FailIf();
        failIf.setSeverity(WARN);
        failIf.setContainsText("no");
        logHandler.setFailIf(failIf);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        Throwable throwable = catchThrowable(mojo::execute);

        // then
        assertThat(new File(outputDir, "document-with-missing-include.html"))
                .isNotEmpty();
        assertThat(throwable)
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("Found 4 issue(s) matching severity WARN or higher and text 'no'");
        assertThat(consoleHolder.getError())
                .contains(fixOsSeparator("[error] asciidoctor: WARN: errors/document-with-missing-include.adoc: line 25: no callout found for <1>"));

        // cleanup
        consoleHolder.release();
    }

    // `asciidoctor` JUL logger inherits a ConsoleHandler that needs to be disabled
    // to avoid redundant messages in error channel
    @Test
    void should_not_print_default_AsciidoctorJ_messages() throws MojoFailureException, MojoExecutionException {
        // setup
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        String sourceDocument = "errors/document-with-missing-include.adoc";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("logHandler");
        LogHandler logHandler = new LogHandler();
        logHandler.setOutputToConsole(true);

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = sourceDocument;
        mojo.outputDirectory = outputDir;
        mojo.standalone = true;
        mojo.execute();

        // then: output file exists & shows include error
        AsciidoctorAsserter.assertThat(new File(outputDir, "document-with-missing-include.html"))
                .contains("<p>Unresolved directive in document-with-missing-include.adoc - include::unexistingdoc.adoc[]</p>");
        assertThat(consoleHolder.getError())
                .isEmpty();

        // cleanup
        consoleHolder.release();
    }

    private List<String> getOutputInfoLines(ConsoleHolder consoleHolder) {
        final String lineSeparator = lineSeparator();
        return Arrays.stream(consoleHolder.getOutput().split(lineSeparator))
                .filter(line -> line.startsWith("[info] asciidoctor"))
                .collect(Collectors.toList());
    }

    private List<String> getErrorLines(ConsoleHolder consoleHolder) {
        return Arrays.stream(consoleHolder.getError().split(lineSeparator()))
                .collect(Collectors.toList());
    }

    private String fixOsSeparator(String text) {
        return isUnix() ? text : text.replaceAll("/", "\\\\");
    }

    private boolean isUnix() {
        return File.separatorChar == '/';
    }

    private String lineSeparator() {
        return isUnix() ? "\n" : "\r\n";
    }
}
