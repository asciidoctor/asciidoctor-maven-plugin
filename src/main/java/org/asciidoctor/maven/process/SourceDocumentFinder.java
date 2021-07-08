package org.asciidoctor.maven.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Finds all source documents inside a source directory.
 * It traverses the source directory and all subdirectories. It can match custom file extensions.
 * If none are specified, it matches <code>.asc</code>, <code>.asciidoc</code>, <code>.ad</code> and <code>.adoc</code>.
 * Files and directories starting with an underscore (<code>_</code>) are ignored.
 *
 * @author stdll
 */
public class SourceDocumentFinder {

    /** Pattern for matching standard file extensions. */
    private static final String STANDARD_FILE_EXTENSIONS_PATTERN = "^[^_.].*\\.a((sc(iidoc)?)|d(oc)?)$";

    /** Prefix for matching custom file extensions. */
    private static final String CUSTOM_FILE_EXTENSIONS_PATTERN_PREFIX = "^[^_.].*\\.(";

    /** Suffix for matching custom file extensions. */
    private static final String CUSTOM_FILE_EXTENSIONS_PATTERN_SUFFIX = ")$";

    /**
     * Finds all source documents inside the source directory with standard file extensions.
     *
     * @param sourceDirectory source directory
     * @return the list of all matching source documents.
     */
    public List<File> find(Path sourceDirectory) {
        return find(sourceDirectory, Pattern.compile(STANDARD_FILE_EXTENSIONS_PATTERN));
    }

    /**
     * Finds all source documents inside the source directory with custom file extensions.
     *
     * @param sourceDirectory          source directory
     * @param sourceDocumentExtensions custom file extensions
     * @return the list of all matching source documents.
     */
    public List<File> find(Path sourceDirectory, List<String> sourceDocumentExtensions) {
        String extensionPattern = sourceDocumentExtensions.stream()
String extensionPattern = sourceDocumentExtensions.stream()
                .collect(Collectors.joining("|", CUSTOM_FILE_EXTENSIONS_PATTERN_PREFIX, CUSTOM_FILE_EXTENSIONS_PATTERN_SUFFIX));
            .concat(CUSTOM_FILE_EXTENSIONS_PATTERN_SUFFIX);
        return find(sourceDirectory, Pattern.compile(extensionPattern));
    }

    private List<File> find(Path sourceDirectory, Pattern sourceDocumentPattern) {
        try (Stream<Path> sourceDocumentCandidates = Files.walk(sourceDirectory)) {
            return sourceDocumentCandidates.filter(Files::isRegularFile)
                // Filter all documents that don't match the file extension pattern.
                .filter(p -> sourceDocumentPattern.matcher(p.getFileName().toString()).matches())
                // Filter all documents that are part of ignored directories.
                .filter(p -> {
                    for (Path part : p) {
                        if (part.toString().startsWith("_")) {
                            return false;
                        }
                    }
                    return true;
                }).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            // Can't access the source directory.
            return Collections.emptyList();
        }
    }
}
