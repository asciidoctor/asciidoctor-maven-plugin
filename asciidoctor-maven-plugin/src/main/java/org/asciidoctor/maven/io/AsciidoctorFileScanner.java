package org.asciidoctor.maven.io;

import org.apache.maven.model.Resource;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;

import java.io.File;
import java.util.*;

/**
 * Recursively traverses directories returning the list of AsciiDoc files that match the applied filters.
 * If no filters are set, AsciiDoc documents with extensions .adoc, .ad, .asc and .asciidoc are returned.
 */
public class AsciidoctorFileScanner {

    // copied from org.asciidoctor.AsciiDocDirectoryWalker.ASCIIDOC_REG_EXP_EXTENSION
    // should probably be configured in AsciidoctorMojo through @Parameter 'extension'
    public static final String ASCIIDOC_FILE_EXTENSIONS_REG_EXP = "a((sc(iidoc)?)|d(oc)?)";
    public static final String ASCIIDOC_NON_INTERNAL_REG_EXP = "^[^_.].*\\." + ASCIIDOC_FILE_EXTENSIONS_REG_EXP + "$";

    public static String[] DEFAULT_ASCIIDOC_EXTENSIONS = {"**/*.adoc", "**/*.ad", "**/*.asc", "**/*.asciidoc"};

    // Files and directories beginning with underscore are ignored
    public static String[] INTERNAL_FOLDERS_AND_FILES_PATTERNS = {
            "**/_*.*",
            "**/_*",
            "**/.*",
            "**/_*/**/*.*",
    };

    // docinfo snippets should not be copied
    public static String[] IGNORED_FILE_NAMES = {
            "docinfo.html",
            "docinfo-header.html",
            "docinfo-footer.html",
            "*-docinfo.html",
            "*-docinfo-header.html",
            "*-docinfo-footer.html",
            "docinfo.xml",
            "docinfo-header.xml",
            "docinfo-footer.xml",
            "*-docinfo.xml",
            "*-docinfo-header.xml",
            "*-docinfo-footer.xml"
    };

    /**
     * Scans a resource directory (and sub-subdirectories) returning all AsciiDoc documents found.
     *
     * @param resource {@link Resource} to scan (the directory property is mandatory)
     * @return List of found documents matching the resource properties
     */
    public List<File> scan(Resource resource) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(new File(resource.getDirectory()));
        setupScanner(scanner, resource);
        scanner.scan();
        List<File> files = new ArrayList<>();
        for (String file : scanner.getIncludedFiles()) {
            files.add(new File(resource.getDirectory(), file));
        }
        return files;
    }

    /**
     * Scans a list of resources returning all AsciiDoc documents found.
     *
     * @param resources List of {@link Resource} to scan (the directory property is mandatory)
     * @return List of found documents matching the resources properties
     */
    public List<File> scan(List<Resource> resources) {
        final List<File> files = new ArrayList<>();
        for (Resource resource : resources) {
            files.addAll(scan(resource));
        }
        return files;
    }

    /**
     * Initializes the Scanner with the default values.
     * <br>
     * By default:
     * <ul>
     *     <li>includes adds extension .adoc, .ad, .asc and .asciidoc
     *     <li>excludes adds filters to avoid hidden files and directoris beginning with undersore
     * </ul>
     * <p>
     * NOTE: Patterns both in inclusions and exclusions are automatically excluded.
     */
    private void setupScanner(Scanner scanner, Resource resource) {

        if (isEmpty(resource.getIncludes())) {
            scanner.setIncludes(DEFAULT_ASCIIDOC_EXTENSIONS);
        } else {
            scanner.setIncludes(resource.getIncludes().toArray(new String[]{}));
        }

        if (isEmpty(resource.getExcludes())) {
            scanner.setExcludes(IGNORED_FILE_NAMES);
        } else {
            scanner.setExcludes(mergeAndConvert(resource.getExcludes(), IGNORED_FILE_NAMES));
        }
        // adds exclusions like SVN or GIT files
        scanner.addDefaultExcludes();
    }

    private boolean isEmpty(List<String> excludes) {
        return excludes == null || excludes.isEmpty();
    }

    /**
     * Returns a String[] with the values of both input parameters.
     * Duplicated values are inserted only once.
     *
     * @param list  List of string
     * @param array Array of String
     * @return Array of String with all values
     */
    private String[] mergeAndConvert(List<String> list, String[] array) {
        Set<String> set = new HashSet<>(Arrays.asList(array));
        set.addAll(list);
        return set.toArray(new String[set.size()]);
    }

}
