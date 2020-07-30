package org.asciidoctor.maven.io;

import java.io.File;
import java.util.UUID;

public class TestFilesHelper {

    public static File newOutputTestDirectory() {
        return new File("target/asciidoctor-test-output/" + UUID.randomUUID());
    }

    public static File newOutputTestDirectory(String subDir) {
        return new File("target/asciidoctor-test-output/" + subDir + "/" + UUID.randomUUID());
    }

}
