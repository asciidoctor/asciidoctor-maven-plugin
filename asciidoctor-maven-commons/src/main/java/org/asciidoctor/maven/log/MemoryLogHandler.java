package org.asciidoctor.maven.log;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/**
 * AsciidoctorJ LogHandler that stores records in memory.
 *
 * @author abelsromero
 */
public class MemoryLogHandler implements LogHandler {

    final List<LogRecord> records = new ArrayList<>();

    private final Boolean outputToConsole;
    private final File sourceDirectory;
    private final Consumer<LogRecord> recordConsumer;

    public MemoryLogHandler(Boolean outputToConsole, File sourceDirectory, Consumer<LogRecord> recordConsumer) {
        this.outputToConsole = outputToConsole == null ? Boolean.FALSE : outputToConsole;
        this.sourceDirectory = sourceDirectory;
        this.recordConsumer = recordConsumer;
    }

    @Override
    public void log(LogRecord logRecord) {
        records.add(logRecord);
        if (outputToConsole)
            recordConsumer.accept(logRecord);
    }

    public void clear() {
        records.clear();
    }

    /**
     * Returns LogRecords that are equal or above the severity level.
     *
     * @param severity Asciidoctor severity level
     * @return list of filtered logRecords
     */
    public List<LogRecord> filter(Severity severity) {
        // FIXME: find better name or replace with stream
        final List<LogRecord> records = new ArrayList<>();
        for (LogRecord record : this.records) {
            if (record.getSeverity().ordinal() >= severity.ordinal())
                records.add(record);
        }
        return records;
    }

    /**
     * Returns LogRecords whose message contains text.
     *
     * @param text text to search for in the LogRecords
     * @return list of filtered logRecords
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
     * Returns LogRecords that are equal or above the severity level and whose message contains text.
     *
     * @param severity Asciidoctor severity level
     * @param text     text to search for in the LogRecords
     * @return list of filtered logRecords
     */
    public List<LogRecord> filter(Severity severity, String text) {
        final List<LogRecord> records = new ArrayList<>();
        for (LogRecord record : this.records) {
            if (record.getSeverity().ordinal() >= severity.ordinal() && record.getMessage().contains(text))
                records.add(record);
        }
        return records;
    }

}
