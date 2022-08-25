package org.asciidoctor.maven.refresh;

import org.asciidoctor.maven.io.AsciidoctorFileScanner;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;
import static org.asciidoctor.maven.io.AsciidoctorFileScanner.ASCIIDOC_FILE_EXTENSIONS_REG_EXP;

/**
 * Builds regular expression to include all valid resources, as well as exclude invalid ones
 * to be copied for `auto-refresh` mojo.
 *
 * @author abelsromero
 */
public class ResourcesPatternBuilder {

    private final String sourceDocumentName;
    private final List<String> sourceDocumentExtensions;

    public ResourcesPatternBuilder(final String sourceDocumentName, final List<String> sourceDocumentExtensions) {
        this.sourceDocumentName = sourceDocumentName;
        this.sourceDocumentExtensions = sourceDocumentExtensions;
    }

    public String build() {
        final StringJoiner filePattern = new StringJoiner("|")
                .add(ASCIIDOC_FILE_EXTENSIONS_REG_EXP);
        if (!sourceDocumentExtensions.isEmpty())
            filePattern.add(String.join("|", sourceDocumentExtensions));

        final String specialFiles = Arrays.stream(AsciidoctorFileScanner.IGNORED_FILE_NAMES)
                .map(pattern -> pattern.replaceAll("\\*", ".*"))
                .map(pattern -> pattern.replaceAll("\\.", "\\\\."))
                .collect(Collectors.joining("|"));

        return new StringBuilder()
                .append("^")
                .append("(?!(" + specialFiles + (isBlank(sourceDocumentName) ? "" : "|" + sourceDocumentName) + "))")
                .append("[^_.].*\\.(?!(")
                .append(filePattern.toString())
                .append(")).*$")
                .toString();
    }

}
