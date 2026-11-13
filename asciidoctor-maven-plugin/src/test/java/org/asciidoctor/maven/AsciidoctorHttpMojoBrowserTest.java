package org.asciidoctor.maven;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.asciidoctor.maven.io.ConsoleHolder;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.asciidoctor.maven.test.TestUtils.mockAsciidoctorHttpMojo;
import static org.assertj.core.api.Assertions.assertThat;

class AsciidoctorHttpMojoBrowserTest {

    private static final String TEST_DIR = "http-mojo-browser";

    @Test
    void should_reload_page_in_browser_when_source_is_updated() throws IOException {
        // given
        final ConsoleHolder consoleHolder = ConsoleHolder.start();

        final File srcDir = newOutputTestDirectory(TEST_DIR);
        final File outputDir = newOutputTestDirectory(TEST_DIR);
        final File sourceFile = new File(srcDir, "index.adoc");
        FileUtils.write(sourceFile, "= Document Title\n\nInitial content.", UTF_8);

        final int httpPort = getAvailablePort();
        final Thread mojoThread = runMojoAsynchronously(srcDir, outputDir, httpPort);
        consoleHolder.awaitProcessingAllSources();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch()) {
            final Page page = browser.newPage();
            page.navigate("http://localhost:" + httpPort + "/index");
            assertThat(page.textContent("body"))
                    .contains("Initial content.");

            // when
            FileUtils.write(sourceFile, "= Document Title\n\nUpdated content.", UTF_8);
            consoleHolder.awaitProcessingSource();

            // then
            page.waitForSelector("text=Updated content.",
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(15_000));
            assertThat(page.textContent("body"))
                    .doesNotContain("Initial content.");
        } finally {
            consoleHolder.input("exit");
            consoleHolder.release();
            awaitTermination(mojoThread);
        }
    }

    private Thread runMojoAsynchronously(File srcDir, File outputDir, int httpPort) {
        final AsciidoctorHttpMojo mojo = mockAsciidoctorHttpMojo();
        mojo.backend = "html5";
        mojo.sourceDirectory = srcDir;
        mojo.outputDirectory = outputDir;
        mojo.port = httpPort;
        mojo.home = "index";
        final Thread mojoThread = new Thread(() -> {
            try {
                mojo.execute();
            } catch (MojoExecutionException | MojoFailureException e) {
                throw new RuntimeException(e);
            }
        });
        mojoThread.start();
        return mojoThread;
    }

    @SneakyThrows
    private int getAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @SneakyThrows
    private void awaitTermination(Thread thread) {
        thread.join(30_000);
        if (thread.isAlive()) {
            throw new InterruptedException("Max wait time reached");
        }
    }
}
