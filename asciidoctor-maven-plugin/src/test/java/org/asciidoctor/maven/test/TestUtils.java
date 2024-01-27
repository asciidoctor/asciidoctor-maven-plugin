package org.asciidoctor.maven.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.maven.AsciidoctorHttpMojo;
import org.asciidoctor.maven.AsciidoctorMojo;
import org.asciidoctor.maven.AsciidoctorRefreshMojo;
import org.asciidoctor.maven.AsciidoctorZipMojo;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.model.Resource;

import static java.util.Collections.singletonList;
import static org.asciidoctor.maven.process.SourceDocumentFinder.STANDARD_FILE_EXTENSIONS_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    private static final MojoMocker mojoMocker = new MojoMocker();

    public static AsciidoctorRefreshMojo newFakeRefreshMojo() {
        return mojoMocker.mock(AsciidoctorRefreshMojo.class, null, null);
    }

    public static AsciidoctorMojo mockAsciidoctorMojo() {
        return mojoMocker.mock(AsciidoctorMojo.class, null, null);
    }

    public static AsciidoctorHttpMojo mockAsciidoctorHttpMojo() {
        return mojoMocker.mock(AsciidoctorHttpMojo.class, null, null);
    }

    public static AsciidoctorZipMojo mockAsciidoctorZipMojo() {
        return mojoMocker.mock(AsciidoctorZipMojo.class, null, null);
    }

    public static AsciidoctorMojo mockAsciidoctorMojo(Map<String, String> mavenProperties) {
        return mojoMocker.mock(AsciidoctorMojo.class, mavenProperties, null);
    }

    public static AsciidoctorMojo mockAsciidoctorMojo(LogHandler logHandler) {
        return mojoMocker.mock(AsciidoctorMojo.class, null, logHandler);
    }

    public static class ResourceBuilder {
        private final Resource resource = new Resource();

        public ResourceBuilder directory(String directory) {
            resource.setDirectory(directory);
            return this;
        }

        public ResourceBuilder includes(String... includes) {
            resource.setIncludes(mutableList(includes));
            return this;
        }

        public ResourceBuilder excludes(String... excludes) {
            resource.setExcludes(mutableList(excludes));
            return this;
        }

        private List<String> mutableList(String[] includes) {
            List<String> list = new ArrayList<>();
            for (String include : includes) {
                list.add(include);
            }
            return list;
        }

        public ResourceBuilder targetPath(String targetPath) {
            resource.setTargetPath(targetPath);
            return this;
        }

        public Resource build() {
            return resource;
        }

        public static List<Resource> excludeAll() {
            return singletonList(new ResourceBuilder().directory(".").excludes("**/**").build());
        }
    }

    /**
     * Validates that the folder structures under certain files contain the same
     * directories and file names.
     *
     * @param expected list of expected folders
     * @param actual   list of actual folders (the ones to validate)
     */
    public static void assertEqualsStructure(File[] expected, File[] actual) {

        List<File> sanitizedExpected = Arrays.stream(expected)
                .filter(TestUtils::isNotHidden)
                .collect(Collectors.toList());

        List<String> expectedNames = sanitizedExpected.stream().map(File::getName).collect(Collectors.toList());
        List<String> actualNames = Arrays.stream(actual).map(File::getName).collect(Collectors.toList());
        assertThat(expectedNames).containsExactlyInAnyOrder(actualNames.toArray(new String[]{}));

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
                File[] asciiDocs = expectedFile.listFiles(f -> f.getName().matches(STANDARD_FILE_EXTENSIONS_PATTERN));
                assertThat(htmls).hasSize(asciiDocs.length);
            }
            File[] actualChildren = actualFile.listFiles(File::isDirectory);
            assertEqualsStructure(expectedChildren, actualChildren);
        }
    }

    private static boolean isNotHidden(File file) {
        final char firstChar = file.getName().charAt(0);
        return firstChar != '_' && firstChar != '.';
    }
}
