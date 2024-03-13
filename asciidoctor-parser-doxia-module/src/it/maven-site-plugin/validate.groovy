import java.util.stream.Collectors

File outputDir = new File(basedir, "target/site")

final def expectedFile = "sample.html"
final def unexpectedFiles = ["_include.html"]

final File file = new File(outputDir, expectedFile)

new FileAsserter().isNotEmpty(file)

String htmlContent = file.text
new HtmlAsserter(htmlContent).with { asserter ->

    asserter.containsMetadata("author", "The Author")
    asserter.containsMetadata("date", "2024-02-07 23:36:29")

    asserter.containsBreadcrumbs("Sample")

    asserter.containsDocumentTitle("Sample")
    asserter.containsPreambleStartingWith("This is an example")
    asserter.containsSectionTitle("First section", 2)
    asserter.containsSectionTitle("Sub section", 3)

    asserter.containsSectionTitle("Features", 2)
    asserter.containsParagraph("This is the second section of the page.")

    asserter.containsSectionTitle("Image", 3)
    asserter.containsImage("images/asciidoctor-logo.png")

    asserter.containsSectionTitle("Table", 3)
    asserter.containsTable(2, 3, ["Name", "Language"], "Ruby platforms")

    asserter.containsSectionTitle("Literal", 3)
    asserter.containsLiteral("This is a literal.")

    asserter.containsSectionTitle("Code blocks", 3)

    asserter.containsSectionTitle("Lists", 3)

    asserter.containsSectionTitle("Unordered list", 4)
    asserter.containsUnorderedList("Apples", "Oranges", "Walnuts", "Almonds")

    asserter.containsSectionTitle("Unordered list with formatting", 4)
    asserter.containsUnorderedList(strong("Apples"), italics("Oranges"), subscript("Walnuts"), monospace("Almonds"), htmlLink('https://some-link.here', 'link'))

    asserter.containsSectionTitle("Ordered list", 4)
    asserter.containsOrderedList("Protons", "Electrons", "Neutrons")

    asserter.containsSectionTitle("Description list", 4)
    asserter.descriptionListTerm("Operating Systems")
    asserter.descriptionListTerm("Linux")
    asserter.contains("<li>Fedora")
    asserter.containsUnorderedList("Desktop")
    asserter.contains("<li>Ubuntu")
    asserter.containsUnorderedList("Desktop", "Server")
    asserter.descriptionListTerm("BSD")
    asserter.containsOrderedList("FreeBSD", "NetBSD")
}

String strong(String text) {
    return htmlElement('strong', text)
}

String italics(String text) {
    return htmlElement('em', text)
}

String subscript(String text) {
    return htmlElement('sub', text)
}

String monospace(String text) {
    return htmlElement('code', text)
}

String htmlLink(String url, String label) {
    return "<a href=\"${url}\">${label}</a>"
}

String htmlElement(String element, String text) {
    return "<${element}>${text}</${element}>"
}

