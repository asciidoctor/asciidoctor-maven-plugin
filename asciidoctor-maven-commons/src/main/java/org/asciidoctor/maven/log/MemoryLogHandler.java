package org.asciidoctor.maven.log;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;
import org.asciidoctor.maven.commons.StringUtils;


/**
 * AsciidoctorJ LogHandler that stores records in memory.
 *
 * @author abelsromero
 * @since 1.5.7
 */
public class MemoryLogHandler implements LogHandler {

    private final List<LogRecord> records = new ArrayList<>();

    private final Boolean outputToConsole;
    private final Consumer<LogRecord> recordConsumer;

    public MemoryLogHandler(Boolean outputToConsole, Consumer<LogRecord> recordConsumer) {
        this.outputToConsole = outputToConsole == null ? Boolean.FALSE : outputToConsole;
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
     * @param severity Asciidoctor's severity level
     * @return list of filtered logRecords
     */
    public List<LogRecord> filter(Severity severity) {
        return this.records.stream()
            .filter(record -> severityIsHigher(record, severity))
            .collect(Collectors.toList());
    }

    /**
     * Returns LogRecords whose message contains text.
     *
     * @param text text to search for in the LogRecords
     * @return list of filtered logRecords
     */
    public List<LogRecord> filter(String text) {
        return this.records.stream()
            .filter(record -> messageContains(record, text))
            .collect(Collectors.toList());
    }

    /**
     * Returns LogRecords that are equal or above the severity level and whose message contains text.
     *
     * @param severity Asciidoctor's severity level
     * @param text     text to search for in the LogRecords
     * @return list of filtered logRecords
     */
    public List<LogRecord> filter(Severity severity, String text) {
        return this.records.stream()
            .filter(record -> severityIsHigher(record, severity) && messageContains(record, text))
            .collect(Collectors.toList());
    }

    /**
     * Returns whether error messages have been captured or no.
     *
     * @return true if no error messages are present
     * @since 3.1.0
     */
    public boolean isEmpty() {
        return records.isEmpty();
    }

    /**
     * Processes all stored log records.
     *
     * @since 3.1.0
     */
    public void processAll() {
        records.forEach(recordConsumer::accept);
    }

    private static boolean severityIsHigher(LogRecord record, Severity severity) {
        if (severity == null) {
            return false;
        } else {
            return record.getSeverity().ordinal() >= severity.ordinal();
        }
    }

    private static boolean messageContains(LogRecord record, String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        } else {
            return record.getMessage().contains(text);
        }
    }

}
