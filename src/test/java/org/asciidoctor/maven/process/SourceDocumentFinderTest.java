package org.asciidoctor.maven.process;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SourceDocumentFinderTest {

    private static final String DEFAULT_SOURCE_DIRECTORY = "src/test/resources/src/asciidoctor";

    @Test
    public void should_init_SourceDocumentFinder() {
        // when
        SourceDocumentFinder walker = new SourceDocumentFinder();
        // then
        assertThat(walker).isNotNull();
    }

    @Test
    public void should_match_standard_file_extensions() {
        // given
        final String rootDirectory = DEFAULT_SOURCE_DIRECTORY + "/file-extensions";

        // when
        List<File> files = new SourceDocumentFinder().find(Paths.get(rootDirectory));

        // then
        assertThat(files)
            .isNotEmpty()
            .map(File::getName)
            .allMatch(name -> name.endsWith("ad") || name.endsWith("adoc") || name.endsWith("asc") ||
                name.endsWith("asciidoc"));
    }

    @Test
    public void should_match_custom_file_extension() {
        // given
        final String rootDirectory = DEFAULT_SOURCE_DIRECTORY + "/file-extensions";

        // when
        List<File> files = new SourceDocumentFinder().find(Paths.get(rootDirectory), Collections.singletonList("my-adoc"));

        // then
        assertThat(files)
            .isNotEmpty()
            .allMatch(file -> file.getName().endsWith("my-adoc"));
    }

    @Test
    public void should_match_custom_file_extensions() {
        // given
        final String rootDirectory = DEFAULT_SOURCE_DIRECTORY + "/file-extensions";
        List<String> customFileExtensions = new ArrayList<>();
        customFileExtensions.add("my-adoc");
        customFileExtensions.add("adoc");

        // when
        List<File> files = new SourceDocumentFinder().find(Paths.get(rootDirectory), customFileExtensions);

        // then
        assertThat(files)
            .isNotEmpty()
            .map(File::getName)
            .allMatch(name -> name.endsWith("my-adoc") || name.endsWith("adoc"));
    }

    @Test
    public void should_not_match_custom_empty_file_extensions() {
        // given
        final String rootDirectory = DEFAULT_SOURCE_DIRECTORY + "/file-extensions";

        // when
        List<File> files = new SourceDocumentFinder().find(Paths.get(rootDirectory), Collections.emptyList());

        // then
        assertThat(files)
            .isEmpty();
    }

    @Test
    public void should_return_empty_list_if_wrong_source_directory() {
        // given
        final String rootDirectory = DEFAULT_SOURCE_DIRECTORY + "/file-extensions/non-existing";

        // when
        List<File> files = new SourceDocumentFinder().find(Paths.get(rootDirectory));

        // then
        assertThat(files)
            .isEmpty();
    }

    @Test
    public void should_exclude_internal_sources() {
        // given
        final String rootDirectory = DEFAULT_SOURCE_DIRECTORY + "/relative-path-treatment";
        final List<String> fileExtensions = Collections.singletonList("adoc");

        // when
        List<File> files = new SourceDocumentFinder().find(Paths.get(rootDirectory), fileExtensions);

        // then
        assertThat(files)
                .isNotEmpty()
                .allMatch(file -> !file.getName().startsWith("_"));
    }

    @Test
    public void should_exclude_internal_directories() {
        // given
        final String rootDirectory = DEFAULT_SOURCE_DIRECTORY + "/relative-path-treatment";
        final List<String> fileExtensions = Collections.singletonList("adoc");

        // when
        List<File> files = new SourceDocumentFinder().find(Paths.get(rootDirectory), fileExtensions);

        // then
        assertThat(files)
                .isNotEmpty()
                .allMatch(file -> !isContainedInInternalDirectory(file));
    }

    private boolean isContainedInInternalDirectory(File file) {
        final String path = file.getPath();
        int cursor = 0;
        do {
            cursor = path.indexOf(File.separator, cursor + 1);
            if (path.charAt(cursor + 1) == '_')
                return true;
        } while (cursor != -1);
        return false;
    }
}
