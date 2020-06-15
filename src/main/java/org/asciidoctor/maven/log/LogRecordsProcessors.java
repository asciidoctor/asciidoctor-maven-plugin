package org.asciidoctor.maven.log;

import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class LogRecordsProcessors {

    private final LogHandler logHandler;
    private final File sourceDirectory;

    private final Consumer<String> errorMessageConsumer;

    public LogRecordsProcessors(LogHandler logHandler,
                                File sourceDirectory,
                                Consumer<String> errorMessageConsumer) {
        this.logHandler = logHandler;
        this.sourceDirectory = sourceDirectory;
        this.errorMessageConsumer = errorMessageConsumer;
    }

    public void processLogRecords(MemoryLogHandler memoryLogHandler) throws Exception {
        if (logHandler.isSeveritySet() && logHandler.isContainsTextNotBlank()) {
            final Severity severity = logHandler.getFailIf().getSeverity();
            final String textToSearch = logHandler.getFailIf().getContainsText();

            final List<LogRecord> records = memoryLogHandler.filter(severity, textToSearch);
            if (records.size() > 0) {
                for (LogRecord record : records) {
                    errorMessageConsumer.accept(LogRecordHelper.format(record, sourceDirectory));
                }
                throw new Exception(String.format("Found %s issue(s) matching severity %s or higher and text '%s'", records.size(), severity, textToSearch));
            }
        } else if (logHandler.isSeveritySet()) {
            final Severity severity = logHandler.getFailIf().getSeverity();
            final List<LogRecord> records = memoryLogHandler.filter(severity);
            if (records.size() > 0) {
                for (LogRecord record : records) {
                    errorMessageConsumer.accept(LogRecordHelper.format(record, sourceDirectory));
                }
                throw new Exception(String.format("Found %s issue(s) of severity %s or higher during conversion", records.size(), severity));
            }
        } else if (logHandler.isContainsTextNotBlank()) {
            final String textToSearch = logHandler.getFailIf().getContainsText();
            final List<LogRecord> records = memoryLogHandler.filter(textToSearch);
            if (records.size() > 0) {
                for (LogRecord record : records) {
                    errorMessageConsumer.accept(LogRecordHelper.format(record, sourceDirectory));
                }
                throw new Exception(String.format("Found %s issue(s) containing '%s'", records.size(), textToSearch));
            }
        }
    }

}
