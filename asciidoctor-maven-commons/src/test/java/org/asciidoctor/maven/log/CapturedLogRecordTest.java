package org.asciidoctor.maven.log;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CapturedLogRecordTest {

    @Nested
    class WhenLogRecordsContainsCursor {

        @Test
        void shouldReturnCursorFile() {
            final var cursorFile = new File("uno", "dos");
            final var recordFile = new File("tres", "quatre");
            final var cursor = new TestCursor(cursorFile.getAbsolutePath(), 0, null, null);

            final var logRecord = new CapturedLogRecord(getLogRecord(cursor), recordFile);
            final var capturedLogRecord = new CapturedLogRecord(logRecord, cursorFile);

            assertThat(capturedLogRecord.getCursor().getFile()).isEqualTo(cursorFile.getAbsolutePath());
        }

    }

    @Nested
    class WhenLogRecordsDoesNotContainsCursor {

        @Test
        void shouldReturnLogRecordFileWhenCursorFileIsSet() {
            final var cursorFile = new File("uno", "dos");
            final var recordFile = new File("tres", "quatre");

            final var logRecord = new CapturedLogRecord(getLogRecord(null), recordFile);
            final var capturedLogRecord = new CapturedLogRecord(logRecord, cursorFile);

            assertThat(capturedLogRecord.getCursor().getFile()).isEqualTo(recordFile.getAbsolutePath());
        }

        @Test
        void shouldReturnNullWhenCursorFileIsNoSet() {
            final var logRecord = new CapturedLogRecord(getLogRecord(null), null);
            final var capturedLogRecord = new CapturedLogRecord(logRecord, null);

            assertThat(capturedLogRecord.getCursor()).isNull();
        }

    }

    private LogRecord getLogRecord(Cursor cursor) {
        return new LogRecord(Severity.INFO, cursor, "a message");
    }

}
