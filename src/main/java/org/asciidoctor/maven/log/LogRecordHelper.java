package org.asciidoctor.maven.log;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.log.LogRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to manage AsciidoctorJ LogRecords.
 */
public class LogRecordHelper {

    private static final String MESSAGE_HEADER = "asciidoctor";

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
        final String relativePath = calculateFileRelativePath(cursor, sourceDirectory);

        final List<String> messageParts = new ArrayList<>();
        messageParts.add(MESSAGE_HEADER);
        messageParts.add(logRecord.getSeverity().toString());

        if (relativePath != null)
            messageParts.add(relativePath);

        if (cursor != null && cursor.getLineNumber() > 0)
            messageParts.add("line " + cursor.getLineNumber());

        messageParts.add(logRecord.getMessage());

        return messageParts.stream().collect(Collectors.joining(": "));
    }

    private static String calculateFileRelativePath(Cursor cursor, File sourceDirectory) {
        try {
            if (cursor != null && cursor.getFile() != null) {
                return new File(cursor.getFile())
                        .getCanonicalPath()
                        .substring(sourceDirectory.getCanonicalPath().length() + 1);
            }
        } catch (IOException e) {
            // use the absolute path as fail-safe
            return cursor.getFile();
        }
        return null;
    }

}
