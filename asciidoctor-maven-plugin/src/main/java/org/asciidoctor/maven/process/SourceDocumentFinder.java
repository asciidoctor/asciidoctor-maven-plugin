package org.asciidoctor.maven.process;

import javax.inject.Singleton;
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
@Singleton
public class SourceDocumentFinder {

    // copied from org.asciidoctor.AsciiDocDirectoryWalker.ASCIIDOC_REG_EXP_EXTENSION
    // should probably be configured in AsciidoctorMojo through @Parameter 'extension'
    public static final String ASCIIDOC_FILE_EXTENSIONS_REG_EXP = "a((sc(iidoc)?)|d(oc)?)";

    /**
     * Pattern for matching standard file extensions.
     */
    public static final String STANDARD_FILE_EXTENSIONS_PATTERN = "^[^_.].*\\." + ASCIIDOC_FILE_EXTENSIONS_REG_EXP + "$";

    /**
     * Prefix for matching custom file extensions.
     */
    public static final String CUSTOM_FILE_EXTENSIONS_PATTERN_PREFIX = "^[^_.].*\\.(";

    /**
     * Suffix for matching custom file extensions.
     */
    public static final String CUSTOM_FILE_EXTENSIONS_PATTERN_SUFFIX = ")$";

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
                .collect(Collectors.joining("|", CUSTOM_FILE_EXTENSIONS_PATTERN_PREFIX, CUSTOM_FILE_EXTENSIONS_PATTERN_SUFFIX));
        return find(sourceDirectory, Pattern.compile(extensionPattern));
    }

    private List<File> find(Path sourceDirectory, Pattern sourceDocumentPattern) {
        try (Stream<Path> sourceDocumentCandidates = Files.walk(sourceDirectory)) {
            return sourceDocumentCandidates
                    .filter(Files::isRegularFile)
                    .filter(path -> sourceDocumentPattern.matcher(path.getFileName().toString()).matches())
                    .filter(path -> {
                        for (Path part : sourceDirectory.relativize(path)) {
                            char firstCharacter = part.toString().charAt(0);
                            if (firstCharacter == '_' || firstCharacter == '.') {
                                return false;
                            }
                        }
                        return true;
                    })
                    .map(Path::toFile)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
