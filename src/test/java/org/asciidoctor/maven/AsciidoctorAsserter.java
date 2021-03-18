package org.asciidoctor.maven;

import org.assertj.core.api.AbstractFileAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;

import java.io.File;

public class AsciidoctorAsserter {

    private final AbstractFileAssert<?> fileAssert;
    private final AbstractStringAssert<?> contentAssert;

    private AsciidoctorAsserter(File generatedFile) {
        this.fileAssert = Assertions.assertThat(generatedFile);
        this.contentAssert = Assertions.assertThat(TestUtils.readAsString(generatedFile));
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
