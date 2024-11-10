package org.asciidoctor.maven.refresh;


import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.maven.AsciidoctorRefreshMojo;
import org.asciidoctor.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.asciidoctor.maven.io.TestFilesHelper.createFileWithContent;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.assertj.core.api.Assertions.assertThat;

class ResourceCopyFileAlterationListenerAdaptorTest {

    private static final String TEST_DIR = "resource-copy-listener";

    private static final Runnable EMPTY_RUNNABLE = () -> {
    };


    @Test
    void should_copy_files_when_match_resource_includes() {
        // given
        final File srcDir = newOutputTestDirectory(TEST_DIR);
        final File outputDir = newOutputTestDirectory(TEST_DIR);

        final AsciidoctorRefreshMojo mojo = createRefreshMojo();
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
        final File resourceFile1 = createFileWithContent(srcDir, "file.jpg");
        final File resourceFile2 = createFileWithContent(srcDir, "file.gif");
        final File resourceFile3 = createFileWithContent(srcDir, "file.txt");

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
    void should_not_copy_files_when_match_resource_excludes() {
        // given
        final File srcDir = newOutputTestDirectory(TEST_DIR);
        final File outputDir = newOutputTestDirectory(TEST_DIR);

        final AsciidoctorRefreshMojo mojo = createRefreshMojo();
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
        final File resourceFile1 = createFileWithContent(srcDir, "file.jpg");
        final File resourceFile2 = createFileWithContent(srcDir, "file.gif");
        final File resourceFile3 = createFileWithContent(srcDir, "file.txt");

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
    void should_copy_and_ignore_files_when_matching_both_resource_includes_and_excludes() {
        // given
        final File srcDir = newOutputTestDirectory(TEST_DIR);
        final File outputDir = newOutputTestDirectory(TEST_DIR);

        final AsciidoctorRefreshMojo mojo = createRefreshMojo();
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
        final File resourceFile1 = createFileWithContent(srcDir, "file.jpg");
        final File resourceFile2 = createFileWithContent(srcDir, "file.gif");
        final File resourceFile3 = createFileWithContent(srcDir, "file.txt");

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

    // Removal of special files is done by FileAlterationObserver, not ResourceCopyFileAlterationListenerAdaptor
    @Test
    void should_copy_special_asciidoctor_files() {
        // given
        final File srcDir = newOutputTestDirectory(TEST_DIR);
        final File outputDir = newOutputTestDirectory(TEST_DIR);

        final AsciidoctorRefreshMojo mojo = createRefreshMojo();
        final Log logSpy = Mockito.spy(Log.class);
        final ResourceCopyFileAlterationListenerAdaptor listenerAdaptor
                = new ResourceCopyFileAlterationListenerAdaptor(mojo, EMPTY_RUNNABLE, logSpy);

        mojo.setBackend("html5");
        mojo.setSourceDirectory(srcDir);
        mojo.setOutputDirectory(outputDir);

        // when
        final String randomPrefix = UUID.randomUUID().toString();
        final List<File> specialFiles = Stream.of(
                        "docinfo.html",
                        "docinfo-header.html",
                        "docinfo-footer.html",
                        randomPrefix + "-docinfo.html",
                        randomPrefix + "-docinfo-header.html",
                        randomPrefix + "-docinfo-footer.html",
                        "docinfo.xml",
                        "docinfo-header.xml",
                        "docinfo-footer.xml",
                        randomPrefix + "-docinfo.xml",
                        randomPrefix + "-docinfo-header.xml",
                        randomPrefix + "-docinfo-footer.xml")
                .map(filename -> createFileWithContent(srcDir, filename))
                .peek(file -> listenerAdaptor.processFile(file, "create"))
                .collect(Collectors.toList());

        // then
        specialFiles.forEach(specialFile -> {
            File outputCandidate = new File(outputDir, specialFile.getName());
            assertThat(outputCandidate).exists();
        });
    }

    private static AsciidoctorRefreshMojo createRefreshMojo() {
        return new AsciidoctorRefreshMojo(null, null, null, null);
    }
}
