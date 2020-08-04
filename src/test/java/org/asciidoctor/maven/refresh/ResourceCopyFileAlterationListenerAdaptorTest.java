package org.asciidoctor.maven.refresh;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.maven.AsciidoctorRefreshMojo;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.assertj.core.api.Assertions.assertThat;

public class ResourceCopyFileAlterationListenerAdaptorTest {

    private static final String TEST_DIR = "resource-copy-listener";

    private static final Runnable EMPTY_RUNNABLE = () -> {
    };


    @Test
    public void should_copy_files_when_match_resource_includes() throws IOException {
        // given
        final File srcDir = newOutputTestDirectory(TEST_DIR);
        final File outputDir = newOutputTestDirectory(TEST_DIR);

        final AsciidoctorRefreshMojo mojo = new AsciidoctorRefreshMojo();
        final Log logSpy = Mockito.spy(Log.class);
        final ResourceCopyFileAlterationListenerAdaptor listenerAdaptor
                = new ResourceCopyFileAlterationListenerAdaptor(mojo, EMPTY_RUNNABLE, logSpy);

        mojo.setBackend("html5");
        mojo.setSourceDirectory(srcDir);
        mojo.setOutputDirectory(outputDir);
        mojo.setResources(Arrays.asList(((Supplier<Resource>) () -> {
            Resource resource = new Resource();
            resource.setDirectory(srcDir.getAbsolutePath());
            resource.setIncludes(Arrays.asList("**/*.jpg", "**/*.gif"));
            return resource;
        }).get()));

        // when
        final File resourceFile1 = new File(srcDir, "file.jpg");
        FileUtils.write(resourceFile1, "Test content", UTF_8);
        final File resourceFile2 = new File(srcDir, "file.gif");
        FileUtils.write(resourceFile2, "Test content", UTF_8);
        final File resourceFile3 = new File(srcDir, "file.txt");
        FileUtils.write(resourceFile3, "Test content", UTF_8);

        listenerAdaptor.processFile(resourceFile1, "update");
        listenerAdaptor.processFile(resourceFile2, "update");
        listenerAdaptor.processFile(resourceFile3, "update");

        // then
        assertThat(new File(outputDir, resourceFile1.getName()))
                .exists();
        assertThat(new File(outputDir, resourceFile2.getName()))
                .exists();
        assertThat(new File(outputDir, resourceFile3.getName()))
                .doesNotExist();
    }

    @Test
    public void should_not_copy_files_when_match_resource_excludes() throws IOException {
        // given
        final File srcDir = newOutputTestDirectory(TEST_DIR);
        final File outputDir = newOutputTestDirectory(TEST_DIR);

        final AsciidoctorRefreshMojo mojo = new AsciidoctorRefreshMojo();
        final Log logSpy = Mockito.spy(Log.class);
        final ResourceCopyFileAlterationListenerAdaptor listenerAdaptor
                = new ResourceCopyFileAlterationListenerAdaptor(mojo, EMPTY_RUNNABLE, logSpy);

        mojo.setBackend("html5");
        mojo.setSourceDirectory(srcDir);
        mojo.setOutputDirectory(outputDir);
        mojo.setResources(Arrays.asList(((Supplier<Resource>) () -> {
            Resource resource = new Resource();
            resource.setDirectory(srcDir.getAbsolutePath());
            resource.setExcludes(Arrays.asList("**/*.jpg", "**/*.gif"));
            return resource;
        }).get()));

        // when
        final File resourceFile1 = new File(srcDir, "file.jpg");
        FileUtils.write(resourceFile1, "Test content", UTF_8);
        final File resourceFile2 = new File(srcDir, "file.gif");
        FileUtils.write(resourceFile2, "Test content", UTF_8);
        final File resourceFile3 = new File(srcDir, "file.txt");
        FileUtils.write(resourceFile3, "Test content", UTF_8);

        listenerAdaptor.processFile(resourceFile1, "update");
        listenerAdaptor.processFile(resourceFile2, "update");
        listenerAdaptor.processFile(resourceFile3, "update");

        // then
        assertThat(new File(outputDir, resourceFile1.getName()))
                .doesNotExist();
        assertThat(new File(outputDir, resourceFile2.getName()))
                .doesNotExist();
        assertThat(new File(outputDir, resourceFile3.getName()))
                .exists();
    }

    @Test
    public void should_copy_and_ignore_files_when_matching_both_resource_includes_and_excludes() throws IOException {
        // given
        final File srcDir = newOutputTestDirectory(TEST_DIR);
        final File outputDir = newOutputTestDirectory(TEST_DIR);

        final AsciidoctorRefreshMojo mojo = new AsciidoctorRefreshMojo();
        final Log logSpy = Mockito.spy(Log.class);
        final ResourceCopyFileAlterationListenerAdaptor listenerAdaptor
                = new ResourceCopyFileAlterationListenerAdaptor(mojo, EMPTY_RUNNABLE, logSpy);

        mojo.setBackend("html5");
        mojo.setSourceDirectory(srcDir);
        mojo.setOutputDirectory(outputDir);
        mojo.setResources(Arrays.asList(((Supplier<Resource>) () -> {
            Resource resource = new Resource();
            resource.setDirectory(srcDir.getAbsolutePath());
            resource.setIncludes(Arrays.asList("**/*.jpg", "**/*.gif"));
            resource.setExcludes(Arrays.asList("**/*.txt"));
            return resource;
        }).get()));

        // when
        final File resourceFile1 = new File(srcDir, "file.jpg");
        FileUtils.write(resourceFile1, "Test content", UTF_8);
        final File resourceFile2 = new File(srcDir, "file.gif");
        FileUtils.write(resourceFile2, "Test content", UTF_8);
        final File resourceFile3 = new File(srcDir, "file.txt");
        FileUtils.write(resourceFile3, "Test content", UTF_8);

        listenerAdaptor.processFile(resourceFile1, "update");
        listenerAdaptor.processFile(resourceFile2, "update");
        listenerAdaptor.processFile(resourceFile3, "update");

        // then
        assertThat(new File(outputDir, resourceFile1.getName()))
                .exists();
        assertThat(new File(outputDir, resourceFile2.getName()))
                .exists();
        assertThat(new File(outputDir, resourceFile3.getName()))
                .doesNotExist();
    }

}
