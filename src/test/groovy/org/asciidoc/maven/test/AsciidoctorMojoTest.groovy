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

    def "asciidoc file extension can be changed"() {
        given: 'an empty output directory'
            def outputDir = new File('target/asciidoc-output')
            outputDir.deleteDir()

        when: 'asciidoctor mojo is called with extension foo and bar and it exists a sample1.foo and a sample2.bar'
            def srcDir = new File('target/test-classes/src/asciidoc')

            outputDir.mkdirs()

            new File(srcDir, 'sample1.foo').withWriter {
                it << '''
                    Document Title
                    ==============

                    foo
                    '''.stripIndent()
            }
            new File(srcDir, 'sample2.bar').withWriter {
                it << '''
                    Document Title
                    ==============

                    bar
                    '''.stripIndent()
            }

            def mojo = new AsciidoctorMojo()
            mojo.backend = 'html'
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.extensions = [ 'foo', 'bar' ]
            mojo.execute()

        then: 'sample1.html and sample2.html exist and contain the extension of the original file'
            def outputs = outputDir.list().toList()
            outputs.size() >= 2
            outputs.contains('sample1.html')
            outputs.contains('sample2.html')

            new File(outputDir, 'sample1.html').text.contains('foo')
            new File(outputDir, 'sample2.html').text.contains('bar')
    }

    def "header footer is enabled by default"() {
        when:
          AsciidoctorMojo mojo = new AsciidoctorMojo()
        then:
          mojo.headerFooter == true
    }
}
