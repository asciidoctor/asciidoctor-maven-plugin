package org.asciidoc.maven.test

import org.asciidoc.maven.AsciidoctorMojo
import spock.lang.Specification

/**
 *
 */
class AsciidoctorMojoTest extends Specification {
    def "renders docbook"() {
        when:
            File srcDir = new File('target/test-classes/src/asciidoc')
            File outputDir = new File('target/asciidoc-output')

            if (!outputDir.exists())
                outputDir.mkdir()

            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.backend = 'docbook'
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.sourceDocumentName = new File(srcDir, 'sample.asciidoc')
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.xml')

            File sampleOutput = new File('sample.xml', outputDir)
            sampleOutput.length() > 0
    }

    def "renders html"() {
        when:
            File srcDir = new File('target/test-classes/src/asciidoc')
            File outputDir = new File('target/asciidoc-output')

            if (!outputDir.exists())
                outputDir.mkdir()

            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.backend = 'html'
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.headerFooter = true
            mojo.attributes = [toc: '', stylesheet: false]
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.html')

            File sampleOutput = new File('sample.html', outputDir)
            sampleOutput.length() > 0
            String text = sampleOutput.getText()
            text.contains('id="toc"')
            !text.contains('link rel="stylesheet"')
    }
}
