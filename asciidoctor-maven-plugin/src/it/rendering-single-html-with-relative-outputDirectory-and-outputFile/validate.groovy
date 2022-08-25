final File outputDir = new File(basedir, 'target/output_path')
final File expectedFile = new File(outputDir, 'a_path/custom-filename.html')

if (!expectedFile.exists()) {
    throw new Exception("Missing file " + file)
}

return true