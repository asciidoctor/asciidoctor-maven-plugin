import java.nio.file.Files
import java.util.regex.Pattern

File outputDir = new File(basedir, "target/site")

final String[] expectedFiles = ["file-with-toc.html", "sample.html"]
final String[] unexpectedFiles = ["_include.html"]

final Pattern tocEntry = Pattern.compile("<li><a href=\"#(.*?)\">.*")
final Pattern elementIdPattern = Pattern.compile(".* id=\"(.*?)\".*")

def asserter = new FileAsserter()

def tocEntryCapturer = new PatternCapturer("<li><a href=\"#(.*?)\">.*")
def elementIdCapturer = new PatternCapturer(".* id=\"(.*?)\".*")

for (String expectedFile : expectedFiles) {
    final File file = new File(outputDir, expectedFile)

    println "Checking for presence of $file"
    asserter.isNotEmpty(file)

    if (expectedFile == "file-with-toc.html") {
        List lines = Files.readAllLines(file.toPath())
        println "Ensuring IDs match TOC links"

        // == Assert TOC ==
        for (String line : lines) {
            tocEntryCapturer.tryMatches(line)

            String match = elementIdCapturer.tryMatches(line)
            if (match != null) {
                tocEntryCapturer.remove(match)
            }
        }

        if (tocEntryCapturer.size() != 0) {
            throw new Exception("Couldn't find matching IDs for the following TOC entries: ${tocEntryCapturer.getHits()}")
        }

        boolean includeResolved = false
        boolean sourceHighlighted = false

        // == Assert includes ==
        for (String line : lines) {
            if (!includeResolved && line.contains("Content included from the file ")) {
                includeResolved = true
            } else if (!sourceHighlighted && line.contains("<span style=\"color:#070;font-weight:bold\">&lt;plugin&gt;</span>")) {
                sourceHighlighted = true;
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

        // == Assert header metadata ==
        def metaPattern = Pattern.compile("<meta name=\"(author|date)\" content=\"(.*)\" />")
        boolean headFound = false
        Map<String, String> metaTags = new HashMap<>()

        for (String line : lines) {
            if (!headFound) {
                headFound = line.endsWith("<head>")
                continue
            }
            if (line.endsWith("</head>")) break
            def matcher = metaPattern.matcher(line.trim())
            if (matcher.matches()) {
                metaTags.put(matcher.group(1), matcher.group(2))
            }
        }

        if (metaTags['author'] != 'The Author')
            throw new RuntimeException("Author not found in $metaTags")
        if (metaTags['date'] != '2024-02-07 23:36:29')
            throw new RuntimeException("docdatetime not found in: $metaTags")

        // assert breadcrumbs
        boolean breadcrumbTagFound = false
        boolean breadcrumbFound = false
        final String docTitle = "File with TOC"

        for (String line : lines) {
            if (!breadcrumbTagFound) {
                breadcrumbTagFound = line.endsWith("<div id=\"breadcrumbs\">")
                continue
            }
            println "Testing: ${line.trim()}"
            breadcrumbFound = line.trim().endsWith("${docTitle}</li>")
            if (breadcrumbFound) break
        }

        if (!breadcrumbFound)
            throw new RuntimeException("No breadcrumb found: expected title: $docTitle")
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

class PatternCapturer {

    private final Pattern pattern
    private final List<String> hits

    PatternCapturer(String pattern) {
        this.pattern = Pattern.compile(pattern)
        this.hits = new ArrayList()
    }


    String tryMatches(String text) {
        def matcher = pattern.matcher(text)
        if (matcher.matches()) {
            def group = matcher.group(1)
            hits.add(group)
            return group
        }
        return null
    }

    void remove(String text) {
        hits.remove(text)
    }

    int size() {
        return hits.size()
    }

    List<String> getHits() {
        return hits
    }
}


return true
