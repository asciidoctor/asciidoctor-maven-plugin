package org.asciidoctor.maven;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.asciidoctor.maven.io.TestFilesHelper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.asciidoctor.maven.TestUtils.mockAsciidoctorZipMojo;
import static org.assertj.core.api.Assertions.assertThat;


class AsciidoctorZipMojoTest {


    @Test
    void should_create_simple_zip() throws IOException, MojoFailureException, MojoExecutionException {
        // given: an empty output directory
        File outputDir = TestFilesHelper.newOutputTestDirectory("asciidoctor-zip-output");

        File zip = new File("target/asciidoctor-zip.zip");
        zip.delete();

        // when: zip mojo is called
        File srcDir = new File("target/test-classes/src/asciidoctor-zip");
        srcDir.mkdirs();


        FileUtils.write(new File(srcDir, "sample.adoc"),
                "= Title\n\ntest", UTF_8);

        AsciidoctorZipMojo mojo = mockAsciidoctorZipMojo();
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.zipDestination = zip;
        mojo.attach = false;
        mojo.execute();

        // then: a zip is created
        mojo.zipDestination.exists();

        ZipFile zipfile = new ZipFile(zip);
        List<String> names = getNames(zipfile.entries())
                .stream()
                .map(value -> normalizeOsPath(value))
                .map(value -> value.replaceAll("/" + normalizeOsPath(outputDir.toString()), ""))
                .collect(Collectors.toList());
        assertThat(names).hasSize(1);
        assertThat(names.get(0).replaceAll("\\\\", "/"))
                .isEqualTo("asciidoctor-zip/sample.html");
    }

    @Test
    void should_replicate_source_structure_in_zip_standard_paths() throws MojoFailureException, MojoExecutionException, IOException {
        // given
        File srcDir = new File("src/test/resources/src/asciidoctor/relative-path-treatment");
        File outputDir = TestFilesHelper.newOutputTestDirectory("asciidoctor-zip-output");

        File zip = new File(outputDir, "asciidoctor-zip.zip");

        // when
        AsciidoctorZipMojo mojo = mockAsciidoctorZipMojo();
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.preserveDirectories = true;
        mojo.relativeBaseDir = true;
        mojo.zipDestination = zip;
        mojo.attach = false;
        mojo.execute();

        // then
        ZipFile zipfile = new ZipFile(zip);
        List<String> names = getNames(zipfile.entries())
                .stream()
                .map(value -> normalizeOsPath(value))
                .map(value -> value.replaceAll("/" + normalizeOsPath(outputDir.toString()), ""))
                .collect(Collectors.toList());
        zipfile.close();
        // Paths are adapted for the test are do not match the real paths inside the zip
        List<String> expected = Arrays.asList(
                "asciidoctor-zip/HelloWorld.groovy",
                "asciidoctor-zip/HelloWorld.html",
                "asciidoctor-zip/level-1-1/asciidoctor-icon.jpg",
                "asciidoctor-zip/level-1-1/HelloWorld2.groovy",
                "asciidoctor-zip/level-1-1/HelloWorld2.html",
                "asciidoctor-zip/level-1-1/HelloWorld22.html",
                "asciidoctor-zip/level-1-1/level-2-1/HelloWorld3.groovy",
                "asciidoctor-zip/level-1-1/level-2-1/HelloWorld3.html",
                "asciidoctor-zip/level-1-1/level-2-2/HelloWorld3.groovy",
                "asciidoctor-zip/level-1-1/level-2-2/HelloWorld3.html",
                "asciidoctor-zip/level-1-1/level-2-2/level-3-1/HelloWorld4.groovy",
                "asciidoctor-zip/level-1-1/level-2-2/level-3-1/HelloWorld4.html"
        );
        assertThat(names)
                .containsAll(expected);
    }

    @Test
    void should_not_replicate_source_structure_in_zip_standard_paths() throws IOException, MojoFailureException, MojoExecutionException {
        // setup
        File srcDir = new File("src/test/resources/src/asciidoctor/relative-path-treatment");
        File outputDir = TestFilesHelper.newOutputTestDirectory("asciidoctor-zip-output");

        File zip = new File(outputDir, "asciidoctor-zip.zip");

        // when
        AsciidoctorZipMojo mojo = mockAsciidoctorZipMojo();
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.zipDestination = zip;
        mojo.attach = false;
        mojo.execute();

        // then
        ZipFile zipfile = new ZipFile(zip);
        List<String> names = getNames(zipfile.entries())
                .stream()
                .map(value -> normalizeOsPath(value))
                .map(value -> value.replaceAll("/" + normalizeOsPath(outputDir.toString()), ""))
                .collect(Collectors.toList());
        zipfile.close();
        // Paths are adapted for the test are do not match the real paths inside the zip
        List<String> expected = Arrays.asList(
                "asciidoctor-zip/HelloWorld.groovy",
                "asciidoctor-zip/HelloWorld.html",
                "asciidoctor-zip/HelloWorld2.html",
                "asciidoctor-zip/HelloWorld22.html",
                "asciidoctor-zip/HelloWorld3.html",
                "asciidoctor-zip/HelloWorld4.html",
                "asciidoctor-zip/level-1-1/asciidoctor-icon.jpg",
                "asciidoctor-zip/level-1-1/HelloWorld2.groovy",
                "asciidoctor-zip/level-1-1/level-2-1/HelloWorld3.groovy",
                "asciidoctor-zip/level-1-1/level-2-2/HelloWorld3.groovy",
                "asciidoctor-zip/level-1-1/level-2-2/level-3-1/HelloWorld4.groovy"
        );
        assertThat(names)
                .containsAll(expected);
    }

    private List<String> getNames(Enumeration<? extends ZipEntry> entries) {
        final List<String> names = new ArrayList<>();
        while (entries.hasMoreElements()) {
            names.add(entries.nextElement().getName());
        }
        return names;
    }

    private String normalizeOsPath(String path) {
        return path.replaceAll("\\\\", "/");
    }
}
