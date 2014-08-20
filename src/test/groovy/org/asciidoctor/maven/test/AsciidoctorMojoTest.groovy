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
            mojo.sourceDocumentName = 'sample.asciidoc'
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
            // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely in the test!
            // Believe it or not, null is the equivalent of writing <toc/> in the XML configuration
            mojo.attributes['toc'] = null
            mojo.attributes['linkcss!'] = ''
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.html')

            File sampleOutput = new File('sample.html', outputDir)
            sampleOutput.length() > 0
            String text = sampleOutput.getText()
            text.contains('id="toc"')
            text.contains('Asciidoctor default stylesheet')
            !text.contains('<link rel="stylesheet" href="./asciidoctor.css">')
            text.contains('<pre class="CodeRay highlight">')
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
            mojo.imagesDir = ''
            mojo.outputDirectory = outputDir
            mojo.sourceDirectory = srcDir
            mojo.sourceDocumentName = 'sample-embedded.adoc'
            mojo.backend = 'html'
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.html')

            File sampleOutput = new File(outputDir, 'sample-embedded.html')
            sampleOutput.length() > 0
            String text = sampleOutput.getText()
            text.contains('Asciidoctor default stylesheet')
            text.contains('data:image/png;base64,iVBORw0KGgo')
            text.contains('font-awesome.min.css')
            text.contains('i class="fa icon-tip"')
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
            mojo.sourceDirectory = srcDir
            mojo.sourceDocumentName = 'attribute-missing.adoc'
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
            mojo.sourceDirectory = srcDir
            mojo.sourceDocumentName = 'attribute-missing.adoc'
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
            mojo.sourceDirectory = srcDir
            mojo.sourceDocumentName = 'attribute-missing.adoc'
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
            mojo.sourceDirectory = srcDir
            mojo.sourceDocumentName = 'attribute-undefined.adoc'
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
            mojo.sourceDirectory = srcDir
            mojo.sourceDocumentName = 'attribute-undefined.adoc'
            mojo.backend = 'html'
            mojo.attributeMissing = 'drop-line'
            mojo.execute()
        then:
            File sampleOutput = new File(outputDir, 'attribute-undefined.html')
            String text = sampleOutput.getText()
            !text.contains('Here is a line that has an attribute that is')
            !text.contains('{set: name!}')
    }

    // Test for Issue 62
    def 'setting_boolean_values'() {
        given:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output-issue-62')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.outputDirectory = outputDir
            mojo.sourceDirectory = srcDir
            mojo.sourceDocumentName = 'sample.asciidoc'
            mojo.backend = 'html'
            // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely in the test!
            // Believe it or not, null is the equivalent of writing <toc/> in the XML configuration
            mojo.attributes.put('toc2', 'true')
            mojo.execute()
        then:
            File sampleOutput = new File(outputDir, 'sample.html')
            String text = sampleOutput.getText()
            text.contains('class="toc2"')

    }

    // Test for Issue 62 (unset)
    def 'unsetting_boolean_values'() {
        given:
        File srcDir = new File('target/test-classes/src/asciidoctor')
        File outputDir = new File('target/asciidoctor-output-issue-62-unset')

        if (!outputDir.exists())
            outputDir.mkdir()
        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.outputDirectory = outputDir
        mojo.sourceDirectory = srcDir
        mojo.sourceDocumentName = 'sample.asciidoc'
        mojo.backend = 'html'
        // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely in the test!
        // Believe it or not, null is the equivalent of writing <toc/> in the XML configuration
        mojo.attributes.put('toc2', 'false')
        mojo.execute()
        then:
        File sampleOutput = new File(outputDir, 'sample.html')
        String text = sampleOutput.getText()
        !text.contains('class="toc2"')
    }

    def 'test_imageDir_properly_passed'() {
        given:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output-imageDir')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.outputDirectory = outputDir
            mojo.sourceDirectory = srcDir
            mojo.sourceDocumentName = 'imageDir.adoc'
            mojo.backend = 'html'
            mojo.imagesDir = 'images-dir'
            mojo.execute()
        then:
            File sampleOutput = new File(outputDir, 'imageDir.html')
            String text = sampleOutput.getText()
            text.contains('<img src="images-dir/my-cool-image.jpg" alt="my cool image">')
    }

    def 'includes_test'() {
        given:
            File srcDir = new File('target/test-classes/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output-include-test')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.sourceDocumentName = new File('main-document.adoc')
            mojo.backend = 'html'
            mojo.execute()
        then:
            File mainDocumentOutput = new File(outputDir, 'main-document.html')
            String text = mainDocumentOutput.getText()
            text.contains('This is the parent document')
            text.contains('This is an included file.')
    }

    def 'issue-78'() {
        given:
        File srcDir = new File('target/test-classes/src/asciidoctor/issue-78')
        File outputDir = new File('target/asciidoctor-output-issue-78')

        if (!outputDir.exists())
            outputDir.mkdir()
        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.sourceDirectory = srcDir
        mojo.outputDirectory = outputDir
        mojo.sourceDocumentName = new File('main.adoc')
        mojo.doctype = 'book'
        mojo.embedAssets = true
        // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely in the test!
        // Believe it or not, null is the equivalent of writing <toc/> in the XML configuration
        mojo.attributes['toc'] = 'true'
        mojo.backend = 'html'
        mojo.execute()
        then:
        File mainDocumentOutput = new File(outputDir, 'main.html')
        File imageFile = new File(outputDir, 'images/halliburton_lab.jpg')
        imageFile.exists();
        String text = mainDocumentOutput.getText()
        text.contains("<p>Here&#8217;s an image:</p>")
        text.contains('<img src="data:image/jpg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4gzESUNDX1BST0ZJTEUAAQEAAA')
    }
}
