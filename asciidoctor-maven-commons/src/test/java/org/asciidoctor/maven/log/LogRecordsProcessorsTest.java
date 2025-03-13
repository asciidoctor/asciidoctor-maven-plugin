package org.asciidoctor.maven.log;

import java.util.ArrayList;
import java.util.List;

import static org.asciidoctor.log.Severity.*;
import static org.asciidoctor.maven.log.TestLogRecords.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LogRecordsProcessorsTest {

    @Test
    void should_not_fail_with_empty_data() {
        final var config = new org.asciidoctor.maven.log.LogHandler();
        final var recordsProcessor = new LogRecordsProcessors(config, null, null);

        final var logHandler = new MemoryLogHandler(null, null);
        Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

        assertThat(throwable).isNull();
    }

    @Nested
    class Severity {

        @Test
        void should_report_messages_when_severity_is_error() {
            final var config = logHandlerConfig(ERROR, null);
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages)
                .containsExactlyInAnyOrder(
                    "asciidoctor: ERROR: error message 1",
                    "asciidoctor: ERROR: error message 2",
                    "asciidoctor: ERROR: error message 3");
            assertThat(throwable)
                .hasMessage("Found 3 issue(s) of severity ERROR or higher during conversion");
        }

        @Test
        void should_report_messages_when_severity_is_info() {
            final var config = logHandlerConfig(INFO, null);
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages)
                .containsExactlyInAnyOrder(
                    "asciidoctor: ERROR: error message 1",
                    "asciidoctor: ERROR: error message 2",
                    "asciidoctor: ERROR: error message 3",
                    "asciidoctor: INFO: info message 1",
                    "asciidoctor: INFO: info message 2",
                    "asciidoctor: INFO: info message 3",
                    "asciidoctor: WARN: warning message 1",
                    "asciidoctor: WARN: warning message 2",
                    "asciidoctor: WARN: warning message 3");
            assertThat(throwable)
                .hasMessage("Found 9 issue(s) of severity INFO or higher during conversion");
        }

        @Test
        void should_report_no_messages_when_severity_is_fatal() {
            final var config = logHandlerConfig(FATAL, null);
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages).isEmpty();
            assertThat(throwable).isNull();
        }
    }

    @Nested
    class Text {

        @Test
        void should_report_messages_when_text_contains_error() {
            final var config = logHandlerConfig(null, "error");
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages)
                .containsExactlyInAnyOrder(
                    "asciidoctor: ERROR: error message 1",
                    "asciidoctor: ERROR: error message 2",
                    "asciidoctor: ERROR: error message 3");
            assertThat(throwable)
                .hasMessage("Found 3 issue(s) containing 'error'");
        }

        @Test
        void should_report_messages_when_text_contains_info() {
            final var config = logHandlerConfig(null, "info");
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages)
                .containsExactlyInAnyOrder(
                    "asciidoctor: INFO: info message 1",
                    "asciidoctor: INFO: info message 2",
                    "asciidoctor: INFO: info message 3");
            assertThat(throwable)
                .hasMessage("Found 3 issue(s) containing 'info'");
        }

        @Test
        void should_report_messages_when_text_contains_warn() {
            final var config = logHandlerConfig(null, "warn");
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages)
                .containsExactlyInAnyOrder(
                    "asciidoctor: WARN: warning message 1",
                    "asciidoctor: WARN: warning message 2",
                    "asciidoctor: WARN: warning message 3");
            assertThat(throwable)
                .hasMessage("Found 3 issue(s) containing 'warn'");
        }

        @Test
        void should_report_messages_when_text_does_not_match() {
            final var config = logHandlerConfig(null, "not present");
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages).isEmpty();
            assertThat(throwable).isNull();
        }
    }

    @Nested
    class SeverityAndText {

        @Test
        void should_report_messages_when_severity_and_text_contains_error() {
            final var config = logHandlerConfig(ERROR, "error");
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages)
                .containsExactlyInAnyOrder(
                    "asciidoctor: ERROR: error message 1",
                    "asciidoctor: ERROR: error message 2",
                    "asciidoctor: ERROR: error message 3");
            assertThat(throwable)
                .hasMessage("Found 3 issue(s) matching severity ERROR or higher and text 'error'");
        }

        @Test
        void should_report_messages_when_severity_and_text_contains_info() {
            final var config = logHandlerConfig(INFO, "info");
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages)
                .containsExactlyInAnyOrder(
                    "asciidoctor: INFO: info message 1",
                    "asciidoctor: INFO: info message 2",
                    "asciidoctor: INFO: info message 3");
            assertThat(throwable)
                .hasMessage("Found 3 issue(s) matching severity INFO or higher and text 'info'");
        }

        @Test
        void should_report_messages_when_severity_and_text_contains_warn() {
            final var config = logHandlerConfig(WARN, "warn");
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages)
                .containsExactlyInAnyOrder(
                    "asciidoctor: WARN: warning message 1",
                    "asciidoctor: WARN: warning message 2",
                    "asciidoctor: WARN: warning message 3");
            assertThat(throwable)
                .hasMessage("Found 3 issue(s) matching severity WARN or higher and text 'warn'");
        }


        @Test
        void should_report_messages_when_text_return_none() {
            final var config = logHandlerConfig(FATAL, "not present");
            final List<String> messages = new ArrayList();

            final var recordsProcessor = new LogRecordsProcessors(config, null, messages::add);

            final var logHandler = testMemoryLogHandler();
            Throwable throwable = catchThrowable(() -> recordsProcessor.processLogRecords(logHandler));

            assertThat(messages).isEmpty();
            assertThat(throwable).isNull();
        }
    }

    private static LogHandler logHandlerConfig(org.asciidoctor.log.Severity severity, String text) {
        final var config = new LogHandler();
        FailIf failIf = new FailIf();
        failIf.setSeverity(severity);
        failIf.setContainsText(text);
        config.setFailIf(failIf);
        return config;
    }

    private static MemoryLogHandler testMemoryLogHandler() {
        final var memoryLogHandler = new MemoryLogHandler(null, null);
        for (int i = 1; i < 4; i++) {
            memoryLogHandler.log(errorMessage(i));
            memoryLogHandler.log(getInfoMessage(i));
            memoryLogHandler.log(warningMessage(i));
        }
        return memoryLogHandler;
    }

}
