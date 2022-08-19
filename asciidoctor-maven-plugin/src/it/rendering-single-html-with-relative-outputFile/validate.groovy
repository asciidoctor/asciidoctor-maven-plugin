final File outputDir = new File(basedir, 'target/generated-docs')
final File expectedFile = new File(outputDir, 'a_path/custom-filename.html')

if (!expectedFile.exists()) {
    throw new Exception("Missing file " + file)
}

return true