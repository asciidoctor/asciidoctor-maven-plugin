package org.asciidoc.maven.test

import org.asciidoc.maven.AsciidoctorMojo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
        mojo.execute()
        then:
        outputDir.list().toList().isEmpty() == false
        outputDir.list().toList().contains('sample.html')

        File sampleOutput = new File('sample.html', outputDir)
        sampleOutput.length() > 0
    }

    def "renders html with css style"() {
        when:
        File srcDir = new File('target/test-classes/src/asciidoc')
        File outputDir = new File('target/asciidoc-output')

        if (!outputDir.exists())
            outputDir.mkdir()

        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.backend = 'html'
        mojo.sourceDirectory = srcDir
        mojo.outputDirectory = outputDir
        mojo.cssStyles = [new File('target/test-classes/src/asciidoc/style.css')] as File[]

        mojo.execute()
        then:
        !outputDir.list().toList().empty
        outputDir.list().toList().contains 'sample.html'

        File sampleOutput = new File('sample.html', outputDir)

        Document document = Jsoup.parse(sampleOutput, null);
        document.select("link[rel=stylesheet][href=style.css]").size() == 1
    }
}
