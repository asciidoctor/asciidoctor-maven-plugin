package org.asciidoctor.maven.refresh;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ResourcesPatternBuilderTest {

    private static final String DOC_INFO_FILES = "docinfo\\.html|docinfo-header\\.html|docinfo-footer\\.html|\\.*-docinfo\\.html|\\.*-docinfo-header\\.html|\\.*-docinfo-footer\\.html|docinfo\\.xml|docinfo-header\\.xml|docinfo-footer\\.xml|\\.*-docinfo\\.xml|\\.*-docinfo-header\\.xml|\\.*-docinfo-footer\\.xml";
    private static final String ASCIIDOC_SOURCES = "(a((sc(iidoc)?)|d(oc)?))";

    @Test
    void should_build_default_pattern() {
        // given
        ResourcesPatternBuilder patternBuilder = new ResourcesPatternBuilder("", Collections.emptyList());
        // when
        final String pattern = patternBuilder.build();
        // then
        assertThat(pattern)
                .isEqualTo("^(?!(" + DOC_INFO_FILES + "))[^_.].*\\.(?!" + ASCIIDOC_SOURCES + ").*$");
    }

    @Test
    void should_build_pattern_with_sourceDocumentName() {
        // given
        ResourcesPatternBuilder patternBuilder = new ResourcesPatternBuilder("fixed-source.name", Collections.emptyList());
        // when
        final String pattern = patternBuilder.build();
        // then
        assertThat(pattern)
                .isEqualTo("^(?!(" + DOC_INFO_FILES + "|fixed-source.name))[^_.].*\\.(?!" + ASCIIDOC_SOURCES + ").*$");
    }

    @Test
    void should_build_pattern_with_sourceDocumentExtensions() {
        // given
        ResourcesPatternBuilder patternBuilder = new ResourcesPatternBuilder("", Arrays.asList("my-docs", "md"));
        // when
        final String pattern = patternBuilder.build();
        // then
        assertThat(pattern)
                .isEqualTo("^(?!(" + DOC_INFO_FILES + "))[^_.].*\\.(?!(a((sc(iidoc)?)|d(oc)?)|my-docs|md)).*$");
    }
}
