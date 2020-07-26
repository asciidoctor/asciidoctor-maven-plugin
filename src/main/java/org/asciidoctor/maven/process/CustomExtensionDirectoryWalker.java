package org.asciidoctor.maven.process;

import org.asciidoctor.jruby.AbstractDirectoryWalker;

import java.io.File;
import java.util.Collection;

/**
 * Directory walker that finds all files matching a collection of file extensions inside a folder and in all its subfolders.
 * <p>
 * It returns only the files which their extensions are: .asc, .asciidoc, .ad or .adoc.
 *
 * @author abelsromero
 */
public class CustomExtensionDirectoryWalker extends AbstractDirectoryWalker {

    private final Collection<String> fileExtensions;

    public CustomExtensionDirectoryWalker(final String absolutePath, final Collection<String> fileExtensions) {
        super(absolutePath);
        this.fileExtensions = fileExtensions;
    }

    @Override
    protected boolean isAcceptedFile(final File filename) {
        final String name = filename.getName();
        return !name.startsWith("_") && fileExtensions.stream().anyMatch(extension -> name.endsWith(extension));
    }
}