package org.asciidoctor.maven.log;

import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * AsciidoctorJ LogHandler that stores records in memory.
 */
public class MemoryLogHandler implements LogHandler {

    final List<LogRecord> records = new ArrayList<>();

    private final Boolean outputToConsole;
    private final File sourceDirectory;
    private final Log log;

    public MemoryLogHandler(Boolean outputToConsole, File sourceDirectory, Log log) {
        this.outputToConsole = outputToConsole == null ? Boolean.FALSE : outputToConsole;
        this.sourceDirectory = sourceDirectory;
        this.log = log;
    }

    @Override
    public void log(LogRecord logRecord) {
        records.add(logRecord);
        if (outputToConsole)
            log.info(LogRecordHelper.format(logRecord, sourceDirectory));
    }

    public void clear() {
        records.clear();
    }

    /**
     * Returns LogRecords that belong to a severity level
     */
    public List<LogRecord> filter(Severity severity) {
        // FIXME: find better name or replace with stream
        final List<LogRecord> records = new ArrayList<>();
        for (LogRecord record : this.records) {
            if (record.getSeverity().equals(severity))
                records.add(record);
        }
        return records;
    }

    /**
     * Returns LogRecords whose message contains a text
     */
    public List<LogRecord> filter(String text) {
        final List<LogRecord> records = new ArrayList<>();
        for (LogRecord record : this.records) {
            if (record.getMessage().contains(text))
                records.add(record);
        }
        return records;
    }

    /**
     * Returns LogRecords that belong to a severity level & whose message contains a text
     */
    public List<LogRecord> filter(Severity severity, String text) {
        final List<LogRecord> records = new ArrayList<>();
        for (LogRecord record : this.records) {
            if (record.getSeverity().equals(severity) && record.getMessage().contains(text))
                records.add(record);
        }
        return records;
    }

}
