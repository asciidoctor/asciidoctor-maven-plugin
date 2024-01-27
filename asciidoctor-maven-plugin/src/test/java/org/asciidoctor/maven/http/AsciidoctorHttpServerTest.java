package org.asciidoctor.maven.http;

import java.io.File;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.RESET_CONTENT;
import static org.asciidoctor.maven.io.TestFilesHelper.createFileWithContent;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.assertj.core.api.Assertions.assertThat;

class AsciidoctorHttpServerTest {

    private static final Random RANDOM = new Random();


    @Test
    void should_start_and_stop_server() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        final String defaultUrl = "http://localhost:" + port + "/index";

        // when
        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, "index")
                        .start();

        final HttpResponse responseWhileStarted = doHttpGet(defaultUrl);
        server.stop();
        final HttpResponse responseOnceStopped = doHttpGet(defaultUrl);

        // then
        assertThat(responseWhileStarted)
                .isEqualTo(HttpResponse.notFound());
        assertThat(responseOnceStopped.getError())
                .isInstanceOf(ConnectException.class)
                .hasMessageStartingWith("Connection refuse");
    }

    @Test
    void should_return_404_when_resource_does_not_exist() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        final String resource = "http://localhost:" + port + "/" + UUID.randomUUID();

        // when
        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, "index")
                        .start();

        final HttpResponse response = doHttpGet(resource);

        // then
        assertThat(response)
                .isEqualTo(HttpResponse.notFound());

        // cleanup
        server.stop();
    }

    @Test
    void should_return_405_when_method_is_not_GET_or_HEAD() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        final String resource = "http://localhost:" + port + "/" + UUID.randomUUID();

        // when
        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, "index")
                        .start();

        final HttpResponse response = doHttpPost(resource);

        // then
        assertThat(response.getStatus())
                .isEqualTo(METHOD_NOT_ALLOWED.code());
        assertThat(response.getMessage())
                .isEqualTo(METHOD_NOT_ALLOWED.reasonPhrase());

        // cleanup
        server.stop();
    }

    @Test
    void should_return_205_when_method_is_HEAD_and_resource_exists() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        createFileWithContent(outputDir, "index.html");
        final String existingResource = "http://localhost:" + port + "/index";

        // when
        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, "index")
                        .start();

        final HttpResponse response = doHttpHead(existingResource);

        // then
        assertThat(response.getStatus())
                .isEqualTo(RESET_CONTENT.code());
        assertThat(response.getMessage())
                .isEqualTo(RESET_CONTENT.reasonPhrase());

        // cleanup
        server.stop();
    }

    @Test
    void should_return_404_when_method_is_HEAD_does_not_exists() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        final String existingResource = "http://localhost:" + port + "/" + UUID.randomUUID();

        // when
        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, "index")
                        .start();

        final HttpResponse response = doHttpHead(existingResource);

        // then
        assertThat(response.getStatus())
                .isEqualTo(NOT_FOUND.code());
        assertThat(response.getMessage())
                .isEqualTo(NOT_FOUND.reasonPhrase());

        // cleanup
        server.stop();
    }

    @Test
    void should_return_modified_html_content_without_modifying_original() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        final String testContent = "<body>Test HTML</body>";
        createFileWithContent(outputDir, "index.html", testContent);
        final String existingResource = "http://localhost:" + port + "/index";

        // when
        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, "index")
                        .start();

        final HttpResponse response = doHttpGet(existingResource);

        // then
        assertThat(response.getStatus())
                .isEqualTo(OK.code());
        assertThat(response.getMessage())
                .isEqualTo(OK.reasonPhrase());
        assertThat(response.getContent())
                .isEqualTo("<body>Test HTML<script src=\"http://livejs.com/live.js#html\"></script></body>");

        // cleanup
        server.stop();
    }

    @Test
    void should_return_non_html_content_without_modifications() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        final String testContent = "almost-a-css {}";
        createFileWithContent(outputDir, "styles.css", testContent);
        final String existingResource = "http://localhost:" + port + "/styles.css";

        // when
        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, "index")
                        .start();

        final HttpResponse response = doHttpGet(existingResource);

        // then
        assertThat(response.getStatus())
                .isEqualTo(OK.code());
        assertThat(response.getMessage())
                .isEqualTo(OK.reasonPhrase());
        assertThat(response.getContent())
                .isEqualTo(testContent);

        // cleanup
        server.stop();
    }

    @Test
    void should_return_default_resource_when_url_is_root() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        final String defaultResource = "my_default.html";
        createFileWithContent(outputDir, defaultResource);
        final String existingResource = "http://localhost:" + port;

        // when
        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, defaultResource)
                        .start();

        final HttpResponse response = doHttpGet(existingResource);

        // then
        assertThat(response.getStatus())
                .isEqualTo(OK.code());
        assertThat(response.getMessage())
                .isEqualTo(OK.reasonPhrase());
        assertThat(response.getContent())
                .isEqualTo("Test content");

        // cleanup
        server.stop();
    }


    private int randomPort() {
        return 1000 + RANDOM.nextInt(8000);
    }

    private HttpResponse doHttpGet(String url) {
        return doHttp(url, "GET");
    }

    private HttpResponse doHttpPost(String url) {
        return doHttp(url, "POST");
    }

    private HttpResponse doHttpHead(String url) {
        return doHttp(url, "HEAD");
    }

    @SneakyThrows
    private HttpResponse doHttp(String url, String method) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            int status = connection.getResponseCode();

            // due to HttpURLConnection returning FileNotFound exception on 404
            if (status == 404)
                return HttpResponse.successful(status, connection.getResponseMessage(), null);

            InputStream is = (status >= 200 && status < 206)
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            return HttpResponse.successful(
                    status,
                    connection.getResponseMessage(),
                    IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return HttpResponse.withError(e);
        }
    }

    @Value
    static class HttpResponse {
        Integer status;
        String message;
        String content;

        Exception error;

        static HttpResponse successful(int status, String message, String content) {
            return new HttpResponse(status, message, content, null);
        }

        static HttpResponse withError(Exception e) {
            return new HttpResponse(null, null, null, e);
        }

        static HttpResponse notFound() {
            return successful(NOT_FOUND.code(), "Not Found", null);
        }
    }
}
