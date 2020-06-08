package org.asciidoctor.maven.log;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class LogRecordHelperTest {

    @Test
    public void should_apply_full_format_logRecord_with_all_data() {
        // given
        final Cursor cursor = new TestCursor(new File("file.adoc").getAbsolutePath(), 3, "path", "dir");
        final LogRecord logRecord = new LogRecord(Severity.INFO, cursor, "a message");
        final File sourceDir = getParentFile();
        // when
        String formattedLogRecord = LogRecordHelper.format(logRecord, sourceDir);
        // then
        assertThat(normalizePath(formattedLogRecord)).isEqualTo("asciidoctor: INFO: asciidoctor-maven-plugin/file.adoc: line 3: a message");
    }

    @Test
    public void should_apply_simple_format_when_cursor_is_null() {
        // given
        final LogRecord logRecord = new LogRecord(Severity.INFO, null, "a message");
        // when
        String formattedLogRecord = LogRecordHelper.format(logRecord, null);
        // then
        assertThat(normalizePath(formattedLogRecord)).isEqualTo("asciidoctor: INFO: a message");
    }

    @Test
    public void should_apply_simple_format_when_cursor_is_empty() {
        // given
        final Cursor cursor = new TestCursor(null, 0, null, null);
        final LogRecord logRecord = new LogRecord(Severity.INFO, cursor, "a message");
        // when
        String formattedLogRecord = LogRecordHelper.format(logRecord, null);
        // then
        assertThat(normalizePath(formattedLogRecord)).isEqualTo("asciidoctor: INFO: a message");
    }

    @Test
    public void should_format_full_logRecord_with_file_absolute_path_when_sourceDir_is_not_valid() throws IOException {
        // given
        final Cursor cursor = new TestCursor(new File("file.adoc").getAbsolutePath(), 3, "path", "dir");
        final LogRecord logRecord = new LogRecord(Severity.INFO, cursor, "a message");
        final File sourceDir = Mockito.mock(File.class);
        Mockito.when(sourceDir.getCanonicalPath()).thenThrow(new IOException());

        // when
        String formattedLogRecord = LogRecordHelper.format(logRecord, sourceDir);
        // then
        assertThat(normalizePath(formattedLogRecord)).matches("asciidoctor: INFO: .*/asciidoctor-maven-plugin/file.adoc: line 3: a message");
    }

    @Test
    public void should_format_logRecords_with_empty_lineNumber_absolute_path_when_sourceDir_is_not_valid2() throws IOException {
        // given
        final Cursor cursor = new TestCursor(new File("file.adoc").getAbsolutePath(), 0, "path", "dir");
        final LogRecord logRecord = new LogRecord(Severity.INFO, cursor, "a message");
        final File sourceDir = Mockito.mock(File.class);
        Mockito.when(sourceDir.getCanonicalPath()).thenThrow(new IOException());

        // when
        String formattedLogRecord = LogRecordHelper.format(logRecord, sourceDir);
        // then
        assertThat(normalizePath(formattedLogRecord)).matches("asciidoctor: INFO: .*/asciidoctor-maven-plugin/file.adoc: a message");
    }

    private File getParentFile() {
        return new File(".").getAbsoluteFile().getParentFile().getParentFile();
    }

    private String normalizePath(String formattedLogRecord) {
        return formattedLogRecord.replaceAll("\\\\", "/");
    }

    class TestCursor implements Cursor {

        private final int lineNumber;
        private final String file;
        private final String path;
        private final String dir;

        TestCursor(String file, int lineNumber, String path, String dir) {
            this.file = file;
            this.lineNumber = lineNumber;
            this.path = path;
            this.dir = dir;
        }

        @Override
        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getDir() {
            return dir;
        }

        @Override
        public String getFile() {
            return file;
        }
    }

}
