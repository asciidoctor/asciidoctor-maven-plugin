package org.asciidoctor.maven.log;

import org.asciidoctor.log.LogRecord;

public class AsciidoctorConvertionException extends Exception {

    private final LogRecord logRecord;

    public AsciidoctorConvertionException(String message, LogRecord logRecord) {
        super(message);
        this.logRecord = logRecord;
    }
}
