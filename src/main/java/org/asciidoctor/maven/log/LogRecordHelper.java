package org.asciidoctor.maven.log;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.log.LogRecord;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to manage AsciidoctorJ LogRecords.
 */
public class LogRecordHelper {

    // includes replacers for cursor (file & line)
    public static final String ASCIIDOCTOR_FULL_LOG_FORMAT = "asciidoctor: %s: %s: line %s: %s";
    public static final String ASCIIDOCTOR_SIMPLE_LOG_FORMAT = "asciidoctor: %s: %s";

    /**
     * Formats the logRecord in a similar manner to original Asciidoctor.
     * Note: prints the absolute path of the file.
     *
     * @param logRecord Asciidoctor logRecord to format
     * @return Asciidoctor-like formatted string
     */
    public static String format(LogRecord logRecord) {
        final Cursor cursor = logRecord.getCursor();
        return String.format(ASCIIDOCTOR_FULL_LOG_FORMAT, logRecord.getSeverity(), cursor.getFile(), cursor.getLineNumber(), logRecord.getMessage());
    }

    /**
     * Formats the logRecord in a similar manner to original Asciidoctor.
     * Note: prints the relative path of the file to `sourceDirectory`.
     *
     * @param logRecord       Asciidoctor logRecord to format
     * @param sourceDirectory source directory of the converted AsciiDoc document
     * @return Asciidoctor-like formatted string
     */
    public static String format(LogRecord logRecord, File sourceDirectory) {
        final Cursor cursor = logRecord.getCursor();
        String relativePath = "";
        try {
            if (cursor != null) {
                relativePath = new File(cursor.getFile()).getCanonicalPath()
                        .substring(sourceDirectory.getCanonicalPath().length() + 1);
            }
        } catch (IOException e) {
            // use the absolute path as fail-safe
            relativePath = cursor.getFile();
        }
        return relativePath.length() > 0 ?
                String.format(ASCIIDOCTOR_FULL_LOG_FORMAT, logRecord.getSeverity(), relativePath, cursor.getLineNumber(), logRecord.getMessage())
                :
                String.format(ASCIIDOCTOR_SIMPLE_LOG_FORMAT, logRecord.getSeverity(), logRecord.getMessage())
                ;
    }

}