for (String unexpectedFile : unexpectedFiles) {
    File candidate = new File(outputDir, unexpectedFile)
    if (candidate.isFile()) {
        throw new Exception("Unexpected candidate $candidate")
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

class HtmlAsserter {

    private final String content

    private int lastAssertionCursor = 0

    HtmlAsserter(String content) {
        this.content = clean(content)
    }

    void fail(String msg) {
        throw new Exception(msg)
    }

    private void assertFound(String element, String value, int positionFound) {
        if (positionFound < 0)
            fail("$element not found: $value (current positionFound: $lastAssertionCursor)")
        lastAssertionCursor = positionFound + value.length()
    }

    private int find(String value) {
        return content.indexOf(value, lastAssertionCursor)
    }

    void contains(String text) {
        def found = find(text)
        assertFound("HTML text", text, found)
    }

    void containsMetadata(String name, String value) {
        def found = find("<meta name=\"${name}\" content=\"${value}\" />")
        assertFound("head/meta", value, found)
    }

    void containsBreadcrumbs(String value) {
        def found = find("<li class=\"active \">${value}</li>")
        assertFound("Breadcrumb", value, found)
    }

    void containsDocumentTitle(String value) {
        def found = find("<h1>$value</h1>")
        assertFound("Document Title", value, found)
    }

    void containsPreambleStartingWith(String value) {
        def found = find("<p>$value")
        assertFound("Preamble", value, found)
    }

    void containsSectionTitle(String value, int level) {
        def found = -1

        def id = value.toLowerCase().replaceAll(" ", "_")
        if (level == 2) {
            found = find("<h2><a id=\"$id\"></a>$value</h2>")
        } else if (level == 3) {
            found = find("<h3><a id=\"$id\"></a>$value</h3>")
        } else if (level == 4) {
            found = find("<h4><a id=\"$id\"></a>$value</h4>")
        }
        assertFound("Section Title (level:$level)", value, found)
    }

    void containsParagraph(String value) {
        def found = find("<p>$value</p>")
        assertFound("Preamble", value, found)
    }

    void containsLiteral(String value) {
        def found = find("<div><pre>$value</pre></div>")
        assertFound("Literal", value, found)
    }

    void containsImage(String value) {
        def found = find("<img src=\"$value\" alt=\"Asciidoctor is awesome\">")
        assertFound("Image", value, found)
    }

    void containsUnorderedList(String... values) {
        def found = find("<ul><li>${values.join('</li><li>')}</li></ul>")
        assertFound("Unordered list", values.join(','), found)
    }

    void containsOrderedList(String... values) {
        def found = find("<ol style=\"list-style-type: decimal;\"><li>${values.join('</li><li>')}</li></ol>")
        assertFound("Ordered list", values.join(','), found)
    }

    void descriptionListTerm(String term) {
        def found = find("<dt>${term}</dt>")
        assertFound("Description list", term, found)
    }

    void containsTable(int columns, int rows, List<String> headers, String caption) {
        def start = content.indexOf("<table", lastAssertionCursor)
        def end = content.indexOf("</table>", lastAssertionCursor) + "</table>".length()
        if (start < 0 || end < 0)
            fail("Table not found ($start, $end)")

        def table = content.substring(start, end)

        assertTableCaption(table, caption)
        assertTableHeaders(table, headers)
        assertTableColumns(table, columns, rows)
        assertTableRows(table, rows)

        lastAssertionCursor = end
    }

    void assertTableCaption(String htmlBlock, String caption) {
        def start = htmlBlock.indexOf("<caption>") + "<caption>".length()
        def end = htmlBlock.indexOf("</caption>")
        if (start < 0 || end < 0)
            fail("Caption not found ($start, $end)")

        def captionHtml = htmlBlock.substring(start, end)

        if (captionHtml != "Table 1. $caption") {
            fail("Caption not valid. Found: $captionHtml")
        }
    }

    void assertTableHeaders(String htmlBlock, List<String> headers) {
        def actualHeaders = Arrays.stream(htmlBlock.split("<"))
                .filter(line -> line.startsWith("th>"))
                .map(line -> {
                    return line.substring("th>".length())
                })
                .collect(Collectors.toList())

        if (actualHeaders != headers)
            fail("Table headers not valid. Found: $actualHeaders, expected: $headers")
    }

    void assertTableColumns(String htmlBlock, int columns, int rows) {
        def count = htmlBlock.count("<td")
        if (count != (columns * rows)) {
            fail("Number of columns do not match. Found: ${count / rows}, expected: $columns")
        }
    }

    void assertTableRows(String htmlBlock, int rows) {
        def count = htmlBlock.count("<tr")
        if (count != rows + 1) {
            fail("Number of rows do not match. Found: ${count - 1}, expected: $rows")
        }
    }

    // Removes linebreaks to validate to avoid OS dependant issues.
    private String clean(String value) {
        return value.replaceAll("\r\n", "")
                .replaceAll("\n", "")
                .trim();
    }
}

return true
