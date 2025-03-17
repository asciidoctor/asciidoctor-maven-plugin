package org.asciidoctor.maven.log;

import java.io.File;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.log.LogRecord;

/**
 * {@link LogRecord} proxy that allows capturing the source file being
 * processed.
 * Important: the {@link #sourceFile} and the actual source where an error is present
 * may not be the same. For example if the source is being included.
 *
 * @since 3.1.2
 */
final class CapturedLogRecord extends LogRecord {

    private final File sourceFile;

    CapturedLogRecord(LogRecord record, File sourceFile) {
        super(record.getSeverity(), record.getCursor(), record.getMessage(), record.getSourceFileName(), record.getSourceMethodName());
        this.sourceFile = sourceFile;
    }

    public Cursor getCursor() {
        if (super.getCursor() != null) {
            return super.getCursor();
        }
        if (sourceFile != null) {
            return new FileCursor(sourceFile);
        }
        return null;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    class FileCursor implements Cursor {

        private final File file;

        public FileCursor(File file) {
            this.file = file;
        }

        @Override
        public int getLineNumber() {
            return 0;
        }

        @Override
        public String getPath() {
            return file.getName();
        }

        @Override
        public String getDir() {
            return file.getParent();
        }

        @Override
        public String getFile() {
            return file.getAbsolutePath();
        }
    }
}
