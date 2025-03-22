package org.asciidoctor.maven.log;

import java.util.Optional;

import static org.asciidoctor.log.Severity.*;

import org.asciidoctor.log.LogRecord;

class TestLogRecords {

    static LogRecord errorMessage() {
        return errorMessage(null);
    }

    static LogRecord errorMessage(Integer index) {
        LogRecord logRecord = new LogRecord(ERROR, buildMessage("error", index));
        return new CapturedLogRecord(logRecord, null);
    }

    static LogRecord getInfoMessage() {
        return getInfoMessage(null);
    }

    static LogRecord getInfoMessage(Integer index) {
        LogRecord logRecord = new LogRecord(INFO, buildMessage("info", index));
        return new CapturedLogRecord(logRecord, null);
    }

    static LogRecord warningMessage() {
        return warningMessage(null);
    }

    static LogRecord warningMessage(Integer index) {
        LogRecord logRecord = new LogRecord(WARN, buildMessage("warning", index));
        return new CapturedLogRecord(logRecord, null);
    }

    private static String buildMessage(String type, Integer index) {
        return Optional.ofNullable(index)
            .map(i -> type + " message " + index)
            .orElse(type + " message");
    }
}
