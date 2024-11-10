package org.asciidoctor.maven.site;

import javax.inject.Singleton;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Document;

/**
 * Asciidoctor conversion wrapper for maven-site integration.
 * In addition to conversion, handles header metadata extraction.
 *
 * @author abelsromero
 * @since 3.0.0
 */
@Singleton
class SiteConverterDecorator {

    Result process(Asciidoctor asciidoctor, String content, Options options) {
        final Document document = asciidoctor.load(content, headerProcessingMetadata(options));
        final HeaderMetadata headerMetadata = HeaderMetadata.from(document);

        final String html = asciidoctor.convert(content, options);

        return new Result(headerMetadata, html);
    }

    private static Options headerProcessingMetadata(Options options) {
        Map<String, Object> optionsMap = options.map();
        OptionsBuilder builder = Options.builder();
        for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
            builder.option(entry.getKey(), entry.getValue());
        }

        builder.parseHeaderOnly(Boolean.TRUE);
        return builder.build();
    }

    /**
     * Simple tuple to return Asciidoctor extracted metadata and conversion result.
     */
    final class Result {

        private final HeaderMetadata headerMetadata;
        private final String html;

        Result(HeaderMetadata headerMetadata, String html) {
            this.headerMetadata = headerMetadata;
            this.html = html;
        }

        HeaderMetadata getHeaderMetadata() {
            return headerMetadata;
        }

        String getHtml() {
            return html;
        }
    }

}
