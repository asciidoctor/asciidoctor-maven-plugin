package org.asciidoctor.maven.test;

import org.asciidoctor.maven.AsciidoctorMojo;
import org.asciidoctor.maven.log.LogHandler;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MojoMockerTest {

    private final MojoMocker mojoMocker = new MojoMocker();

    @Test
    void should_mock_mojo() {
        AsciidoctorMojo mock = mojoMocker.mock(AsciidoctorMojo.class, null, null);

        assertThat(mock).isNotNull();
    }

    @Test
    void should_mock_mojo_with_properties() {
        Map<String, String> properties = Map.of("a-key", "a-value");
        AsciidoctorMojo mock = mojoMocker.mock(AsciidoctorMojo.class, properties, null);

        assertThat(mock).isNotNull();
    }

    @Test
    void should_mock_mojo_with_logHandler() {
        AsciidoctorMojo mock = mojoMocker.mock(AsciidoctorMojo.class, null, new LogHandler());

        assertThat(mock).isNotNull();
    }
}
