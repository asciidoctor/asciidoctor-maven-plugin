package org.asciidoctor.maven.test;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestLogHandlerService implements LogHandler {

    public static final String CUSTOM_LOG = "custom_log.log";

    final FileOutputStream logFile;

    private static List<LogRecord> logRecords = new ArrayList<>();

    public static List<LogRecord> getLogRecords() {
        return logRecords;
    }

    public static void clear() {
        logRecords.clear();
    }

    public TestLogHandlerService() {
        try {
            logFile = new FileOutputStream(new File(CUSTOM_LOG));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void log(LogRecord logRecord) {
        writeLine("Logging from TestLogHandlerService: " + logRecord.getMessage());
        logRecords.add(logRecord);
    }

    private void writeLine(String message) {
        try {
            logFile.write(message.getBytes());
            logFile.flush();
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}