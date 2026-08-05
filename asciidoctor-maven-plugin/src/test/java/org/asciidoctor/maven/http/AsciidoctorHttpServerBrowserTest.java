package org.asciidoctor.maven.http;

import java.io.File;
import java.util.Random;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.asciidoctor.maven.io.TestFilesHelper.createFileWithContent;
import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.assertj.core.api.Assertions.assertThat;

class AsciidoctorHttpServerBrowserTest {

    private static final Random RANDOM = new Random();


    @Test
    void should_expose_changed_last_modified_to_browser_HEAD_polls_like_live_js() {
        // given
        int port = randomPort();
        final File outputDir = newOutputTestDirectory();
        final String testContent = "<body>Test HTML</body>";
        final File file = createFileWithContent(outputDir, "index.html", testContent);
        file.setLastModified(file.lastModified() - 5_000);
        final String existingResource = "http://localhost:" + port + "/index";

        final AsciidoctorHttpServer server =
                new AsciidoctorHttpServer(Mockito.mock(Log.class), port, outputDir, "index")
                        .start();

        final Playwright playwright = Playwright.create();
        final Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        final Page page = browser.newPage();
        page.navigate(existingResource);
        final String pollLastModified = "async () => {" +
                "const response = await fetch(window.location.href, { method: 'HEAD', cache: 'no-store' });" +
                "return response.headers.get('last-modified');" +
                "}";

        // when
        final String firstPoll = page.evaluate(pollLastModified).toString();

        file.setLastModified(file.lastModified() + 5_000);
        final String secondPoll = page.evaluate(pollLastModified).toString();

        // then
        assertThat(firstPoll)
                .isNotBlank()
                .isNotEqualTo(secondPoll);

        // cleanup
        browser.close();
        playwright.close();
        server.stop();
    }

    private int randomPort() {
        return 1000 + RANDOM.nextInt(8000);
    }
}