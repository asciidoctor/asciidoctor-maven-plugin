package org.asciidoctor.maven;

import org.asciidoctor.maven.process.SourceDirectoryFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.asciidoctor.maven.process.SourceDirectoryFinder.ORDERED_CANDIDATE_PATHS;
import static org.assertj.core.api.Assertions.assertThat;

public class SourceDirectoryFinderTest {

    @TempDir
    public File testDirectory;

    private static final File MOJO_DEFAULT_SOURCE_DIR = new File(SourceDirectoryFinder.DEFAULT_SOURCE_DIR);
    private static final File[] FALLBACK_CANDIDATES = new File[]{
            new File(ORDERED_CANDIDATE_PATHS[1]),
            new File(ORDERED_CANDIDATE_PATHS[2]),
    };

    public static final Consumer<File> EMPTY_CONSUMER = dir -> {
    };


    @Test
    public void should_not_try_candidates_and_not_find_when_initial_does_not_match_default_value() {
        // given
        final File fakePath = new File("fake_path");

        // when
        final AtomicInteger counter = new AtomicInteger();
        Optional<File> file = new SourceDirectoryFinder(fakePath, testDirectory, dir -> counter.getAndIncrement())
                .find();

        // then
        assertThat(file).isNotPresent();
        assertThat(counter).hasValue(1);
    }

    @Test
    public void should_not_try_candidates_and_find_when_initial_does_not_match_default_value() {
        // given
        final File fakePath = new File("fake_path");
        new File(testDirectory, fakePath.toString()).mkdirs();

        // when
        Optional<File> file = new SourceDirectoryFinder(fakePath, testDirectory, EMPTY_CONSUMER)
                .find();

        // then
        assertThat(file).isPresent();
    }

    @Test
    public void should_find_default_candidate_when_set_as_relative_path() {
        // given
        final File candidate = MOJO_DEFAULT_SOURCE_DIR;
        new File(testDirectory, candidate.toString()).mkdirs();

        // when
        Optional<File> file = new SourceDirectoryFinder(candidate, testDirectory, EMPTY_CONSUMER)
                .find();

        // then
        assertThat(file).isPresent();
        assertThat(file.get()).isAbsolute();
        assertThat(file.get().toPath()).endsWith(candidate.toPath());
    }

    @Test
    public void should_find_default_candidate_when_set_as_absolute_path() {
        // given
        final File candidate = new File(testDirectory, MOJO_DEFAULT_SOURCE_DIR.toString());
        candidate.mkdirs();

        // when
        Optional<File> file = new SourceDirectoryFinder(candidate, testDirectory, EMPTY_CONSUMER)
                .find();

        // then
        assertThat(file).isPresent();
        assertThat(file.get()).isAbsolute();
        assertThat(file.get().toPath()).isEqualTo(candidate.toPath());
    }

    @Test
    public void should_find_first_fallback_candidate_when_set_as_relative_path() {
        // given
        final File candidate = FALLBACK_CANDIDATES[0];
        new File(testDirectory, FALLBACK_CANDIDATES[0].toString()).mkdirs();
        final File defaultSourceDir = MOJO_DEFAULT_SOURCE_DIR;

        // when
        Optional<File> file = new SourceDirectoryFinder(defaultSourceDir, testDirectory, EMPTY_CONSUMER)
                .find();

        // then
        assertThat(file).isPresent();
        assertThat(file.get()).isAbsolute();
        assertThat(file.get().toPath()).endsWith(candidate.toPath());
    }

    @Test
    public void should_find_first_fallback_candidate_when_set_as_absolute_path() {
        // given
        final File candidate = new File(testDirectory, FALLBACK_CANDIDATES[0].toString());
        candidate.mkdirs();
        final File defaultSourceDir = MOJO_DEFAULT_SOURCE_DIR;

        // when
        Optional<File> file = new SourceDirectoryFinder(defaultSourceDir, testDirectory, EMPTY_CONSUMER)
                .find();

        // then
        assertThat(file).isPresent();
        assertThat(file.get()).isAbsolute();
        assertThat(file.get().toPath()).isEqualTo(candidate.toPath());
    }

    @Test
    public void should_find_second_fallback_candidate_when_set_as_relative_path() {
        // given
        final File candidate = FALLBACK_CANDIDATES[1];
        new File(testDirectory, FALLBACK_CANDIDATES[1].toString()).mkdirs();
        final File defaultSourceDir = MOJO_DEFAULT_SOURCE_DIR;

        // when
        Optional<File> file = new SourceDirectoryFinder(defaultSourceDir, testDirectory, EMPTY_CONSUMER)
                .find();

        // then
        assertThat(file).isPresent();
        assertThat(file.get()).isAbsolute();
        assertThat(file.get().toPath()).endsWith(candidate.toPath());
    }

    @Test
    public void should_find_second_fallback_candidate_when_set_as_absolute_path() {
        // given
        final File candidate = new File(testDirectory, FALLBACK_CANDIDATES[1].toString());
        candidate.mkdirs();
        final File defaultSourceDir = MOJO_DEFAULT_SOURCE_DIR;

        // wh
        Optional<File> file = new SourceDirectoryFinder(defaultSourceDir, testDirectory, EMPTY_CONSUMER)
                .find();

        // then
        assertThat(file).isPresent();
        assertThat(file.get()).isAbsolute();
        assertThat(file.get().toPath()).isEqualTo(candidate.toPath());
    }

    @Test
    public void should_try_all_candidates_and_not_find_any_candidate_when_initial_is_default() {
        // given
        final File defaultSourceDir = MOJO_DEFAULT_SOURCE_DIR;

        // when
        final AtomicInteger counter = new AtomicInteger();
        Optional<File> file = new SourceDirectoryFinder(defaultSourceDir, testDirectory, dir -> counter.getAndIncrement())
                .find();

        // then
        assertThat(file).isNotPresent();
        assertThat(counter).hasValue(3);
    }

}
