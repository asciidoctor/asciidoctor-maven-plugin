package org.asciidoctor.maven.site;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.Document;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

/**
 * Asciidoctor conversion wrapper to extract metadata and
 */
class SiteConverter {

    private static final Pattern TITLE_PATTERN = Pattern.compile("^.*<h1>([^<]*)</h1>.*$", DOTALL | CASE_INSENSITIVE);

    private final Asciidoctor asciidoctor;

    SiteConverter(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    Result process(String content, Options options) {
        Document load = asciidoctor.load(content, options);
        String title1 = load.getTitle();
        List<Author> authors = load.getAuthors();

        String html = asciidoctor.convert(content, options);
        String title = extractTitle(html);
        return new Result(title, html);
    }

    private String extractTitle(String content) {
        Matcher titleMatcher = TITLE_PATTERN.matcher(content);
        return titleMatcher.matches() ? titleMatcher.group(1) : null;
    }

    final class Result {
        private final String title;
        private final String html;

        Result(String title, String html) {
            this.title = title;
            this.html = html;
        }

        public String getTitle() {
            return title;
        }

        public String getHtml() {
            return html;
        }
    }
}
