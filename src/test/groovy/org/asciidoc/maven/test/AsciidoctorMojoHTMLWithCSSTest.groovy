package org.asciidoc.maven.test

import org.asciidoc.maven.AsciidoctorMojo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Specification

/**
 *
 */
class AsciidoctorMojoHTMLWithCSSTest extends Specification {

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
        outputDir.list().toList().isEmpty() == false
        outputDir.list().toList().contains 'sample.html'

        File sampleOutput = new File('sample.html', outputDir)

        Document document = Jsoup.parse(sampleOutput, null);
        document.select("link[rel=stylesheet][href=style.css]").size() == 1
    }

}
