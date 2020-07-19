package org.asciidoctor.maven.test

class AsciidoctorMojoTestHelper {

    static int getAvailablePort() {
        ServerSocket socket = new ServerSocket(0)
        def port = socket.getLocalPort()
        socket.close()
        return port
    }

    static File newOutputTestDirectory() {
        new File("target/asciidoctor-test-output/${UUID.randomUUID()}")
    }

    static File newOutputTestDirectory(String subDir) {
        new File("target/asciidoctor-test-output/${subDir}/${UUID.randomUUID()}")
    }

}
