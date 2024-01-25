package org.asciidoctor.maven.process;

import org.apache.commons.io.FileUtils;
import org.asciidoctor.maven.AsciidoctorMojo;
import org.asciidoctor.maven.test.TestUtils.ResourceBuilder;
import org.asciidoctor.maven.model.Resource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.asciidoctor.maven.io.TestFilesHelper.createFileWithContent;
import static org.asciidoctor.maven.process.CopyResourcesProcessor.IGNORED_FILE_NAMES;
import static org.assertj.core.api.Assertions.assertThat;

public class CopyResourcesProcessorTest {

    @TempDir
    File sourceDir;
    @TempDir
    File outputDir;

    CopyResourcesProcessor resourceProcessor = new CopyResourcesProcessor();


    @Test
    void should_not_fail_when_source_does_not_exist() {
        File nonExistentDir = new File(sourceDir, UUID.randomUUID().toString());
        AsciidoctorMojo config = new AsciidoctorMojo();

        resourceProcessor.process(nonExistentDir, outputDir, config);

        assertThat(sourceDir).exists();
        assertThat(outputDir).exists();
        assertThat(nonExistentDir).doesNotExist();
    }

    @Test
    void should_not_fail_when_output_does_not_exist() {
        File nonExistentDir = new File(sourceDir, UUID.randomUUID().toString());
        AsciidoctorMojo config = new AsciidoctorMojo();

        resourceProcessor.process(sourceDir, nonExistentDir, config);

        assertThat(sourceDir).exists();
        assertThat(outputDir).exists();
        assertThat(nonExistentDir).doesNotExist();
    }

    @Test
    void should_ignore_asciidoc_source_files() {
        final String[] fileExtensions = {"adoc", "ad", "asc", "asciidoc"};
        for (String fileExtension : fileExtensions)
            createFileWithContent(sourceDir, "source." + fileExtension);

        resourceProcessor.process(sourceDir, outputDir, new AsciidoctorMojo());

        assertThat(sourceDir.listFiles()).hasSize(fileExtensions.length);
        assertThat(outputDir.listFiles()).hasSize(0);
    }

    @Test
    void should_ignore_docinfo_files() {
        for (String filename : IGNORED_FILE_NAMES) {
            if (filename.contains("*"))
                createFileWithContent(sourceDir, filename.replace("*", "source"));
            else
                createFileWithContent(sourceDir, filename);
        }

        resourceProcessor.process(sourceDir, outputDir, new AsciidoctorMojo());

        assertThat(sourceDir.listFiles()).hasSize(IGNORED_FILE_NAMES.length);
        assertThat(outputDir.listFiles()).hasSize(0);
    }

    @Test
    void should_ignore_sourceDocumentName() {
        final String sourceDocumentName = "my-file.special";
        AsciidoctorMojo config = new AsciidoctorMojo();
        config.setSourceDocumentName(sourceDocumentName);

        createFileWithContent(sourceDir, sourceDocumentName);

        resourceProcessor.process(sourceDir, outputDir, config);

        assertThat(outputDir.listFiles()).hasSize(0);
    }

    @Test
    void should_ignore_sourceDocumentExtensions() {
        final List<String> fileExtensions = Arrays.asList("ext1", "ext2", "exta", "extb");
        AsciidoctorMojo config = new AsciidoctorMojo();
        config.setSourceDocumentExtensions(fileExtensions);

        for (String fileExtension : fileExtensions)
            createFileWithContent(sourceDir, "source." + fileExtension);

        resourceProcessor.process(sourceDir, outputDir, config);

        assertThat(outputDir.listFiles()).hasSize(0);
    }

    @Test
    void should_copy_resources_in_root_source_directory() {
        createFileWithContent(sourceDir, "image.jpg");
        createFileWithContent(sourceDir, "image.gif");

        resourceProcessor.process(sourceDir, outputDir, new AsciidoctorMojo());

        assertThat(outputDir.list())
                .containsExactlyInAnyOrder("image.jpg", "image.gif");
    }

    @Test
    void should_not_copy_empty_directories() throws IOException {
        FileUtils.forceMkdir(new File(sourceDir, "sub_1"));
        FileUtils.forceMkdir(new File(sourceDir, "sub_2"));
        FileUtils.forceMkdir(new File(sourceDir, "sub_1/sub_1_2"));

        resourceProcessor.process(sourceDir, outputDir, new AsciidoctorMojo());

        assertThat(outputDir.list())
                .hasSize(0);
    }

    @Nested
    class WithoutResourcesSet {

