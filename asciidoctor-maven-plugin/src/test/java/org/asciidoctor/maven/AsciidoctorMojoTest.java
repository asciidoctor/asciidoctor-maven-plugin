package org.asciidoctor.maven;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.asciidoctor.maven.extensions.ExtensionConfiguration;
import org.asciidoctor.maven.io.AsciidoctorFileScanner;
import org.asciidoctor.maven.io.ConsoleHolder;
import org.asciidoctor.maven.test.processors.RequireCheckerTreeprocessor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.asciidoctor.maven.AsciidoctorAsserter.assertThat;
import static org.asciidoctor.maven.TestUtils.*;
import static org.asciidoctor.maven.TestUtils.ResourceBuilder.excludeAll;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;


public class AsciidoctorMojoTest {

    private static final String DEFAULT_SOURCE_DIRECTORY = "target/test-classes/src/asciidoctor";


    @Test
    public void should_skip_execution_when_skip_is_set() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();
        Assertions.assertThat(outputDir).doesNotExist();

        // // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "main-document.adoc";
        mojo.outputDirectory = outputDir;
        mojo.skip = true;
        mojo.execute();
        // then
        Assertions.assertThat(outputDir).doesNotExist();
    }

    @Test
    public void should_skip_processing_when_source_directory_does_no_exist() throws MojoFailureException, MojoExecutionException {
        // given
        PrintStream originalOut = System.out;
        OutputStream newOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newOut));
        File outputDir = newOutputTestDirectory("multiple-resources-skip");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = new File(UUID.randomUUID().toString());
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        Assertions.assertThat(newOut.toString())
                .contains("sourceDirectory " + mojo.getSourceDirectory() + " does not exist")
                .contains("No sourceDirectory found. Skipping processing");
        Assertions.assertThat(outputDir).doesNotExist();

        // cleanup
        System.setOut(originalOut);
    }

    @Test
    public void should_skip_processing_when_there_are_no_sources() throws MojoFailureException, MojoExecutionException {
        // given
        PrintStream originalOut = System.out;
        OutputStream newOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newOut));

        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY, "templates");
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        Assertions.assertThat(newOut.toString())
                .contains("No sources found. Skipping processing");
        Assertions.assertThat(outputDir).doesNotExist();

        // cleanup
        System.setOut(originalOut);
    }

    @Test
    public void should_convert_to_docbook() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "docbook";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.execute();

        // then
        assertThat(outputDir, "sample.xml")
                .isNotEmpty()
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .contains("<article xmlns=\"http://docbook.org/ns/docbook\" xmlns:xl=\"http://www.w3.org/1999/xlink\" version=\"5.0\" xml:lang=\"en\">");
    }

    @Test
    public void should_convert_to_html5_with_defaults() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.resources = excludeAll();
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        assertThat(outputDir, "sample.html")
                .isNotEmpty()
                .contains("<body class=\"article\">")
                .contains("Asciidoctor default stylesheet")
                .contains("<pre class=\"highlight\"><code class=\"language-ruby\" data-lang=\"ruby\">")
                .doesNotContain("<link rel=\"stylesheet\" href=\"./asciidoctor.css\">");
    }

    @Test
    public void should_convert_to_html_with_attributes() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.resources = excludeAll();
        mojo.outputDirectory = outputDir;
        mojo.headerFooter = true;
        mojo.attributes = map("toc", null,
                "linkcss!", "",
                "source-highlighter", "coderay");

        mojo.execute();

        // then
        assertThat(outputDir, "sample.html")
                .isNotEmpty()
                .contains("<body class=\"article\">")
                .contains("id=\"toc\"")
                .contains("Asciidoctor default stylesheet")
                .contains("<pre class=\"CodeRay highlight\">")
                .doesNotContain("<link rel=\"stylesheet\" href=\"./asciidoctor.css\">");
    }

    @Test
    public void should_convert_to_html_with_a_custom_template() throws MojoFailureException, MojoExecutionException {
        // given
        final String templatesPath = "target/test-classes/templates/";
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.resources = excludeAll();
        mojo.outputDirectory = outputDir;
        mojo.templateDirs = Arrays.asList(
                new File(templatesPath, "set-1"),
                new File(templatesPath, "set-2")
        );
        mojo.execute();

        // then
        assertThat(outputDir, "sample.html")
                .isNotEmpty()
                .contains("custom-admonition-block")
                .contains("custom-block-style");
    }

    @Test
    public void should_set_output_file() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.attributes = map("icons", "font");
        mojo.embedAssets = true;
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "sample-embedded.adoc";
        mojo.outputDirectory = outputDir;
        mojo.outputFile = new File("custom_output_file.html");
        mojo.execute();

        // then
        assertThat(outputDir, "custom_output_file.html")
                .isNotEmpty()
                .contains("Asciidoctor default stylesheet")
                .contains("data:image/png;base64,iVBORw0KGgo")
                .contains("font-awesome.min.css")
                .contains("i class=\"fa icon-tip\"");
    }

    @Test
    public void should_override_output_directory_with_output_file_with_absolute_path() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();
        File outputFile = new File(newOutputTestDirectory().getAbsolutePath(), "custom_output_file_absolute.html");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.attributes = map("icons", "font");
        mojo.embedAssets = true;
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "sample-embedded.adoc";
        mojo.outputDirectory = outputDir;
        mojo.outputFile = outputFile;
        mojo.resources = excludeAll();
        mojo.execute();

        // then
        // NOTE: the plugin cr
        Assertions.assertThat(outputDir).exists();

        assertThat(outputFile)
                .isNotEmpty()
                .contains("Asciidoctor default stylesheet")
                .contains("data:image/png;base64,iVBORw0KGgo")
                .contains("font-awesome.min.css")
                .contains("i class=\"fa icon-tip\"");
    }

    @Test
    public void should_set_file_extension() throws MojoFailureException, MojoExecutionException {
        // given
        File outputDir = newOutputTestDirectory();
        Assertions.assertThat(outputDir).doesNotExist();

        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        outputDir.mkdirs();
        writeToFile(srcDir, "sample1.foo", "= Document Title\n\nfoo");
        writeToFile(srcDir, "sample2.bar", "= Document Title\n\nbar");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.sourceDocumentExtensions = Arrays.asList("foo", "bar");
        mojo.execute();

        // then
        assertThat(outputDir, "sample1.html")
                .isNotEmpty()
                .contains("foo");

        assertThat(outputDir, "sample2.html")
                .isNotEmpty()
                .contains("bar");
    }

    @Test
    public void should_set_flag_attribute_as_true() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely in the test!
        // Believe it or not, null is the equivalent of writing <toc/> in the XML configuration
        mojo.attributes = map("toc2", "true");
        mojo.execute();

        // then
        assertThat(outputDir, "sample.html")
                .isNotEmpty()
                .contains("<div id=\"toc\" class=\"toc2\">");
    }

    @Test
    public void should_unset_flag_attribute_as_false() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely in the test!
        // Believe it or not, null is the equivalent of writing <toc/> in the XML configuration
        mojo.attributes = map("toc2", "false");
        mojo.execute();

        // then
        assertThat(outputDir, "sample.html")
                .isNotEmpty()
                .doesNotContain("<div id=\"toc\" class=\"toc2\">");
    }

    @Test
    public void should_set_flag_attribute_as_null() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely in the test!
        // Believe it or not, null is the equivalent of writing <toc/> in the XML configuration
        mojo.attributes = map("toc", null);
        mojo.execute();

        // then
        assertThat(outputDir, "sample.html")
                .isNotEmpty()
                .contains("<div id=\"toc\" class=\"toc\">");
    }

    /**
     * Tests the behaviour // when:
     * - simple paths are used
     * - preserveDirectories = true
     * - relativeBaseDir = true
     * <p>
     * Expected:
     * - all documents are converted in the same folder structure found in the sourceDirectory
     * - all documents are correctly converted with the import
     */
    @Test
    public void should_replicate_source_structure_when_standard_paths() throws MojoFailureException, MojoExecutionException, IOException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor/relative-path-treatment");
        File outputDir = newOutputTestDirectory("relative");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.preserveDirectories = true;
        mojo.relativeBaseDir = true;
        mojo.attributes = map("icons", "font");
        mojo.execute();

        // then
        Assertions.assertThat(outputDir).isNotEmptyDirectory();
        assertEqualsStructure(srcDir.listFiles(File::isDirectory), outputDir.listFiles(File::isDirectory));

        List<Path> asciidocs = Files.walk(outputDir.toPath())
                .filter(path -> path.getFileName().toString().endsWith("html"))
                .collect(Collectors.toList());
        Assertions.assertThat(asciidocs).hasSize(6);

        // Checks that all imports are found in the respective baseDir
        Assertions.assertThat(asciidocs).allSatisfy(path -> {
            assertThat(path.toFile()).doesNotContain("Unresolved directive");
        });
    }

    /**
     * Tests the behaviour // when:
     * - complex paths are used
     * - preserveDirectories = true
     * - relativeBaseDir = true
     * <p>
     * Expected:
     * - all documents are converted in the same folder structure found in the sourceDirectory
     * - all documents are correctly converted with the import
     */
    @Test
    public void should_replicate_source_structure_when_complex_paths() throws MojoFailureException, MojoExecutionException, IOException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor/relative-path-treatment/../relative-path-treatment");
        File outputDir = newOutputTestDirectory("relative");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.preserveDirectories = true;
        mojo.relativeBaseDir = true;
        mojo.attributes = map("icons", "font");
        mojo.execute();

        // then
        Assertions.assertThat(outputDir).isNotEmptyDirectory();
        Assertions.assertThat(outputDir.listFiles(f -> f.getName().endsWith("html"))).hasSize(1);

        assertEqualsStructure(srcDir.listFiles(File::isDirectory), outputDir.listFiles(File::isDirectory));

        List<Path> asciidocs = Files.walk(outputDir.toPath())
                .filter(path -> path.getFileName().toString().endsWith("html"))
                .collect(Collectors.toList());
        Assertions.assertThat(asciidocs).hasSize(6);

        // Checks that all imports are found in the respective baseDir
        Assertions.assertThat(asciidocs).allSatisfy(path -> {
            assertThat(path.toFile()).doesNotContain("Unresolved directive");
        });
    }

    /**
     * Tests the behaviour // when:
     * - complex paths are used
     * - preserveDirectories = false
     * - relativeBaseDir = false
     * <p>
     * Expected:
     * - all documents are converted in the same outputDirectory. 1 document is overwritten
     * - all documents but 1 (in the root) are incorrectly converted because they cannot find the imported file
     */
    @Test
    public void should_not_replicate_source_structure_when_complex_paths() throws MojoFailureException, MojoExecutionException, IOException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor/relative-path-treatment/../relative-path-treatment");
        File outputDir = newOutputTestDirectory("relative");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        Assertions.assertThat(outputDir).isNotEmptyDirectory();
        // 1 file is missing because 2 share the same name and 1 is overwritten in outputDirectory
        List<Path> asciidocs = Files.walk(outputDir.toPath())
                .filter(path -> path.getFileName().toString().endsWith("html"))
                .collect(Collectors.toList());
        Assertions.assertThat(asciidocs).hasSize(5);

        // folders are copied anyway
        assertEqualsStructure(srcDir.listFiles(File::isDirectory), outputDir.listFiles(File::isDirectory));

        // Looks for import errors in all files but the one in the root folder
        Assertions.assertThat(asciidocs.stream()
                .filter(path -> !path.getFileName().toString().equals("HelloWorld.html"))
                .collect(Collectors.toList()))
                .allSatisfy(path -> {
                    assertThat(path.toFile()).contains("Unresolved directive");
                });
    }

    /**
     * Tests the behaviour // when:
     * - simple paths are used
     * - preserveDirectories = true
     * - relativeBaseDir = false
     * <p>
     * Expected:
     * - all documents are converted in the same folder structure found in the sourceDirectory
     * - all documents but 1 (in the root) are incorrectly converted because they cannot find the imported file
     */
    @Test
    public void should_replicate_source_structure_when_no_baseDir_rewrite() throws MojoFailureException, MojoExecutionException, IOException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor/relative-path-treatment");
        File outputDir = newOutputTestDirectory("relative");

        // // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.preserveDirectories = true;
        mojo.baseDir = srcDir;
        //mojo.relativeBaseDir = true
        mojo.attributes = map("icons", "font");
        mojo.execute();

        // then
        Assertions.assertThat(outputDir).isNotEmptyDirectory();
        assertEqualsStructure(srcDir.listFiles(File::isDirectory), outputDir.listFiles(File::isDirectory));

        List<Path> asciidocs = Files.walk(outputDir.toPath())
                .filter(path -> path.getFileName().toString().endsWith("html"))
                .collect(Collectors.toList());
        Assertions.assertThat(asciidocs).hasSize(6);

        // Looks for import errors in all files but the one in the root folder
        Assertions.assertThat(asciidocs.stream()
                .filter(path1 -> !path1.getFileName().toString().equals("HelloWorld.html"))
                .collect(Collectors.toList()))
                .allSatisfy(path -> assertThat(path.toFile()).contains("Unresolved directive"));
    }

    /**
     * Tests the behaviour // when:
     * - simple paths are used
     * - preserveDirectories = false
     * - relativeBaseDir = true
     * <p>
     * Expected: all documents are correctly converted in the same folder
     */
    @Test
    public void should_not_replicate_source_structure_when_baseDir_rewrite() throws MojoFailureException, MojoExecutionException, IOException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor/relative-path-treatment");
        File outputDir = newOutputTestDirectory("relative");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.preserveDirectories = false;
        mojo.relativeBaseDir = true;
        mojo.attributes = map("icons", "font");
        mojo.execute();

        // then
        assertEqualsStructure(srcDir.listFiles(File::isDirectory), outputDir.listFiles(File::isDirectory));
        // all files are converted in the outputDirectory
        // 1 file is missing because 2 share the same name and 1 is overwritten in outputDirectory
        List<Path> asciidocs = Files.walk(outputDir.toPath())
                .filter(path -> path.getFileName().toString().endsWith("html"))
                .collect(Collectors.toList());
        Assertions.assertThat(asciidocs).hasSize(5);
        // Checks that all imports are found in the respective baseDir
        Assertions.assertThat(asciidocs).allSatisfy(path -> {
            assertThat(path.toFile()).doesNotContain("Unresolved directive");
        });
    }

    @Test
    public void should_copy_all_resources_into_output_folder() throws MojoFailureException, MojoExecutionException {
        // given
        File outputDir = newOutputTestDirectory("multiple-resources-multiple-sources");
        String relativeTestsPath = DEFAULT_SOURCE_DIRECTORY + "/relative-path-treatment";

        // when: 2 directories with filters
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = new File(DEFAULT_SOURCE_DIRECTORY + "/issue-78");
        mojo.resources = Arrays.asList(
                new ResourceBuilder()
                        .directory(DEFAULT_SOURCE_DIRECTORY + "/issue-78")
                        .includes("**/*.adoc")
                        .build(),
                new ResourceBuilder()
                        .directory(relativeTestsPath)
                        .excludes("**/*.jpg")
                        .build());
        mojo.outputDirectory = outputDir;
        mojo.preserveDirectories = true;
        mojo.execute();

        // then
        File[] files = outputDir.listFiles(File::isFile);
        // includes 2 converted AsciiDoc documents and 3 resources
        Assertions.assertThat(files).hasSize(5);
        // from 'issue-78' directory
        // resource files obtained using the include
        // 'images' folder is not copied because it's not included
        List<String> actualFilenames = Arrays.stream(files)
                .map(File::getName)
                .collect(Collectors.toList());
        Assertions.assertThat(actualFilenames)
                .contains("main.html", "image-test.html")
                .doesNotContain("images");
        // from 'relative-path-treatment' directory
        // all folders and files are created because only image files are excluded
        assertEqualsStructure(new File(relativeTestsPath).listFiles(File::isDirectory), outputDir.listFiles(File::isDirectory));
        // images are excluded but not the rest of files
        Assertions.assertThat(FileUtils.listFiles(outputDir, new String[]{"groovy"}, true)).hasSize(5);
        Assertions.assertThat(FileUtils.listFiles(outputDir, new String[]{"jpg"}, true)).hasSize(0);
    }

    @Test
    public void should_not_copy_files_in_hidden_directories() throws MojoFailureException, MojoExecutionException {
        // given
        String relativeTestsPath = DEFAULT_SOURCE_DIRECTORY + "/relative-path-treatment";
        File outputDir = newOutputTestDirectory("hidden-resources");

        //when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = new File(relativeTestsPath);
        mojo.preserveDirectories = true;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        List<String> hiddenDirectories = Arrays.asList("_this_is_ignored", "level-1-1/level-2-2/_this_is_ignored");
        Assertions.assertThat(hiddenDirectories)
                .allSatisfy(directory -> {
                    Assertions.assertThat(new File(relativeTestsPath, directory)).isDirectory();
                    Assertions.assertThat(new File(outputDir, directory)).doesNotExist();
                });
    }

    @Test
    public void should_not_copy_custom_source_documents_when_custom_extensions_are_set() throws MojoFailureException, MojoExecutionException {
        // given
        File outputDir = newOutputTestDirectory("resources");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = new File(DEFAULT_SOURCE_DIRECTORY);
        mojo.sourceDocumentExtensions = singletonList("ext");
        mojo.resources = singletonList(new ResourceBuilder()
                .directory(DEFAULT_SOURCE_DIRECTORY)
                .includes("**/*.adoc")
                .excludes("**/**")
                .build());
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        Assertions.assertThat(FileUtils.listFiles(outputDir, new String[]{"ext"}, true)).isEmpty();
        assertThat(outputDir, "sample.html")
                .contains("Asciidoctor default stylesheet");
    }

    /**
     * Validates that the folder structures under certain files are the same
     *
     * @param expected list of expected folders
     * @param actual   list of actual folders (the ones to validate)
     */
    private void assertEqualsStructure(File[] expected, File[] actual) {

        List<File> sanitizedExpected = Arrays.stream(expected)
                .filter(file -> {
                    char firstChar = file.getName().charAt(0);
                    return firstChar != '_' && firstChar != '.';
                })
                .collect(Collectors.toList());

        List<String> expectedNames = sanitizedExpected.stream().map(File::getName).collect(Collectors.toList());
        List<String> actualNames = Arrays.stream(actual).map(File::getName).collect(Collectors.toList());
        Assertions.assertThat(expectedNames).containsExactlyInAnyOrder(actualNames.toArray(new String[]{}));

        for (File actualFile : actual) {
            File expectedFile = sanitizedExpected.stream()
                    .filter(f -> f.getName().equals(actualFile.getName()))
                    .findFirst()
                    .get();

            // check that at least the number of html files and asciidoc are the same in each folder
            File[] expectedChildren = Arrays.stream(expectedFile.listFiles(File::isDirectory))
                    .filter(f -> !f.getName().startsWith("_"))
                    .toArray(File[]::new);

            File[] htmls = actualFile.listFiles(f -> f.getName().endsWith("html"));
            if (htmls.length > 0) {
                File[] asciidocs = expectedFile.listFiles(f -> {
                    String asciidocFilePattern = ".*\\." + AsciidoctorFileScanner.ASCIIDOC_FILE_EXTENSIONS_REG_EXP + "$";
                    return f.getName().matches(asciidocFilePattern) && !f.getName().startsWith("_") && !f.getName().startsWith(".");
                });
                Assertions.assertThat(htmls).hasSize(asciidocs.length);
            }
            File[] actualChildren = actualFile.listFiles(File::isDirectory);
            assertEqualsStructure(expectedChildren, actualChildren);
        }
    }

    @Test
    public void should_not_crash_when_enabling_maven_resource_filtering() throws MojoFailureException, MojoExecutionException {
        // given
        File outputDir = newOutputTestDirectory("resources");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = new File(".");
        mojo.sourceDocumentName = "README.adoc";
        Resource resource = new ResourceBuilder()
                .directory(".")
                .excludes("**/**")
                .build();
        resource.setFiltering(true);
        mojo.resources = singletonList(resource);
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        File[] actualConvertedFiles = outputDir.listFiles(File::isFile);
        Assertions.assertThat(actualConvertedFiles).hasSize(1);
        assertThat(actualConvertedFiles[0])
                .contains("Asciidoctor Maven Plugin");
    }

    @Test
    public void should_only_convert_documents_and_not_copy_any_resources_when_resources_directory_does_no_exist() throws MojoFailureException, MojoExecutionException {
        // given
        File outputDir = newOutputTestDirectory("multiple-sources-error-source-not-found");

        // when: resource directory does not exist but source AsciiDoc documents do
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = new File(DEFAULT_SOURCE_DIRECTORY);
        mojo.resources = singletonList(new ResourceBuilder()
                .directory(UUID.randomUUID().toString())
                .build());
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then: only converts (html) files are found in the target directory
        File[] actualFiles = outputDir.listFiles(File::isFile);
        Collection<File> htmlFiles = FileUtils.listFiles(outputDir, new String[]{"html"}, true);

        Assertions.assertThat(actualFiles).hasSize(htmlFiles.size());
        Assertions.assertThat(outputDir.listFiles(File::isDirectory)).hasSize(0);
    }

    @Test
    public void should_convert_single_document_and_not_copy_any_resources_when_excluding_all_resources() throws MojoFailureException, MojoExecutionException {
        // given
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDocumentName = "README.adoc";
        mojo.sourceDirectory = new File(".");
        mojo.resources = excludeAll();
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        File[] actualFiles = outputDir.listFiles(File::isFile);
        Assertions.assertThat(actualFiles).hasSize(1);
        AsciidoctorAsserter.assertThat(outputDir, "README.html")
                .contains("Asciidoctor Maven Plugin");
    }

    @Test
    public void should_only_convert_a_single_file_and_not_copy_any_resource() throws MojoFailureException, MojoExecutionException {
        // given
        File outputDir = newOutputTestDirectory("multiple-resources-file-pattern");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.sourceDirectory = new File(DEFAULT_SOURCE_DIRECTORY);
        mojo.backend = "html5";
        mojo.sourceDocumentName = "attribute-missing.adoc";
        // excludes all, nothing at all is copied
        mojo.resources = singletonList(new ResourceBuilder()
                .directory(DEFAULT_SOURCE_DIRECTORY)
                .excludes("**/**")
                .build());
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        File[] actualFiles = outputDir.listFiles(File::isFile);
        Assertions.assertThat(actualFiles).hasSize(1);
        Assertions.assertThat(actualFiles[0].getName()).isEqualTo("attribute-missing.html");
    }

    @Test
    public void should_require_ruby_gem() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.requires = singletonList("time");
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.outputDirectory = outputDir;
        ExtensionConfiguration extension = new ExtensionConfiguration();
        extension.setClassName(RequireCheckerTreeprocessor.class.getCanonicalName());
        mojo.extensions.add(extension);
        mojo.execute();

        // then
        assertThat(outputDir, "sample.html")
                .contains("RequireCheckerTreeprocessor was here");
    }

    @Test
    public void should_embed_resources() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("embedAssets");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDocumentName = "sample-embedded.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.attributes = map("icons", "font");
        mojo.embedAssets = true;
        mojo.execute();

        // then
        // note: fontawesome is not embedded
        assertThat(outputDir, "sample-embedded.html")
                .contains("Asciidoctor default stylesheet")
                .contains("data:image/png;base64,iVBORw0KGgo")
                .contains("font-awesome.min.css")
                .contains("i class=\"fa icon-tip\"");
    }


    // issue-78
    @Test
    public void should_embed_image_in_included_adoc() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File("target/test-classes/src/asciidoctor/issue-78");
        File outputDir = newOutputTestDirectory("embedAssets");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDocumentName = "main.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.doctype = "book";
        mojo.embedAssets = true;
        mojo.execute();

        // then
        assertThat(outputDir, "main.html")
                .contains("<p>Here&#8217;s an image:</p>")
                .contains("<img src=\"data:image/jpg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4gzESUNDX1BST0ZJTEUAAQEAAA");
        assertThat(new File(outputDir, "halliburton_lab.jpg")).isNotEmpty();
    }

    @Test
    public void should_pass_images_directory_as_attribute() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.sourceDocumentName = "imageDir.adoc";
        mojo.outputDirectory = outputDir;
        mojo.attributes = map("imagesdir", "custom-images-dir");
        mojo.execute();

        // then
        assertThat(outputDir, "imageDir.html")
                .contains("<img src=\"custom-images-dir/my-cool-image.jpg\" alt=\"my cool image\">");
    }

    @Test
    public void should_pass_attributes_from_pom_configuration() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("attributes");

        //  when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDocumentName = "attributes-example.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.attributes = map(
                "plugin-configuration-attribute", "plugin configuration",
                "execution-attribute", "execution configuration"
        );
        mojo.resources = excludeAll();
        mojo.execute();

        // then
        assertThat(outputDir, "attributes-example.html")
                .contains("This attribute is set in the plugin configuration: plugin configuration")
                .contains("This attribute is set in the execution configuration: execution configuration");
    }

    @Test
    public void should_pass_attributes_from_maven_properties() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("attributes");
        //  when

        AsciidoctorMojo mojo = mockAsciidoctorMojo(map("project.property.attribute", "project property configuration"));
        mojo.backend = "html5";
        mojo.sourceDocumentName = "attributes-example.adoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.resources = excludeAll();
        mojo.execute();

        // then
        assertThat(outputDir, "attributes-example.html")
                .contains("This attribute is set in the project&#8217;s properties: project property configuration");
    }

    @Test
    public void command_line_attributes_should_replace_configurations_and_attributes() throws MojoFailureException, MojoExecutionException {
        // given
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY);
        File outputDir = newOutputTestDirectory("configuration");

        // when: set toc and sourceHighlighter as XML configuration and command line attributes
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html";
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.attributes = map(
                "toc", "left",
                "source-highlighter", "coderay");
        // replace some options
        mojo.attributesChain = "toc=right source-highlighter=highlight.js";
        mojo.execute();

        // then: command line options are applied instead of xml configuration
        assertThat(outputDir, "sample.html")
                .contains("<body class=\"article toc2 toc-right\">")
                .contains("<pre class=\"highlightjs highlight\">");
    }

    @Test
    public void should_show_message_when_overwriting_files_without_outputFile() throws MojoFailureException, MojoExecutionException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();
        // srcDir contains 6 documents, 2 of them with the same name (HellowWorld3.adoc)
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY, "/relative-path-treatment/");
        File outputDir = newOutputTestDirectory("overlapping-outputFile");
        if (!outputDir.exists())
            outputDir.mkdir();

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.execute();

        // then
        Collection<File> actualConvertedFiles = FileUtils.listFiles(outputDir, new String[]{"html"}, true);
        Assertions.assertThat(actualConvertedFiles).hasSize(5);

        Assertions.assertThat(consoleHolder.getOutput())
                .containsPattern("(Converted ([\\s\\S])*){6}")
                .containsPattern("Duplicated destination found");

        // cleanup
        consoleHolder.release();
    }

    @Test
    public void should_show_message_when_overwriting_files_using_outputFile() throws MojoFailureException, MojoExecutionException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY, "relative-path-treatment/");
        File outputDir = newOutputTestDirectory("overlapping-outputFile");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.outputFile = new File("single-output.html");
        mojo.execute();

        // then
        Collection<File> actualConvertedFiles = FileUtils.listFiles(outputDir, new String[]{"html"}, true);
        Assertions.assertThat(actualConvertedFiles).hasSize(1);

        Assertions.assertThat(consoleHolder.getOutput())
                .containsPattern("(Converted ([\\s\\S])*){6}")
                .containsPattern("(Duplicated destination found: ([\\s\\S])*){5}");

        // cleanup
        consoleHolder.release();
    }

    @SneakyThrows
    @Test
    public void should_not_show_message_when_overwriting_files_using_outputFile_and_preserveDirectories() {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY, "/relative-path-treatment/");
        File outputDir = newOutputTestDirectory("overlapping-outputFile");

        // when
        AsciidoctorMojo mojo = mockAsciidoctorMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.preserveDirectories = true;
        mojo.outputFile = new File("single-output.html");
        mojo.execute();

        // then
        Collection<File> actualConvertedFiles = FileUtils.listFiles(outputDir, new String[]{"html"}, true);
        Assertions.assertThat(actualConvertedFiles).hasSize(5);

        Assertions.assertThat(consoleHolder.getOutput())
                .containsPattern("(Converted ([\\s\\S])*){6}") // any character including linebreaks
                .containsPattern("Duplicated destination found");

        // cleanup
        consoleHolder.release();
    }

}
