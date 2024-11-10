package org.asciidoctor.maven.process;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Finds the first available source directory amongst a list of possible candidates,
 * unless the initial candidate is the default value.
 * In that case, it only checks availability for the value provided as initial.
 *
 * @author abelsromero
 * @since 2.0.0
 */
public class SourceDirectoryFinder {

    public static final String DEFAULT_SOURCE_DIR = "src/docs/asciidoc";
    public static final String[] ORDERED_CANDIDATE_PATHS = new String[]{
            DEFAULT_SOURCE_DIR,
            "src/asciidoc",
            "src/main/asciidoc"
    };

    private final String initialCandidate;
    private final File baseDir;
    private final Consumer<File> notFoundAction;

    public SourceDirectoryFinder(File initialCandidate, File baseDir, Consumer<File> notFoundAction) {
        this.initialCandidate = initialCandidate.toString();
        this.baseDir = baseDir;
        this.notFoundAction = notFoundAction;
    }

    public Optional<File> find() {

        if (!matchesDefaultSourceDirectory(initialCandidate)) {
            File filePath = resolvePath(initialCandidate);
            if (filePath.exists())
                return Optional.of(filePath);
            else {
                notFoundAction.accept(filePath);
                return Optional.empty();
            }
        }

        for (String candidatePath : ORDERED_CANDIDATE_PATHS) {
            File current = resolvePath(candidatePath);
            if (current.exists()) {
                return Optional.of(current);
            }
            notFoundAction.accept(current);
        }

        return Optional.empty();
    }

    private boolean matchesDefaultSourceDirectory(String path) {
        return resolvePath(path).equals(new File(baseDir, DEFAULT_SOURCE_DIR));
    }

    private File resolvePath(String filePath) {
        final File candidate = new File(filePath);
        if (candidate.isAbsolute())
            return candidate;
        else
            return new File(baseDir, filePath);
    }

}
