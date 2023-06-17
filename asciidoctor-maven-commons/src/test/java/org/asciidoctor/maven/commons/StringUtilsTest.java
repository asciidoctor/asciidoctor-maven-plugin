package org.asciidoctor.maven.commons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void should_detect_null_as_blank_string() {
        assertThat(isBlank(null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "\t",
            "\n\n",
            " \n\t \n ",
    })
    void should_detect_blank_string(String strings) {
        assertThat(isBlank(strings)).isTrue();
    }
}
