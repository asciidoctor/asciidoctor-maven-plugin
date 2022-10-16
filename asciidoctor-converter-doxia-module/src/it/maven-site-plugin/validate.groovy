import java.nio.file.Files
import java.util.regex.Matcher
import java.util.regex.Pattern

File outputDir = new File(basedir, "target/site")

final String[] expectedFiles = ["file-with-toc.html", "sample.html"]
final String[] unexpectedFiles = ["_include.html"]

final Pattern tocEntry = Pattern.compile("<li><a href=\"#(.*?)\">.*")
final Pattern elementIdPattern = Pattern.compile(".* id=\"(.*?)\".*")

def asserter = new FileAsserter()

for (String expectedFile : expectedFiles) {
    final File file = new File(outputDir, expectedFile)
    println "Checking for presence of $file"

    asserter.isNotEmpty(file)

    if (expectedFile == "file-with-toc.html") {
        List lines = Files.readAllLines(file.toPath())
        println "Ensuring IDs match TOC links"

        List tocEntries = new ArrayList()
        for (String line : lines) {
            Matcher matcher = tocEntry.matcher(line)
            if (matcher.matches()) {
                tocEntries.add(matcher.group(1))
            }

            matcher = elementIdPattern.matcher(line)
            if (matcher.matches()) {
                String elementId = matcher.group(1)
                if (tocEntries.contains(elementId)) {
                    tocEntries.remove(tocEntries.indexOf(elementId))
                }
            }
        }

        if (tocEntries.size() != 0) {
            throw new Exception("Couldn't find matching IDs for the following TOC entries: $tocEntries")
        }

        boolean includeResolved = false
        boolean sourceHighlighted = false

        for (String line : lines) {
            if (!includeResolved && line.contains("Content included from the file ")) {
                includeResolved = true
            } else if (!sourceHighlighted && line.contains("<span style=\"color:#070;font-weight:bold\">&lt;plugin&gt;</span>")) {
                sourceHighlighted = true
            }
        }

        if (!includeResolved) {
            throw new Exception("Include file was not resolved")
        }

        if (!sourceHighlighted) {
            throw new Exception("Source code was not highlighted")
        }

        // validate that maven properties are replaced same as attributes
        boolean foundReplacement = false
        for (String line : lines) {
            if (line.contains("v1.2.3")) {
                foundReplacement = true
                break
            }
        }
        if (!foundReplacement) {
            throw new Exception("Maven properties not replaced.")
        }
    }
}

for (String unexpectedFile : unexpectedFiles) {
    File file = new File(outputDir, unexpectedFile)
    println "Checking for absence of $file"
    if (file.isFile()) {
        throw new Exception("Unexpected file $file")
    }
}

class FileAsserter {

    void isNotEmpty(File file) {
        if (!file.isFile()) {
            throw new Exception("Missing file $file")
        }
        if (file.length() == 0) {
            throw new Exception("Empty file $file")
        }
    }
}

return true
