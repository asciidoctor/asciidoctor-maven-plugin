package org.asciidoctor.maven.log;

import org.apache.maven.plugin.MojoExecutionException;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

import java.io.File;
import java.util.List;

public class LogHandlerHelper {


    public static void processLogRecords(LogHandler logHandler, File sourceDirectory, MemoryLogHandler memoryLogHandler) throws MojoExecutionException {
        if (logHandler.isSeveritySet() && logHandler.isContainsTextNotBlank()) {
            final Severity severity = logHandler.getFailIf().getSeverity();
            final String textToSearch = logHandler.getFailIf().getContainsText();

            final List<LogRecord> records = memoryLogHandler.filter(severity, textToSearch);
            if (records.size() > 0) {
                for (LogRecord record : records) {
                    getLog().error(LogRecordHelper.format(record, sourceDirectory));
                }
                throw new MojoExecutionException(String.format("Found %s issue(s) matching severity %s or higher and text '%s'", records.size(), severity, textToSearch));
            }
        } else if (logHandler.isSeveritySet()) {
            final Severity severity = logHandler.getFailIf().getSeverity();
            final List<LogRecord> records = memoryLogHandler.filter(severity);
            if (records.size() > 0) {
                for (LogRecord record : records) {
                    getLog().error(LogRecordHelper.format(record, sourceDirectory));
                }
                throw new MojoExecutionException(String.format("Found %s issue(s) of severity %s or higher during rendering", records.size(), severity));
            }
        } else if (logHandler.isContainsTextNotBlank()) {
            final String textToSearch = logHandler.getFailIf().getContainsText();
            final List<LogRecord> records = memoryLogHandler.filter(textToSearch);
            if (records.size() > 0) {
                for (LogRecord record : records) {
                    getLog().error(LogRecordHelper.format(record, sourceDirectory));
                }
                throw new MojoExecutionException(String.format("Found %s issue(s) containing '%s'", records.size(), textToSearch));
            }
        }
    }

}
