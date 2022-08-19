final File outputDir = new File(basedir, "target/generated-docs");

final def expectedFiles = ["sample.html"]

expectedFiles.each { it ->
    File file = new File(outputDir, it);
    System.out.println("Checking for existence of " + file)
    if (!file.exists() || !file.isFile()) {
        throw new Exception("Missing file " + file)
    }
    if (!file.text.startsWith("<!DOCTYPE html>")) {
        throw new Exception("Expected file does not contain HTML: " + file)
    }
}

return true