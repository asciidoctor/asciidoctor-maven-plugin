package org.asciidoctor.maven;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.asciidoctor.maven.io.TestFilesHelper;
import org.asciidoctor.maven.io.UserInputSimulator;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.asciidoctor.maven.TestUtils.mockAsciidoctorHttpMojo;
import static org.assertj.core.api.Assertions.assertThat;

class AsciidoctorHttpMojoTest {

    @Test
    void http_front_should_let_access_converted_files() throws IOException {
        // given
        File srcDir = new File("target/test-classes/src/asciidoctor-http");
        File outputDir = TestFilesHelper.newOutputTestDirectory("http-mojo");

        UserInputSimulator userInput = new UserInputSimulator();
        userInput.type("exit\r\n");

        int httpPort = getAvailablePort();

        FileUtils.write(new File(srcDir, "content.asciidoc"),
                "= Document Title\n\nThis is test, only a test.", UTF_8);

        AsciidoctorHttpMojo mojo = mockAsciidoctorHttpMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.port = httpPort;
        mojo.home = "index";
        Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (MojoExecutionException | MojoFailureException e) {
                e.printStackTrace();
            }
        });
        mojoThread.start();

        // then
        userInput.waitForCompletion();
        assertThat(responseContent("http://localhost:" + httpPort + "/content"))
                .contains("This is test, only a test", "</html>");

        // cleanup
        userInput.stop();
        awaitTermination(mojoThread);
    }

    @Test
    void should_return_default_page() throws IOException {
        // given
        File srcDir = new File("target/test-classes/src/asciidoctor-http-default");
        File outputDir = TestFilesHelper.newOutputTestDirectory("http-mojo");

        UserInputSimulator userInput = new UserInputSimulator();
        userInput.type("exit\r\nexit\r\nexit\r\n");

        int httpPort = getAvailablePort();

        FileUtils.write(new File(srcDir, "content.asciidoc"),
                "= Document Title\n\nDEFAULT", UTF_8);

        AsciidoctorHttpMojo mojo = mockAsciidoctorHttpMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.port = httpPort;
        mojo.home = "content";
        Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (MojoExecutionException | MojoFailureException e) {
                e.printStackTrace();
            }
        });
        mojoThread.start();

        // when
        userInput.waitForCompletion();
        assertThat(responseContent("http://localhost:" + httpPort + "/content"))
                .contains("DEFAULT");

        // cleanup
        userInput.stop();
        awaitTermination(mojoThread);
    }

    @Test
    void should_return_404_when_file_does_not_exist() {
        // given
        File emptySrcDir = new File("some_path");
        File outputDir = TestFilesHelper.newOutputTestDirectory("http-mojo");

        UserInputSimulator userInput = new UserInputSimulator();
        userInput.type("exit\r\nexit\r\nexit\r\n");

        int httpPort = getAvailablePort();

        AsciidoctorHttpMojo mojo = mockAsciidoctorHttpMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = emptySrcDir;
        mojo.outputDirectory = outputDir;
        mojo.port = httpPort;
        mojo.home = "content";
        Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (MojoExecutionException | MojoFailureException e) {
                e.printStackTrace();
            }
        });
        mojoThread.start();

        // then
        userInput.waitForCompletion();
        assertThat(responseStatus("http://localhost:" + httpPort, "GET"))
                .isEqualTo(404);

        // cleanup
        userInput.stop();
        awaitTermination(mojoThread);
    }

    @Test
    void should_return_405_when_method_is_not_POST() {
        // given
        File emptySrcDir = new File("some_path");
        File outputDir = TestFilesHelper.newOutputTestDirectory("http-mojo");

        UserInputSimulator userInput = new UserInputSimulator();
        userInput.type("exit\r\nexit\r\nexit\r\n");

        int httpPort = getAvailablePort();

        AsciidoctorHttpMojo mojo = mockAsciidoctorHttpMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = emptySrcDir;
        mojo.outputDirectory = outputDir;
        mojo.port = httpPort;
        mojo.home = "content";
        Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (MojoExecutionException | MojoFailureException e) {
                e.printStackTrace();
            }
        });
        mojoThread.start();

        // then
        userInput.waitForCompletion();
        assertThat(responseStatus("http://localhost:" + httpPort, "POST"))
                .isEqualTo(405);

        // cleanup
        userInput.stop();
        awaitTermination(mojoThread);
    }

    @Test
    void should_return_205_when_method_is_HEAD_and_resource_exists() {
        // given
        File emptySrcDir = new File("some_path");
        File outputDir = TestFilesHelper.newOutputTestDirectory("http-mojo");
        TestFilesHelper.createFileWithContent(outputDir, "index.html");

        UserInputSimulator userInput = new UserInputSimulator();
        userInput.type("exit\r\nexit\r\nexit\r\n");

        int httpPort = getAvailablePort();

        AsciidoctorHttpMojo mojo = mockAsciidoctorHttpMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = emptySrcDir;
        mojo.outputDirectory = outputDir;
        mojo.port = httpPort;
        mojo.home = "index";
        Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (MojoExecutionException | MojoFailureException e) {
                e.printStackTrace();
            }
        });
        mojoThread.start();

        // then
        userInput.waitForCompletion();
        assertThat(responseStatus("http://localhost:" + httpPort, "HEAD"))
                .isEqualTo(205);

        // cleanup
        userInput.stop();
        awaitTermination(mojoThread);
    }

    @SneakyThrows
    private int responseStatus(String url, String httpMethod) {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(httpMethod);
        return connection.getResponseCode();
    }

    @SneakyThrows
    private String responseContent(String url) {
        try (final BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            IOUtils.copy(in, os);
            return new String(os.toByteArray());
        }
    }

    @SneakyThrows
    private int getAvailablePort() {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
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
