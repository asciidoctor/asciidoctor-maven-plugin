package org.asciidoctor.maven;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.DefaultMavenFileFilter;
import org.apache.maven.shared.filtering.DefaultMavenResourcesFiltering;
import org.asciidoctor.maven.io.ConsoleHolder;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.Mockito.when;

public class AsciidoctorRefreshMojoTest {

    @SneakyThrows
    public AsciidoctorRefreshMojo newFakeRefreshMojo() {
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        when(mavenProject.getBasedir()).thenReturn(new File("."));

        final BuildContext buildContext = new DefaultBuildContext();

        final DefaultMavenFileFilter mavenFileFilter = new DefaultMavenFileFilter();
        final ConsoleLogger plexusLogger = new ConsoleLogger();
        mavenFileFilter.enableLogging(plexusLogger);
        setVariableValueInObject(mavenFileFilter, "buildContext", buildContext);

        final DefaultMavenResourcesFiltering resourceFilter = new DefaultMavenResourcesFiltering();
        setVariableValueInObject(resourceFilter, "mavenFileFilter", mavenFileFilter);
        setVariableValueInObject(resourceFilter, "buildContext", buildContext);
        resourceFilter.initialize();
        resourceFilter.enableLogging(plexusLogger);

        final AsciidoctorRefreshMojo mojo = new AsciidoctorRefreshMojo();
        setVariableValueInObject(mojo, "log", new SystemStreamLog());
        mojo.encoding = "UTF-8";
        mojo.project = mavenProject;
        mojo.outputResourcesFiltering = resourceFilter;

        return mojo;
    }


