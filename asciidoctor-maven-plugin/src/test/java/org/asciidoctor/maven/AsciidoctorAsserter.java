package org.asciidoctor.maven;

import java.io.File;
import java.nio.file.Files;

import lombok.SneakyThrows;
import org.assertj.core.api.AbstractFileAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;

public class AsciidoctorAsserter {

    private final AbstractFileAssert<?> fileAssert;
    private final AbstractStringAssert<?> contentAssert;

    @SneakyThrows
    private AsciidoctorAsserter(File generatedFile) {
        this.fileAssert = Assertions.assertThat(generatedFile);
        this.contentAssert = Assertions.assertThat(Files.readString(generatedFile.toPath()));
    }

    public static AsciidoctorAsserter assertThat(File file) {
        return new AsciidoctorAsserter(file);
    }

    public static AsciidoctorAsserter assertThat(File parentPath, String filename) {
        return new AsciidoctorAsserter(new File(parentPath, filename));
    }

    public AsciidoctorAsserter isNotEmpty() {
        fileAssert.exists().isNotEmpty();
        return this;
    }

    public AsciidoctorAsserter contains(String text) {
        contentAssert.contains(text);
        return this;
    }

    public AsciidoctorAsserter containsPattern(String regex) {
        contentAssert.containsPattern(regex);
        return this;
    }

    public AsciidoctorAsserter containsOnlyOnce(String text) {
        contentAssert.containsOnlyOnce(text);
        return this;
    }

    public AsciidoctorAsserter doesNotContain(String text) {
        contentAssert.doesNotContain(text);
        return this;
    }

    public AsciidoctorAsserter startsWith(String prefix) {
        contentAssert.startsWith(prefix);
        return this;
    }
}
