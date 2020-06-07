package org.asciidoctor.maven.site;

import org.asciidoctor.log.Severity;
import org.asciidoctor.maven.log.FailIf;
import org.asciidoctor.maven.log.LogHandler;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SiteLogHandlerDeserializerTest {

    @Test
    public void should_deserialize_null_logHandler() {
        // given
        final Xpp3Dom logHandlerConfig = null;
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(defaultLogHandler());
    }

    @Test
    public void should_deserialize_empty_logHandler() {
        // given
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.asciidocNode()
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(defaultLogHandler());
    }

    @Test
    public void should_deserialize_valid_outputToConsole() {
        // given
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("outputToConsole", "false")
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then
        LogHandler expected = new LogHandler();
        expected.setOutputToConsole(Boolean.FALSE);
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    public void should_deserialize_invalid_outputToConsole() {
        // given
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("outputToConsole", "text")
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then: set false as default (same as Maven does for a normal Mojo)
        LogHandler expected = new LogHandler();
        expected.setOutputToConsole(Boolean.FALSE);
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    public void should_deserialize_empty_failIf() {
        // given
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("failIf")
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then:
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(defaultLogHandler());
    }

    @Test
    public void should_deserialize_failIf_with_valid_severity() {
        // given
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("failIf")
                .addChild("severity", "INFO")
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then:
        LogHandler expected = defaultLogHandler();
        FailIf failIf = new FailIf();
        failIf.setSeverity(Severity.INFO);
        expected.setFailIf(failIf);
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    public void should_deserialize_failIf_with_invalid_severity() {
        // given
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("failIf")
                .addChild("severity", "INVALID")
                .build();
        // when
        Throwable throwable = catchThrowable(() -> new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig));
        // then: mimic Maven Mojo behaviour
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No enum constant org.asciidoctor.log.Severity.INVALID");
    }

    @Test
    public void should_deserialize_failIf_with_empty_severity() {
        // given
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("failIf")
                .addChild("severity")
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then:
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(defaultLogHandler());
    }

    @Test
    public void should_deserialize_failIf_with_containsText() {
        // given
        final String textPattern = "some words";
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("failIf")
                .addChild("containsText", textPattern)
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then:
        LogHandler expected = defaultLogHandler();
        FailIf failIf = new FailIf();
        failIf.setContainsText(textPattern);
        expected.setFailIf(failIf);
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    public void should_deserialize_failIf_empty_containsText() {
        // given
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("failIf")
                .addChild("containsText")
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then:
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(defaultLogHandler());
    }

    @Test
    public void should_deserialize_failIf_with_severity_and_containsText() {

        // given
        final String textPattern = "some words";
        final Xpp3Dom logHandlerConfig = Xpp3DoomBuilder.logHandler()
                .addChild("failIf")
                .addChild("containsText", textPattern)
                .parent()
                .addChild("severity", "FATAL")
                .build();
        // when
        LogHandler logHandler = new SiteLogHandlerDeserializer()
                .deserialize(logHandlerConfig);
        // then:
        LogHandler expected = defaultLogHandler();
        FailIf failIf = new FailIf();
        failIf.setContainsText(textPattern);
        failIf.setSeverity(Severity.FATAL);
        expected.setFailIf(failIf);
        assertThat(logHandler)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    private LogHandler defaultLogHandler() {
        LogHandler expected = new LogHandler();
        expected.setOutputToConsole(Boolean.TRUE);
        return expected;
    }
}
