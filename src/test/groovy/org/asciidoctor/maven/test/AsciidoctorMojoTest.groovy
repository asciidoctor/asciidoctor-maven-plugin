package org.asciidoctor.maven.test

import org.asciidoctor.maven.AsciidoctorMojo
import spock.lang.Specification

/**
 *
 */
class AsciidoctorMojoTest extends Specification {
    def "renders docbook"() {
        setup:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
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
        setup:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.backend = 'html'
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.headerFooter = true
            mojo.sourceHighlighter = 'coderay'
            mojo.attributes['toc'] = true
            mojo.attributes['linkcss!'] = ''
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.html')

            File sampleOutput = new File('sample.html', outputDir)
            sampleOutput.length() > 0
            String text = sampleOutput.getText()
            text.contains('id="toc"')
            !text.contains('link rel="stylesheet"')
            text.contains('<pre class="CodeRay">')
    }

    def "asciidoc file extension can be changed"() {
        given: 'an empty output directory'
            def outputDir = new File('target/asciidoctor-output')
            outputDir.delete()

        when: 'asciidoctor mojo is called with extension foo and bar and it exists a sample1.foo and a sample2.bar'
            def srcDir = new File('target/test-classes/src/asciidoctor')

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

    def "embedding resources"() {
        setup:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.attributes["icons"] = "font"
            mojo.embedAssets = true
            mojo.outputDirectory = outputDir
            mojo.sourceDocumentName = new File(srcDir, 'sample-embedded.adoc')
            mojo.backend = 'html'
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.html')

            File sampleOutput = new File(outputDir, 'sample-embedded.html')
            sampleOutput.length() > 0
            String text = sampleOutput.getText()
            text.contains('font-awesome.min.css')
            text.contains('i class="icon-tip')
    }

    def "missing-attribute skip"() {
        given:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.outputDirectory = outputDir
            mojo.sourceDocumentName = new File(srcDir, 'attribute-missing.adoc')
            mojo.backend = 'html'
            mojo.attributeMissing = 'skip'
            mojo.execute()
        then:
            File sampleOutput = new File(outputDir, 'attribute-missing.html')
            String text = sampleOutput.getText()
            text.contains('Here is a line that has an attribute that is {missing}!')
    }

    def "missing-attribute drop"() {
        given:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.outputDirectory = outputDir
            mojo.sourceDocumentName = new File(srcDir, 'attribute-missing.adoc')
            mojo.backend = 'html'
            mojo.attributeMissing = 'drop'
            mojo.execute()
        then:
            File sampleOutput = new File(outputDir, 'attribute-missing.html')
            String text = sampleOutput.getText()
            text.contains('Here is a line that has an attribute that is !')
            !text.contains('{name}')
    }

    def "missing-attribute drop-line"() {
        given:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.outputDirectory = outputDir
            mojo.sourceDocumentName = new File(srcDir, 'attribute-missing.adoc')
            mojo.backend = 'html'
            mojo.attributeMissing = 'drop-line'
            mojo.execute()
        then:
            File sampleOutput = new File(outputDir, 'attribute-missing.html')
            String text = sampleOutput.getText()
            !text.contains('Here is a line that has an attribute that is')
            !text.contains('{set: name!}')
    }

    def "undefined-attribute drop"() {
        given:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.outputDirectory = outputDir
            mojo.sourceDocumentName = new File(srcDir, 'attribute-undefined.adoc')
            mojo.backend = 'html'
            mojo.attributeUndefined = 'drop'
            mojo.execute()
        then:
            File sampleOutput = new File(outputDir, 'attribute-undefined.html')
            String text = sampleOutput.getText()
            text.contains('Here is a line that has an attribute that is !')
            !text.contains('{set: name!}')
    }

    def "undefined-attribute drop-line"() {
        given:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.outputDirectory = outputDir
            mojo.sourceDocumentName = new File(srcDir, 'attribute-undefined.adoc')
            mojo.backend = 'html'
            mojo.attributeMissing = 'drop-line'
            mojo.execute()
        then:
            File sampleOutput = new File(outputDir, 'attribute-undefined.html')
            String text = sampleOutput.getText()
            !text.contains('Here is a line that has an attribute that is')
            !text.contains('{set: name!}')
    }
}
