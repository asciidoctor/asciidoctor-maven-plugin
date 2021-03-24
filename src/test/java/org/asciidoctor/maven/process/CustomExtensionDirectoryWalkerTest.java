package org.asciidoctor.maven.process;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomExtensionDirectoryWalkerTest {

    private static final String DEFAULT_SOURCE_DIRECTORY = "src/test/resources/src/asciidoctor";

    @Test
    public void should_init_CustomExtensionDirectoryWalker() {
        // when
        CustomExtensionDirectoryWalker walker = new CustomExtensionDirectoryWalker("", Collections.emptyList());
        // then
        assertThat(walker).isNotNull();
    }

    @Test
    public void should_exclude_internal_sources() {
        // given
        final String rootDirectory = DEFAULT_SOURCE_DIRECTORY + "/relative-path-treatment";
        final List<String> fileExtensions = Collections.singletonList("adoc");

        // when
        List<File> files = new CustomExtensionDirectoryWalker(rootDirectory, fileExtensions)
                .scan();

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
        List<File> files = new CustomExtensionDirectoryWalker(rootDirectory, fileExtensions)
                .scan();

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
