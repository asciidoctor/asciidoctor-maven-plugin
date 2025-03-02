package org.asciidoctor.maven.log;

import java.util.List;

import static org.asciidoctor.log.Severity.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.log.LogRecord;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemoryLogHandlerTest {

    @Test
    void should_not_fail_when_filtering_without_records() {
        final var memoryLogHandler = new MemoryLogHandler(null, null);

        assertThat(memoryLogHandler.filter(ERROR)).hasSize(0);
        assertThat(memoryLogHandler.filter("any text")).hasSize(0);
        assertThat(memoryLogHandler.filter(INFO, "any text")).hasSize(0);

        assertThat(memoryLogHandler.filter((org.asciidoctor.log.Severity) null)).hasSize(0);
        assertThat(memoryLogHandler.filter((String) null)).hasSize(0);
        assertThat(memoryLogHandler.filter(null, null)).hasSize(0);
    }

    @Test
    void should_return_all_by_default() {
        final var memoryLogHandler = testMemoryLogHandler();

        assertThat(memoryLogHandler.filter((org.asciidoctor.log.Severity) null)).hasSize(3);
        assertThat(memoryLogHandler.filter((String) null)).hasSize(3);
        assertThat(memoryLogHandler.filter(null, null)).hasSize(3);
    }

    @Test
    void should_return_non_empty() {
        final var memoryLogHandler = new MemoryLogHandler(null, null);
        assertThat(memoryLogHandler.isEmpty()).isTrue();
    }

    @Test
    void should_return_empty() {
        final var memoryLogHandler = testMemoryLogHandler();
        assertThat(memoryLogHandler.isEmpty()).isFalse();
    }

    @Nested
    class Severity {

        @Test
        void should_filter_by_error_severity() {
            final var memoryLogHandler = testMemoryLogHandler();

            List<LogRecord> filter = memoryLogHandler.filter(ERROR);

            assertThat(filter)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(errorMessage())
                .hasSize(1);
        }

        @Test
        void should_filter_by_info_severity() {
            final var memoryLogHandler = testMemoryLogHandler();

            List<LogRecord> filter = memoryLogHandler.filter(INFO);

            assertThat(filter)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(errorMessage())
                .contains(getInfoMessage())
                .contains(warningMessage())
                .hasSize(3);
        }

        @Test
        void should_filter_by_warn_severity() {
            final var memoryLogHandler = testMemoryLogHandler();

            List<LogRecord> filter = memoryLogHandler.filter(WARN);

            assertThat(filter)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(errorMessage())
                .contains(warningMessage())
                .hasSize(2);
        }
    }

    @Nested
    class Text {

        @Test
        void should_filter_by_text_and_return_none() {
            final var memoryLogHandler = testMemoryLogHandler();

            List<LogRecord> filter = memoryLogHandler.filter("nothing");

            assertThat(filter).hasSize(0);
        }

        @Test
        void should_filter_by_text_and_return_some_record() {
            final var memoryLogHandler = testMemoryLogHandler();

            List<LogRecord> filter = memoryLogHandler.filter("error");

            assertThat(filter)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(errorMessage())
                .hasSize(1);
        }

        @Test
        void should_filter_by_text_and_return_all_records() {
            final var memoryLogHandler = testMemoryLogHandler();

            List<LogRecord> filter = memoryLogHandler.filter("message");

            assertThat(filter)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(errorMessage())
                .contains(getInfoMessage())
                .contains(warningMessage())
                .hasSize(3);
        }
    }

    @Nested
    class SeverityAndText {

        @Test
        void should_filter_by_severity_and_test_and_return_none() {
            final var memoryLogHandler = testMemoryLogHandler();

            List<LogRecord> filter = memoryLogHandler.filter(INFO, "nothing");

            assertThat(filter).hasSize(0);
        }

        @Test
        void should_filter_by_severity_and_test_and_return_all() {
            final var memoryLogHandler = testMemoryLogHandler();

            List<LogRecord> filter = memoryLogHandler.filter(INFO, "message");

            assertThat(filter)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(errorMessage())
                .contains(getInfoMessage())
                .contains(warningMessage())
                .hasSize(3);
        }
    }

    private static MemoryLogHandler testMemoryLogHandler() {
        final var memoryLogHandler = new MemoryLogHandler(null, null);
        memoryLogHandler.log(errorMessage());
        memoryLogHandler.log(getInfoMessage());
        memoryLogHandler.log(warningMessage());
        return memoryLogHandler;
    }

    private static LogRecord errorMessage() {
        return new LogRecord(ERROR, "error message");
    }

    private static LogRecord getInfoMessage() {
        return new LogRecord(INFO, "info message");
    }

    private static LogRecord warningMessage() {
        return new LogRecord(WARN, "warning message");
    }
}