    @Test
    public void should_stop_with_exit_command() {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // when
        consoleHolder.input("exit");

        // then
        consoleHolder.awaitProcessingAllSources();
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_stop_with_quit_command() {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // when
        consoleHolder.input("quit");

        // then
        consoleHolder.awaitProcessingAllSources();
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_show_tip_when_command_is_not_valid() {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);
        consoleHolder.awaitProcessingAllSources();
        // when
        consoleHolder.input("not_a_command");
        // then
        consoleHolder.awaitForMessage("'not_a_command' not understood, available commands are [quit, exit, refresh]");
        // then
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_only_auto_convert_file_with_custom_sourceDocumentName_when_source_is_updated() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final String fileExtension = "adoc";
        final File sourceFile = new File(srcDir, "my-sourceFile-" + UUID.randomUUID() + "." + fileExtension);
        final File ignoredFile = new File(srcDir, "extra-sourceFile." + fileExtension);

        // when
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        Thread mojoThread = runMojoAsynchronously(mojo -> {
            mojo.backend = "html5";
            mojo.sourceDirectory = srcDir;
            mojo.outputDirectory = outputDir;
            mojo.sourceDocumentName = sourceFile.getName();
        });

        // then
        final File target = new File(outputDir, sourceFile.getName().replace(fileExtension, "html"));
        consoleHolder.awaitProcessingAllSources();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("This is test, only a test");
        final File ignoredTarget = new File(outputDir, ignoredFile.getName().replace(fileExtension, "html"));
        assertThat(ignoredTarget)
                .doesNotExist();

        // and when
        FileUtils.write(sourceFile, "= Document Title\n\nWow, this will be auto refreshed !", UTF_8);

        // then
        consoleHolder.awaitProcessingSource();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("Wow, this will be auto refreshed");
        assertThat(ignoredTarget)
                .doesNotExist();

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_auto_convert_file_with_custom_file_extension_when_source_is_updated() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final String customFileExtension = "myadoc";
        final File sourceFile = new File(srcDir, "sourceFile." + customFileExtension);

        // when
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        Thread mojoThread = runMojoAsynchronously(mojo -> {
            mojo.backend = "html5";
            mojo.sourceDirectory = srcDir;
            mojo.outputDirectory = outputDir;
            mojo.sourceDocumentExtensions = Arrays.asList(customFileExtension);
        });

        // then
        final File target = new File(outputDir, sourceFile.getName().replace(customFileExtension, "html"));
        consoleHolder.awaitProcessingAllSources();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("This is test, only a test");

        // and when
        FileUtils.write(sourceFile, "= Document Title\n\nWow, this will be auto refreshed !", UTF_8);

        // then
        consoleHolder.awaitProcessingSource();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("Wow, this will be auto refreshed");

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_auto_convert_file_in_root_when_source_is_updated() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final File sourceFile = new File(srcDir, "sourceFile.asciidoc");

        // when
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // then
        final File target = new File(outputDir, sourceFile.getName().replace(".asciidoc", ".html"));
        consoleHolder.awaitProcessingAllSources();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("This is test, only a test");

        // and when
        FileUtils.write(sourceFile, "= Document Title\n\nWow, this will be auto refreshed !", UTF_8);

        // then
        consoleHolder.awaitProcessingSource();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("Wow, this will be auto refreshed");

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_auto_convert_file_in_subDir_when_source_is_updated() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final File sourceFile = new File(new File(srcDir, "sub-dir1/sub_dir2"), "sourceFile.asciidoc");

        // when
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // then
        File target = new File(outputDir, sourceFile.getName().replace(".asciidoc", ".html"));
        consoleHolder.awaitProcessingAllSources();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("This is test, only a test");

        // and when
        FileUtils.write(sourceFile, "= Document Title\n\nWow, this will be auto refreshed !", UTF_8);

        // then
        consoleHolder.awaitProcessingSource();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("Wow, this will be auto refreshed");

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_auto_convert_file_when_new_source_is_created() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final File sourceFile = new File(new File(srcDir, "sub-dir1/sub_dir2"), "sourceFile.asciidoc");

        // when
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // then
        File target = new File(outputDir, sourceFile.getName().replace(".asciidoc", ".html"));
        consoleHolder.awaitProcessingAllSources();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("This is test, only a test");

        // and when
        final File newSourceFile = new File(new File(srcDir, "sub-dir1/sub_dir2"), "sourceFile-2.asciidoc");
        FileUtils.write(newSourceFile, "= Document Title\n\nWow, this is NEW!!", UTF_8);

        // then
        File newTarget = new File(outputDir, newSourceFile.getName().replace(".asciidoc", ".html"));
        consoleHolder.awaitProcessingSource();
        assertThat(FileUtils.readFileToString(newTarget, UTF_8))
                .contains("Wow, this is NEW!!");
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .contains("This is test, only a test");

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_copy_resources_when_updated_but_not_on_start_when_there_are_no_sources() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final File resourceFile = new File(srcDir, "fakeImage.jpg");

        // when
        FileUtils.write(resourceFile, "Supposedly image content", UTF_8);
        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // then
        final File target = new File(outputDir, resourceFile.getName());
        consoleHolder.awaitProcessingAllSources();
        assertThat(target)
                .doesNotExist();

        // and when
        FileUtils.write(resourceFile, "Supposedly image content UPDATED!", UTF_8);

        // then
        consoleHolder.awaitProcessingResource();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .isEqualTo("Supposedly image content UPDATED!");

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_copy_resource_in_root_when_resource_is_updated() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        FileUtils.write(new File(srcDir, "sourceFile.asciidoc"),
                "= Document Title\n\nThis is test, only a test.", UTF_8);
        final String resourceFileExtension = "jpg";
        final File resourceFile = new File(srcDir, "fakeImage." + resourceFileExtension);

        // when
        FileUtils.write(resourceFile, "Supposedly image content", UTF_8);
        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // then
        final File target = new File(outputDir, resourceFile.getName());
        consoleHolder.awaitProcessingAllSources();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .isEqualTo("Supposedly image content");

        // and when
        FileUtils.write(resourceFile, "Supposedly image content UPDATED!", UTF_8);

        // then
        consoleHolder.awaitProcessingResource();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .isEqualTo("Supposedly image content UPDATED!");

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_copy_resource_in_subDir_when_resource_is_updated() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        FileUtils.write(new File(srcDir, "sourceFile.asciidoc"),
                "= Document Title\n\nThis is test, only a test.", UTF_8);
        final File subDirectory = new File(srcDir, "sub-dir1/sub_dir2");
        final String resourceFileExtension = "jpg";
        final File resourceFile = new File(subDirectory, "fakeImage." + resourceFileExtension);

        // when
        FileUtils.write(resourceFile, "Supposedly image content", UTF_8);
        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // then
        final File target = new File(subDirectory, resourceFile.getName());
        consoleHolder.awaitProcessingAllSources();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .isEqualTo("Supposedly image content");

        // and when
        FileUtils.write(resourceFile, "Supposedly image content UPDATED!", UTF_8);

        // then
        consoleHolder.awaitProcessingResource();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .isEqualTo("Supposedly image content UPDATED!");

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_ignore_resource_file_when_matches_custom_source_file_extensions() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final File sourceFile = new File(srcDir, "sourceFile.adoc");
        final File resourceFile1 = new File(srcDir, "fakeImage.jpg");
        final File resourceFile2 = new File(srcDir, "fakeImage.gif");

        // when:
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        FileUtils.write(resourceFile1, "= Not an image\n\nThis is reality a Adoc source with jpg extension", UTF_8);
        FileUtils.write(resourceFile2, "Supposedly image content", UTF_8);

        Thread mojoThread = runMojoAsynchronously(mojo -> {
            mojo.backend = "html5";
            mojo.sourceDirectory = srcDir;
            mojo.outputDirectory = outputDir;
            mojo.sourceDocumentExtensions = Arrays.asList("jpg");
        });

        // then: default source extensions and custom extensions are not copied as resources
        consoleHolder.awaitProcessingAllSources();
        final File targetSource = new File(outputDir, sourceFile.getName().replace("adoc", "html"));
        assertThat(targetSource)
                .doesNotExist();
        final File targetResource1 = new File(outputDir, resourceFile1.getName().replace("jpg", "html"));
        assertThat(FileUtils.readFileToString(targetResource1, UTF_8))
                .contains("This is reality a Adoc source with jpg extension");
        assertThat(new File(outputDir, resourceFile1.getName()))
                .doesNotExist();
        final File targetResource2 = new File(outputDir, resourceFile2.getName());
        assertThat(targetResource2)
                .exists();

        // and when
        FileUtils.write(resourceFile1, "= Not an image\n\nWow, this will be auto refreshed !", UTF_8);

        // then: custom file extensions is processed as source
        consoleHolder.awaitProcessingSource();
        assertThat(FileUtils.readFileToString(targetResource1, UTF_8))
                .contains("Wow, this will be auto refreshed");
        assertThat(new File(outputDir, resourceFile1.getName()))
                .doesNotExist();
        assertThat(targetResource2)
                .exists();
        assertThat(targetSource)
                .doesNotExist();

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_copy_resource_when_new_resource_is_created() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        FileUtils.write(new File(srcDir, "sourceFile.asciidoc"),
                "= Document Title\n\nThis is test, only a test.", UTF_8);
        final File subDirectory = new File(srcDir, "sub-dir1/sub_dir2");
        final File resourceFile = new File(subDirectory, "fakeImage.jpg");

        // when
        FileUtils.write(resourceFile, "Supposedly image content", UTF_8);
        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // then
        final File target = new File(subDirectory, resourceFile.getName());
        consoleHolder.awaitProcessingAllSources();
        assertThat(FileUtils.readFileToString(target, UTF_8))
                .isEqualTo("Supposedly image content");

        // and when
        final File newResourceFile = new File(subDirectory, "fakeImage-2.jpg");
        FileUtils.write(newResourceFile, "Supposedly NEW image content!!", UTF_8);

        // then
        consoleHolder.awaitProcessingResource();
        assertThat(FileUtils.readFileToString(newResourceFile, UTF_8))
                .isEqualTo("Supposedly NEW image content!!");
        assertThat(resourceFile)
                .exists();

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    @Test
    public void should_run_full_convert_with_refresh_command() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final File sourceFile = new File(srcDir, "sourceFile.asciidoc");

        // when
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        Thread mojoThread = runMojoAsynchronously(srcDir, outputDir);

        // then
        consoleHolder.awaitProcessingAllSources();

        // and when
        consoleHolder.input("refresh");

        // then
        consoleHolder.awaitProcessingAllSources();

        // cleanup
        consoleHolder.input("exit");
        consoleHolder.release();
        awaitTermination(mojoThread);
    }

    private Thread runMojoAsynchronously(Consumer<AsciidoctorRefreshMojo> mojoConfigurator) {
        final AsciidoctorRefreshMojo mojo = newFakeRefreshMojo();
        mojoConfigurator.accept(mojo);
        Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (MojoExecutionException | MojoFailureException e) {
            }
        });
        mojoThread.start();
        return mojoThread;
    }

    private Thread runMojoAsynchronously(File srcDir, File outputDir) {
        return runMojoAsynchronously(mojo -> {
            mojo.backend = "html5";
            mojo.sourceDirectory = srcDir;
            mojo.outputDirectory = outputDir;
        });
    }

    @SneakyThrows
    private void awaitTermination(Thread thread) {
        int pollTime = 250;
        int ticks = (10 * 1000 / pollTime);
        while (thread.isAlive()) {
            ticks--;
            if (ticks == 0)
                throw new InterruptedException("Max wait time reached");
            else
                Thread.sleep(pollTime);
        }
    }

}
