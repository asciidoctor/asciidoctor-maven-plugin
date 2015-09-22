package org.asciidoctor.maven.test

class AsciidoctorMojoTestHelper {
    def getAvailablePort() {
        ServerSocket socket = new ServerSocket(0)
        def port = socket.getLocalPort()
        socket.close()
        return port
    }
}
