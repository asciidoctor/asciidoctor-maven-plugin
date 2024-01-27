package org.asciidoctor.maven.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.asciidoctor.maven.commons.AsciidoctorHelper.addAttributes;
import static org.assertj.core.api.Assertions.assertThat;

public class AsciidoctorHelperTest {

    @ParameterizedTest
    @MethodSource("specialAttributes")
    void should_add_attributes_with_special_values(Object actual, String expected) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("toc", actual);
        final AttributesBuilder attributesBuilder = Attributes.builder();

        addAttributes(attributes, attributesBuilder);

        var attributesAsMap = attributesBuilder.build().map();
        assertThat(attributesAsMap)
                .containsExactly(MapEntry.entry("toc", expected));
    }

    private static Stream<Arguments> specialAttributes() {
        return Stream.of(
                Arguments.of(null, ""),
                Arguments.of("", ""),
                Arguments.of("true", ""),
                Arguments.of("false", null),
                Arguments.of(true, ""),
                Arguments.of(false, null)
        );
    }
}
