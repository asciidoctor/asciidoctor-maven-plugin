final File outputDir = new File(basedir, "target/generated-docs");
final File expectedFile = new File(outputDir, 'attributes-example.html')

if (!expectedFile.exists()) {
    throw new Exception("Missing file " + expectedFile)
}

expectedFile.text.with { outputContent ->
    assertContains(outputContent, 'This attribute is set in the plugin configuration: plugin configuration')
    assertContains(outputContent, 'This attribute is set in the execution configuration: execution configuration')
    assertContains(outputContent, 'This attribute is set in the project&#8217;s properties: project property configuration')
}

void assertContains(String text, String expectedValueToContain) {
    if (!text.contains(expectedValueToContain))
        throw new Exception("Expected value '$expectedValueToContain' not found")
}

return true