        @Test
        void should_copy_resources_in_root_source_directory() {
            createFileWithContent(sourceDir, "image.jpg");
            createFileWithContent(sourceDir, "image.gif");

            resourceProcessor.process(sourceDir, outputDir, new AsciidoctorMojo());

            assertThat(outputDir.list())
                    .containsExactlyInAnyOrder("image.jpg", "image.gif");
        }

        @Test
        void should_copy_resources_in_sub_directories() {
            createFileWithContent(new File(sourceDir, "sub_1"), "image.jpg");
            createFileWithContent(new File(sourceDir, "sub_2"), "image.gif");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image.bmp");

            resourceProcessor.process(sourceDir, outputDir, new AsciidoctorMojo());

            assertThat(outputDir.list())
                    .containsExactlyInAnyOrder("sub_1", "sub_2");
            assertThat(new File(outputDir, "sub_1").list())
                    .containsExactlyInAnyOrder("image.jpg", "sub_1_2");
            assertThat(new File(outputDir, "sub_2").list())
                    .containsExactlyInAnyOrder("image.gif");
            assertThat(new File(outputDir, "sub_1/sub_1_2").list())
                    .containsExactlyInAnyOrder("image.bmp");
        }
    }

    @Nested
    class WithResourcesSet {

        @Test
        void should_only_copy_resources_in_single_include() {
            Resource resource = new ResourceBuilder()
                    .directory(sourceDir.getAbsolutePath())
                    .includes("**/*.txt")
                    .build();
            AsciidoctorMojo configuration = new AsciidoctorMojo();
            configuration.setResources(Arrays.asList(resource));

            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.txt");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.ignore");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.txt");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.txt");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.ignore");

            resourceProcessor.process(sourceDir, outputDir, configuration);

