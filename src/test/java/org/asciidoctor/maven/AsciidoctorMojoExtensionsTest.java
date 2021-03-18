package org.asciidoctor.maven;

import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.asciidoctor.maven.extensions.ExtensionConfiguration;
import org.asciidoctor.maven.io.ConsoleHolder;
import org.asciidoctor.maven.test.processors.*;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.asciidoctor.maven.TestUtils.map;
import static org.asciidoctor.maven.TestUtils.mockAsciidoctorMojo;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Specific tests to validate usage of AsciidoctorJ extension in AsciidoctorMojo.
 *
 * Most of the examples have been directly adapted from the ones found in AsciidoctorJ
 * documentation (https://github.com/asciidoctor/asciidoctorj/blob/master/README.adoc)
 *
 * @author abelsromero
 */
public class AsciidoctorMojoExtensionsTest {

    private static final String SRC_DIR = "target/test-classes/src/asciidoctor/";

    @Test
    public void should_fail_when_extension_is_not_found_in_classpath() {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("preprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null, "linkcss!", "");
        mojo.extensions = Arrays.asList(extensionConfiguration("non.existent.Processor"));
        Throwable throwable = Assertions.catchThrowable(mojo::execute);
        // then
        assertThat(throwable)
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("not found in classpath");
        assertThat(outputDir)
                .isEmptyDirectory();
    }

    // This test is added to keep track of possible changes in the extension"s SPI
    @Test
    public void should_fail_when_extension_throws_an_uncaught_exception() {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("preprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null, "linkcss!", "");
        mojo.extensions = Arrays.asList(extensionConfiguration(FailingPreprocessor.class));
        Throwable throwable = Assertions.catchThrowable(mojo::execute);
        // then
        // since v 1.5.4 resources are copied before conversion, so some files remain
        assertThat(throwable)
                .isInstanceOf(RuntimeException.class);
        assertThat(outputDir)
                .isNotEmptyDirectory();
    }

    @Test
    public void should_register_and_run_Preprocessor() {

        String extensionClassName = "ChangeAttributeValuePreprocessor";
        String expectedMessage = "ChangeAttributeValuePreprocessor(Preprocessor) initialized";
        String executionMessage = "Processing ChangeAttributeValuePreprocessor";

        shouldRegisterAndRunExtension(extensionClassName, expectedMessage, executionMessage);
    }

    @Test
    public void should_register_and_run_Treeprocessor() {

        String extensionClassName = "DummyTreeprocessor";
        String expectedMessage = "DummyTreeprocessor(Treeprocessor) initialized";
        String executionMessage = "Processing DummyTreeprocessor";

        shouldRegisterAndRunExtension(extensionClassName, expectedMessage, executionMessage);
    }

    @Test
    public void should_register_and_run_PostProcessor() {

        String extensionClassName = "DummyPostprocessor";
        String expectedMessage = "DummyPostprocessor(Postprocessor) initialized";
        String executionMessage = "Processing DummyPostprocessor";

        shouldRegisterAndRunExtension(extensionClassName, expectedMessage, executionMessage);
    }

    @Test
    public void should_register_and_run_DocinfoProcessor() {

        String extensionClassName = "MetaDocinfoProcessor";
        String expectedMessage = "MetaDocinfoProcessor(DocinfoProcessor) initialized";
        String executionMessage = "Processing MetaDocinfoProcessor";

        shouldRegisterAndRunExtension(extensionClassName, expectedMessage, executionMessage);
    }

    @Test
    public void should_register_and_run_IncludeProcessor() {

        String extensionClassName = "UriIncludeProcessor";
        String expectedMessage = "UriIncludeProcessor(IncludeProcessor) initialized";
        String executionMessage = "Processing UriIncludeProcessor";

        shouldRegisterAndRunExtension(extensionClassName, expectedMessage, executionMessage);
    }

    @SneakyThrows
    private void shouldRegisterAndRunExtension(String extensionClassName, String initializationMessage, String executionMessage) {
        // given
        ConsoleHolder consoleHolder = ConsoleHolder.start();
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("preprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = singletonList(extensionConfiguration("org.asciidoctor.maven.test.processors." + extensionClassName));
        mojo.execute();
        // then
        assertThat(consoleHolder.getOutput())
                .contains(initializationMessage)
                .contains(executionMessage);
        // cleanup
        consoleHolder.release();
    }

    @Test
    public void should_convert_to_html_with_a_preprocessor() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("preprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = Arrays.asList(extensionConfiguration(ChangeAttributeValuePreprocessor.class));
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .isNotEmpty()
                .containsPattern("(" + ChangeAttributeValuePreprocessor.AUTHOR_NAME + "([\\s\\S])*){2}");
    }

    @Test
    public void should_convert_to_html_with_a_blockprocessor() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("blockprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = singletonList(extensionConfiguration(YellBlockProcessor.class, "yell"));
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .isNotEmpty()
                .contains("The time is now. Get a move on.".toUpperCase());
    }

    @Test
    public void should_convert_to_html_and_add_meta_tag_with_a_DocinfoProcessor() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("docinfoProcessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = singletonList(extensionConfiguration(MetaDocinfoProcessor.class, "yell"));
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .isNotEmpty()
                .contains("<meta name=\"author\" content=\"asciidoctor\">");
    }

    @Test
    public void should_convert_to_html_and_modify_output_with_a_BlockMacroProcessor() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("blockMacroProcessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = singletonList(extensionConfiguration(GistBlockMacroProcessor.class, "gist"));
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .isNotEmpty()
                .contains("<script src=\"https://gist.github.com/123456.js\"></script>");
    }

    @Test
    public void should_convert_to_html_and_modify_output_with_a_InlineMacroProcessor() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("inlineMacroProcessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = singletonList(extensionConfiguration(ManpageInlineMacroProcessor.class, "man"));
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .isNotEmpty()
                .contains("<p>See <a href=\"gittutorial.html\">gittutorial</a> to get started.</p>");
    }

    @Test
    public void should_convert_to_html_and_modify_output_with_an_IncludeProcessor() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("includeProcessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = singletonList(extensionConfiguration(UriIncludeProcessor.class));
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .isNotEmpty()
                .contains("source 'https://rubygems.org'");
    }

    @Test
    public void should_run_the_same_preprocessor_twice_when_registered_twice() throws MojoFailureException, MojoExecutionException {
        // given
        ConsoleHolder consoleHolder = ConsoleHolder.start();
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("preprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = Arrays.asList(
                extensionConfiguration(ChangeAttributeValuePreprocessor.class),
                extensionConfiguration(ChangeAttributeValuePreprocessor.class)
        );
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .isNotEmpty()
                .containsPattern("(" + ChangeAttributeValuePreprocessor.AUTHOR_NAME + "([\\s\\S])*){2}");

        assertThat(consoleHolder.getOutput())
                .containsPattern("(Processing ChangeAttributeValuePreprocessor([\\s\\S])*){2}");
        // cleanup
        consoleHolder.release();
    }

    // Adding a BlockMacroProcessor or BlockProcessor makes the conversion fail
    @Test
    public void should_convert_to_html_with_Preprocessor_DocinfoProcessor_InlineMacroProcessor_and_IncludeProcessor() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("preprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null);
        mojo.extensions = Arrays.asList(
                extensionConfiguration("org.asciidoctor.maven.test.processors.ChangeAttributeValuePreprocessor"),
                extensionConfiguration("org.asciidoctor.maven.test.processors.MetaDocinfoProcessor"),
                extensionConfiguration("org.asciidoctor.maven.test.processors.ManpageInlineMacroProcessor", "man"),
                extensionConfiguration("org.asciidoctor.maven.test.processors.UriIncludeProcessor")
        );
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .containsPattern("(" + ChangeAttributeValuePreprocessor.AUTHOR_NAME + "([\\s\\S])*){2}")
                .contains("<meta name=\"author\" content=\"asciidoctor\">")
                .contains("<p>See <a href=\"gittutorial.html\">gittutorial</a> to get started.</p>")
                .contains("source 'https://rubygems.org'");
    }

    @Test
    public void should_convert_to_html_using_all_extension_types() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("preprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", "",
                "linkcss", "",
                "copycss!", "");
        mojo.extensions = Arrays.asList(
                extensionConfiguration("org.asciidoctor.maven.test.processors.ChangeAttributeValuePreprocessor"),
                extensionConfiguration("org.asciidoctor.maven.test.processors.MetaDocinfoProcessor"),
                extensionConfiguration("org.asciidoctor.maven.test.processors.ManpageInlineMacroProcessor", "man"),
                extensionConfiguration("org.asciidoctor.maven.test.processors.UriIncludeProcessor"),
                extensionConfiguration("org.asciidoctor.maven.test.processors.GistBlockMacroProcessor", "gist"),
                extensionConfiguration("org.asciidoctor.maven.test.processors.YellBlockProcessor", "yell")
        );
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .contains("<meta name=\"author\" content=\"asciidoctor\">")
                .contains("<script src=\"https://gist.github.com/123456.js\"></script>")
                .contains("<p>See <a href=\"gittutorial.html\">gittutorial</a> to get started.</p>")
                .contains("<p>THE TIME IS NOW. GET A MOVE ON.</p>");
    }

    /**
     *  Manual test to validate automatic extension registration.
     *  To execute, copy _org.asciidoctor.extension.spi.ExtensionRegistry to
     *  /src/test/resources/META-INF/services/ and execute
     */
    @Ignore
    @Test
    public void property_extension() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(SRC_DIR);
        File outputDir = newOutputTestDirectory("preprocessor");
        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "processors-sample.adoc";
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null, "linkcss!", "");
        mojo.execute();
        // then
        AsciidoctorAsserter.assertThat(outputDir, "processors-sample.html")
                .containsPattern("(" + ChangeAttributeValuePreprocessor.AUTHOR_NAME + " ([\\s\\S])*){2}");
    }

    private ExtensionConfiguration extensionConfiguration(String className, String blockName) {
        ExtensionConfiguration extensionConfiguration = new ExtensionConfiguration();
        extensionConfiguration.setClassName(className);
        extensionConfiguration.setBlockName(blockName);
        return extensionConfiguration;
    }

    private ExtensionConfiguration extensionConfiguration(String className) {
        return extensionConfiguration(className, null);
    }

    private ExtensionConfiguration extensionConfiguration(Class<?> clazz) {
        return extensionConfiguration(clazz.getCanonicalName(), null);
    }

    private ExtensionConfiguration extensionConfiguration(Class<?> clazz, String blockName) {
        return extensionConfiguration(clazz.getCanonicalName(), blockName);
    }
}
