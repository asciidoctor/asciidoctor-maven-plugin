package org.asciidoctor.maven.io;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import lombok.SneakyThrows;

public class TestFilesHelper {

    public static final String TEST_OUTPUT_BASE_PATH = "target/test-outputs/";

    public static File newOutputTestDirectory() {
        return createDirectory(TEST_OUTPUT_BASE_PATH + UUID.randomUUID());
    }

    public static File newOutputTestDirectory(String subDir) {
        return createDirectory(TEST_OUTPUT_BASE_PATH + subDir + "/" + UUID.randomUUID());
    }

    private static File createDirectory(String path) {
        return new File(path);
    }

    public static File createFileWithContent(File srcDir, String filename) {
        return createFileWithContent(srcDir, filename, "Test content");
    }

    @SneakyThrows
    public static File createFileWithContent(File srcDir, String filename, String content) {
        srcDir.mkdirs();
        final File file = new File(srcDir, filename);
        Files.write(file.toPath(), content.getBytes());
        return file;
    }
}