            assertThat(outputDir.list())
                    .containsExactlyInAnyOrder("sub_1", "sub_2");
            assertThat(new File(outputDir, "sub_1").list())
                    .containsExactlyInAnyOrder("image-1.txt", "sub_1_2");
            assertThat(new File(outputDir, "sub_2").list())
                    .containsExactlyInAnyOrder("image-2.txt");
            assertThat(new File(outputDir, "sub_1/sub_1_2").list())
                    .containsExactlyInAnyOrder("image-3.txt");
        }

        @Test
        void should_only_copy_resources_with_multiple_includes() {
            Resource resource = new ResourceBuilder()
                    .directory(sourceDir.getAbsolutePath())
                    .includes("**/*.txt", "**/*.doc")
                    .build();
            AsciidoctorMojo configuration = new AsciidoctorMojo();
            configuration.setResources(Arrays.asList(resource));

            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.txt");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.doc");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.img");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.ignore");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.txt");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.doc");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.img");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.txt");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.doc");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.img");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.ignore");

            resourceProcessor.process(sourceDir, outputDir, configuration);

            assertThat(outputDir.list())
                    .containsExactlyInAnyOrder("sub_1", "sub_2");
            assertThat(new File(outputDir, "sub_1").list())
                    .containsExactlyInAnyOrder("image-1.txt", "image-1.doc", "sub_1_2");
            assertThat(new File(outputDir, "sub_2").list())
                    .containsExactlyInAnyOrder("image-2.txt", "image-2.doc");
            assertThat(new File(outputDir, "sub_1/sub_1_2").list())
                    .containsExactlyInAnyOrder("image-3.txt", "image-3.doc");
        }

        @Test
        void should_only_copy_resources_in_multiple_includes() {
            Resource resource1 = new ResourceBuilder()
                    .directory(sourceDir.getAbsolutePath())
                    .includes("**/*.txt", "**/*.doc")
                    .build();
            Resource resource2 = new ResourceBuilder()
                    .directory(sourceDir.getAbsolutePath())
                    .includes("**/*.img")
                    .build();
            AsciidoctorMojo configuration = new AsciidoctorMojo();
            configuration.setResources(Arrays.asList(resource1, resource2));

            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.txt");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.doc");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.img");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.ignore");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.txt");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.doc");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.img");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.txt");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.doc");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.img");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.ignore");

            resourceProcessor.process(sourceDir, outputDir, configuration);

            assertThat(outputDir.list())
                    .containsExactlyInAnyOrder("sub_1", "sub_2");
            assertThat(new File(outputDir, "sub_1").list())
                    .containsExactlyInAnyOrder("image-1.txt", "image-1.doc", "image-1.img", "sub_1_2");
            assertThat(new File(outputDir, "sub_2").list())
                    .containsExactlyInAnyOrder("image-2.txt", "image-2.doc", "image-2.img");
            assertThat(new File(outputDir, "sub_1/sub_1_2").list())
                    .containsExactlyInAnyOrder("image-3.txt", "image-3.doc", "image-3.img");
        }

        @Test
        void should_copy_resources_without_including_excluded_files() {
            Resource resource = new ResourceBuilder()
                    .directory(sourceDir.getAbsolutePath())
                    .excludes("**/*.img")
                    .build();
            AsciidoctorMojo configuration = new AsciidoctorMojo();
            configuration.setResources(Arrays.asList(resource));

            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.img");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.txt");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.img");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.txt");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.img");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.txt");

            resourceProcessor.process(sourceDir, outputDir, configuration);

            assertThat(outputDir.list())
                    .containsExactlyInAnyOrder("sub_1", "sub_2");
            assertThat(new File(outputDir, "sub_1").list())
                    .containsExactlyInAnyOrder("image-1.txt", "sub_1_2");
            assertThat(new File(outputDir, "sub_2").list())
                    .containsExactlyInAnyOrder("image-2.txt");
            assertThat(new File(outputDir, "sub_1/sub_1_2").list())
                    .containsExactlyInAnyOrder("image-3.txt");
        }

        @Test
        void should_copy_resources_into_custom_relative_target_path() {
            String targetPath = UUID.randomUUID().toString();
            Resource resource = new ResourceBuilder()
                    .directory(sourceDir.getAbsolutePath())
                    .targetPath(targetPath)
                    .excludes("**/*.img")
                    .build();
            AsciidoctorMojo configuration = new AsciidoctorMojo();
            configuration.setResources(Arrays.asList(resource));

            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.txt");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.doc");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.img");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.txt");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.doc");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.img");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.txt");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.doc");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.img");

            resourceProcessor.process(sourceDir, outputDir, configuration);

            assertThat(outputDir.list())
                    .hasSize(1);
            final File targetFile = new File(outputDir + "/" + targetPath);
            assertThat(targetFile.list())
                    .containsExactlyInAnyOrder("sub_1", "sub_2");
            assertThat(new File(targetFile, "sub_1").list())
                    .containsExactlyInAnyOrder("image-1.txt", "image-1.doc", "sub_1_2");
            assertThat(new File(targetFile, "sub_2").list())
                    .containsExactlyInAnyOrder("image-2.txt", "image-2.doc");
            assertThat(new File(targetFile, "sub_1/sub_1_2").list())
                    .containsExactlyInAnyOrder("image-3.txt", "image-3.doc");
        }

        @Test
        void should_copy_resources_into_custom_absolute_target_path() {
            String targetPath = outputDir.getAbsolutePath() + "/" + UUID.randomUUID();
            Resource resource = new ResourceBuilder()
                    .directory(sourceDir.getAbsolutePath())
                    .targetPath(targetPath)
                    .excludes("**/*.img")
                    .build();
            AsciidoctorMojo configuration = new AsciidoctorMojo();
            configuration.setResources(Arrays.asList(resource));

            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.txt");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.doc");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.img");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.txt");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.doc");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.img");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.txt");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.doc");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.img");

            resourceProcessor.process(sourceDir, outputDir, configuration);

            assertThat(outputDir.list())
                    .hasSize(1);
            final File targetFile = new File(targetPath);
            assertThat(targetFile.list())
                    .containsExactlyInAnyOrder("sub_1", "sub_2");
            assertThat(new File(targetFile, "sub_1").list())
                    .containsExactlyInAnyOrder("image-1.txt", "image-1.doc", "sub_1_2");
            assertThat(new File(targetFile, "sub_2").list())
                    .containsExactlyInAnyOrder("image-2.txt", "image-2.doc");
            assertThat(new File(targetFile, "sub_1/sub_1_2").list())
                    .containsExactlyInAnyOrder("image-3.txt", "image-3.doc");
        }

        @Test
        void should_not_copy_any_resource() {
            List<Resource> resources = ResourceBuilder.excludeAll();
            AsciidoctorMojo configuration = new AsciidoctorMojo();
            configuration.setResources(resources);

            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.txt");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.doc");
            createFileWithContent(new File(sourceDir, "sub_1"), "image-1.img");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.txt");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.doc");
            createFileWithContent(new File(sourceDir, "sub_2"), "image-2.img");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.txt");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.doc");
            createFileWithContent(new File(sourceDir, "sub_1/sub_1_2"), "image-3.img");

            resourceProcessor.process(sourceDir, outputDir, configuration);

            assertThat(outputDir.list()).hasSize(0);
        }
    }
}
