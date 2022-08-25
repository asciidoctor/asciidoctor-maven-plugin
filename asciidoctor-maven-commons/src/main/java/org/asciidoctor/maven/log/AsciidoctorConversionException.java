package org.asciidoctor.maven.log;

import org.asciidoctor.log.LogRecord;

public class AsciidoctorConversionException extends Exception {

    private final LogRecord logRecord;

    public AsciidoctorConversionException(String message, LogRecord logRecord) {
        super(message);
        this.logRecord = logRecord;
    }
}
