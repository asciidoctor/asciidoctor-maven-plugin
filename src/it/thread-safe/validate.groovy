import java.nio.charset.Charset
import java.nio.file.Files

String fileTemplate = "asciidoctor-project-%s/target/docs/sample.html"

Set<String> contentOfFiles = new HashSet<>()

for (int i = 1; i <= 3; i++) {
    File file = new File(basedir, String.format(fileTemplate, i))
    println("Checking for existence of " + file)
    if (!file.isFile()) {
        throw new Exception("Missing file " + file)
    }

    StringBuilder contentOfFile = new StringBuilder()
    for (String line : Files.readAllLines(file.toPath(), Charset.forName("UTF-8"))) {
        contentOfFile.append(line)
    }
    contentOfFiles.add(contentOfFile.toString())
}

if (contentOfFiles.size() == 1) {
    return true
} else {
    throw new Exception("All files must contain the same content")
}

