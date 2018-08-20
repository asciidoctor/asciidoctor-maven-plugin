import java.nio.charset.Charset
import java.nio.file.Files

String fileTemplate = "asciidoctor-project-%s/target/docs/sample.html"

List<String> contents = new ArrayList<>()
List<String> files = new ArrayList<>()
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
    contents.add(contentOfFile.toString())
    files.add(String.format(fileTemplate, i))

}


if(!contents.get(0).equals(contents.get(1))) {
    throw new Exception(String.format("The content of two files are different.\n\n%s: %s\n%s: %s", files.get(0), contents.get(0), files.get(1), contents.get(1) ))
}

if(!contents.get(1).equals(contents.get(2))) {
    throw new Exception(String.format("The content of two files are different.\n\n%s: %s\n%s: %s", files.get(1), contents.get(1), files.get(2), contents.get(2) ))
}

if(!contents.get(2).equals(contents.get(0))) {
    throw new Exception(String.format("The content of two files are different.\n\n%s: %s\n%s: %s", files.get(2), contents.get(2), files.get(0), contents.get(0) ))
}

return true