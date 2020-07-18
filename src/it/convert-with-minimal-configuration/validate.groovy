File outputDir = new File(basedir, 'target/generated-docs');

def expectedFiles = ['sample.html']

for (String expectedFile : expectedFiles) {
    File file = new File(outputDir, expectedFile)
    if (!file.isFile())
        throw new Exception("Missing file $file")
}

return true