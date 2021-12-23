package org.asciidoctor.maven.log;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.log.LogRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LogRecordFormatter {

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

        String sourcePath = calculateFileRelativePath(cursor, sourceDirectory);
        if (sourcePath == null && cursor != null)
            sourcePath = cursor.getFile();

        final List<String> messageParts = new ArrayList<>();
        messageParts.add(MESSAGE_HEADER);
        messageParts.add(logRecord.getSeverity().toString());

        if (sourcePath != null)
            messageParts.add(sourcePath);

        if (cursor != null && cursor.getLineNumber() > 0)
            messageParts.add("line " + cursor.getLineNumber());

        messageParts.add(logRecord.getMessage());

        return messageParts.stream().collect(Collectors.joining(": "));
    }

    /**
     * Attempts to obtain the source path in relative format for ease and security.
     *
     * @return relative path or null if it was not possible
     */
    private static String calculateFileRelativePath(Cursor cursor, File sourceDirectory) {
        try {
            if (isValidFile(cursor)) {
                final String sourceFile = new File(cursor.getFile()).getCanonicalPath();
                final String sourceDir = sourceDirectory.getCanonicalPath();

                if (sourceFile.startsWith(sourceDir)) {
                    return sourceFile.substring(sourceDirectory.getCanonicalPath().length() + 1);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static boolean isValidFile(Cursor cursor) {
        return cursor != null && cursor.getFile() != null && !isHttpSource(cursor.getFile());
    }

    private static boolean isHttpSource(String filePath) {
        return filePath.startsWith("http://") || filePath.startsWith("https://");
    }
}
