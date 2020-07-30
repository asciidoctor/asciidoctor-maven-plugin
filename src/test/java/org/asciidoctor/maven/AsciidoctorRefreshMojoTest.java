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
    public void should_auto_convert_file_in_root_when_source_is_updated() throws IOException {

        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.hold();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final File sourceFile = new File(srcDir, "sourceFile.asciidoc");
        if (sourceFile.exists())
            sourceFile.delete();

        // when
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        runMojoAsynchronously(srcDir, outputDir);

        // when
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
        consoleHolder.release();
    }

    @Test
    public void should_auto_convert_file_in_subDir_when_source_is_updated() throws IOException {

        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.hold();

        final File srcDir = newOutputTestDirectory("refresh-mojo");
        final File outputDir = newOutputTestDirectory("refresh-mojo");

        final File sourceFile = new File(new File(srcDir, "sub-dir1/sub_dir2"), "sourceFile.asciidoc");
        if (sourceFile.exists())
            sourceFile.delete();

        // when
        FileUtils.write(sourceFile, "= Document Title\n\nThis is test, only a test.", UTF_8);
        runMojoAsynchronously(srcDir, outputDir);

        // when
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
        consoleHolder.release();
    }

    private void runMojoAsynchronously(File srcDir, File outputDir) {
        final AsciidoctorMojo mojo = newFakeRefreshMojo();
        mojo.backend = "html";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (MojoExecutionException | MojoFailureException e) {
            }
            System.out.println("end");
        });
        mojoThread.start();
    }

}
