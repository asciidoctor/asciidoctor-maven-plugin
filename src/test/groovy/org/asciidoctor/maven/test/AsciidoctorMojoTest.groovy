package org.asciidoctor.maven.test

import groovy.io.FileType

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.RegexFileFilter
import org.apache.maven.model.Resource
import org.apache.maven.plugin.MojoExecutionException
import org.asciidoctor.maven.AsciidoctorMojo
import org.asciidoctor.maven.test.plexus.mock.MockPlexusContainer
import spock.lang.Specification

/**
 *
 */
class AsciidoctorMojoTest extends Specification {

    static final String DEFAULT_SOURCE_DIRECTORY = 'target/test-classes/src/asciidoctor'
    static final String MULTIPLE_SOURCES_OUTPUT = 'target/asciidoctor-output/multiple-sources'

    MockPlexusContainer mockPlexusContainer = new MockPlexusContainer()

    Resource defaultSource = [
            directory : DEFAULT_SOURCE_DIRECTORY,
            includes : ['sample.asciidoc']
        ]

    def "renders docbook"() {
        setup:
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'docbook'
            mojo.sources = [defaultSource]
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.xml')

            File sampleOutput = new File('sample.xml', outputDir)
            sampleOutput.length() > 0
    }

    def "renders a single html"() {
        setup:
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html'
            mojo.sources = [defaultSource]
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
            text.contains('<body class="article">')
            text.contains('id="toc"')
            text.contains('Asciidoctor default stylesheet')
            !text.contains('<link rel="stylesheet" href="./asciidoctor.css">')
            text.contains('<pre class="CodeRay highlight">')
    }

    def "should honor doctype set in document"() {
        setup:
            File outputDir = new File('target/asciidoctor-output')
            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html'
            mojo.sources = [[
                    directory : DEFAULT_SOURCE_DIRECTORY,
                    includes : ['book.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.headerFooter = true
            mojo.attributes['linkcss'] = ''
            mojo.attributes['copycss!'] = ''
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('book.html')
            File sampleOutput = new File('book.html', outputDir)
            sampleOutput.length() > 0
            String text = sampleOutput.getText()
            text.contains('<body class="book">')
    }

    def "asciidoc file extension can be defined using includes"() {
        given: 'an empty output directory'
            def outputDir = new File('target/asciidoctor-output/file-extensions')
            FileUtils.deleteDirectory(outputDir)
            outputDir.mkdir()

        when: 'mojo is called with includes patterns foo and bar and it exists a sample1.foo and a sample2.bar'
            def srcDir = new File('target/test-classes/src/asciidoctor/file-extensions')
            srcDir.mkdir()

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

            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html'
            mojo.sources = [[
                    directory : DEFAULT_SOURCE_DIRECTORY,
                    includes : ['**/*.foo', '**/*bar']
                ] as Resource]
            mojo.outputDirectory = outputDir
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

    def "should require library"() {
        setup:
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.requires = ['time'] as List
            mojo.backend = 'html'
            mojo.outputDirectory = outputDir
            mojo.sources = [defaultSource]
            mojo.execute()
        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.list().toList().contains('sample.html')
            assert "constant".equals(org.asciidoctor.internal.JRubyRuntimeContext.get().evalScriptlet('defined? ::DateTime').toString())
    }

    def "embedding resources"() {
        setup:
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.attributes["icons"] = "font"
            mojo.embedAssets = true
            mojo.imagesDir = ''
            mojo.outputDirectory = outputDir
            mojo.sources = [[
                    directory : DEFAULT_SOURCE_DIRECTORY,
                    includes: ['sample-embedded.adoc']
                ] as Resource]
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
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.outputDirectory = outputDir
            mojo.sources = [[
                    directory : DEFAULT_SOURCE_DIRECTORY,
                    includes: ['attribute-missing.adoc']
                ] as Resource]
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
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.outputDirectory = outputDir
            mojo.sources = [[
                    directory : DEFAULT_SOURCE_DIRECTORY,
                    includes: ['attribute-missing.adoc']
                ] as Resource]
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
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.outputDirectory = outputDir
            mojo.sources = [[
                    directory : DEFAULT_SOURCE_DIRECTORY,
                    includes: ['attribute-missing.adoc']
                ] as Resource]
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
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.outputDirectory = outputDir
            mojo.sources = [[
                    directory : DEFAULT_SOURCE_DIRECTORY,
                    includes: ['attribute-undefined.adoc']
                ] as Resource]
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
            File outputDir = new File('target/asciidoctor-output')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.outputDirectory = outputDir
            mojo.sources = [[
                    directory : DEFAULT_SOURCE_DIRECTORY,
                    includes: ['attribute-undefined.adoc']
                ] as Resource]
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
            File outputDir = new File('target/asciidoctor-output-issue-62')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.outputDirectory = outputDir
            mojo.sources = [defaultSource]
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
            File outputDir = new File('target/asciidoctor-output-issue-62-unset')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.outputDirectory = outputDir
            mojo.sources = [defaultSource]
            mojo.backend = 'html'
            // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely
            // in the test!
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
            File outputDir = new File('target/asciidoctor-output-imageDir')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.outputDirectory = outputDir
            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: ['imageDir.adoc']
                ] as Resource]
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
            File outputDir = new File('target/asciidoctor-output-include-test')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes:  ['main-document.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.backend = 'html'
            mojo.execute()
        then:
            File mainDocumentOutput = new File(outputDir, 'main-document.html')
            String text = mainDocumentOutput.getText()
            text.contains('This is the parent document')
            text.contains('This is an included file.')
    }

    def 'skip'() {
        given:
            File outputDir = new File('target/asciidoctor-output-skip-test')
            if (outputDir.exists())
                outputDir.delete()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: 'main-document.adoc'
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.backend = 'html'
            mojo.skip = true
            mojo.execute()
        then:
            !outputDir.exists()
    }

    def 'issue-78'() {
        given:
            File outputDir = new File('target/asciidoctor-output-issue-78')

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/issue-78",
                    includes: ['main.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.doctype = 'book'
            mojo.embedAssets = true
            // IMPORTANT Maven can only assign string values or null, so we have to emulate the value precisely
            // in the test!
            // Believe it or not, null is the equivalent of writing <toc/> in the XML configuration
            mojo.attributes['toc'] = 'true'
            mojo.backend = 'html'
            mojo.execute()
        then:
            File mainDocumentOutput = new File(outputDir, 'main.html')
            File imageFile = new File(outputDir, 'images/halliburton_lab.jpg')
            imageFile.exists()
            String text = mainDocumentOutput.getText()
            text.contains("<p>Here&#8217;s an image:</p>")
            text.contains('<img src="data:image/jpg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4gzESUNDX1BST0ZJTEUAAQEAAA')
    }

    /**
     * Tests CodeRay source code highlighting options.
     */
    def 'code highlighting - coderay'() {
        setup:
            File outputDir = new File('target/asciidoctor-output-sourceHighlighting/coderay')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: ['main-document.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.sourceHighlighter = 'coderay'
            mojo.backend = 'html'
            mojo.execute()

        then:
            File mainDocumentOutput = new File(outputDir, 'main-document.html')
            String text = mainDocumentOutput.getText()
            text.contains('CodeRay')
    }

    /**
     * Tests Highlight.js source code highlighting options.
     */
    def 'code highlighting - highlightjs'() {
        setup:
            File outputDir = new File('target/asciidoctor-output-sourceHighlighting/highlightjs')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: ['main-document.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.sourceHighlighter = 'highlightjs'
            mojo.backend = 'html'
            mojo.execute()

        then:
            File mainDocumentOutput = new File(outputDir, 'main-document.html')
            String text = mainDocumentOutput.getText()
            text.contains('highlight')
    }

    /**
     * Tests Prettify source code highlighting options.
     */
    def 'code highlighting - prettify'() {
        setup:
            File outputDir = new File('target/asciidoctor-output-sourceHighlighting/prettify')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: ['main-document.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.sourceHighlighter = 'prettify'
            mojo.backend = 'html'
            mojo.execute()

        then:
            File mainDocumentOutput = new File(outputDir, 'main-document.html')
            String text = mainDocumentOutput.getText()
            text.contains('prettify')
    }

    /**
     * Tests behavior when source code highlighting with Pygments is specified.
     *
     * Test checks that an exception is not thrown.
     */
    def 'code highlighting - pygments'() {
        setup:
            File srcDir = new File('src/test/resources/src/asciidoctor')
            File outputDir = new File('target/asciidoctor-output-sourceHighlighting/pygments')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: ['main-document.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.sourceHighlighter = 'pygments'
            mojo.backend = 'html'
            mojo.execute()

        then:
            File mainDocumentOutput = new File(outputDir, 'main-document.html')
            String text = mainDocumentOutput.getText()
            text.contains('Pygments is not available.')
            text.contains('<pre class="pygments highlight">')
    }

    /**
     * Tests behaviour when an invalid source code highlighting option is set.
     *
     * Test checks that no additional CSS are added.
     */
    def 'code highlighting - nonExistent'() {
        setup:
            File outputDir = new File('target/asciidoctor-output-sourceHighlighting/nonExistent')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: ['main-document.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.sourceHighlighter = 'nonExistent'
            mojo.backend = 'html'
            mojo.execute()

        then:
            File mainDocumentOutput = new File(outputDir, 'main-document.html')
            String text = mainDocumentOutput.getText()
            // No extra CSS is added other than Asciidoctor's default
            text.count('<style>') == 1
    }

    /**
     * Tests for relative folder structures treatment
     */
    static final FileFilter DIRECTORY_FILTER = {File f -> f.isDirectory() && !f.name.startsWith("_")} as FileFilter
    static final String ASCIIDOC_REG_EXP_EXTENSION = '.*\\.a((sc(iidoc)?)|d(oc)?)$'

    /**
     * Validates that the folder structures under two paths are the same
     *
     * @param expected
     *         list of expected folders
     * @param actual
     *         list of actual folders (the ones to validate)
     */
    private void assertEqualsStructure (File[] expected, File[] actual) {

        assert expected.length == actual.length
        expected*.name.containsAll(actual*.name)
        actual*.name.containsAll(expected*.name)

        for (File actualFile in actual) {
            File expectedFile = expected.find {it.getName() == actualFile.getName()}
            assert expectedFile != null

            // checks that at least the number of html files and asciidoc are the same in each folder
            File[] htmls = actualFile.listFiles({File f -> f.getName() ==~ /.+html/} as FileFilter)
            if (htmls) {
                File[] asciidocs =  expectedFile.listFiles({File f ->
                    f.name ==~ ASCIIDOC_REG_EXP_EXTENSION && !f.name.startsWith("_")
                } as FileFilter)
                assert htmls.length == asciidocs.length
            }

            File[] expectedChildren =  expectedFile.listFiles(DIRECTORY_FILTER)
            File[] actualChildren =  actualFile.listFiles(DIRECTORY_FILTER)
            assertEqualsStructure(expectedChildren, actualChildren)
        }
    }


    /**
     * Tests the behaviour when: 
     *  - simple paths are used
     *  - preserveDirectories = true
     *  - relativeBaseDir = true
     *
     *  Expected:
     *   - all documents are rendered in the same folder structure found in the sourceDirectory
     *   - all documents are correctly rendered with the import
     */
    def 'should replicate source structure-standard paths'() {
        setup:
            File srcDir = new File('src/test/resources/src/asciidoctor/relative-path-treatment')
            File outputDir = new File('target/asciidoctor-output-relative')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html5'
            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/relative-path-treatment"
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.imagesDir = '.'
            mojo.preserveDirectories = true
            mojo.relativeBaseDir = true
            mojo.sourceHighlighter = 'prettify'
            mojo.attributes = ['icons':'font']
            mojo.execute()

        then:
            outputDir.list().toList().isEmpty() == false
            assertEqualsStructure(srcDir.listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
            def asciidocs = []
            outputDir.eachFileRecurse(FileType.FILES) {
                if (it.getName() ==~ /.+html/) asciidocs << it
            }
            asciidocs.size() == 6
            // Checks that all imports are found in the respective baseDir
            for (File renderedFile in asciidocs) {
                assert renderedFile.text.contains('Unresolved directive') == false
            }
        cleanup:
            // Avoids false positives in other tests
            FileUtils.deleteDirectory(outputDir)
    }

    /**
     * Tests the behaviour when: 
     *  - complex paths are used
     *  - preserveDirectories = true
     *  - relativeBaseDir = true
     *
     *  Expected:
     *   - all documents are rendered in the same folder structure found in the sourceDirectory
     *   - all documents are correctly rendered with the import
     */
    def 'should replicate source structure-complex paths'() {
        setup:
            File srcDir = new File('src/test/resources/src/asciidoctor/relative-path-treatment/../relative-path-treatment')
            File outputDir = new File('target/../target/asciidoctor-output-relative')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html5'
            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/relative-path-treatment/../relative-path-treatment"
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.preserveDirectories = true
            mojo.relativeBaseDir = true
            mojo.sourceHighlighter = 'coderay'
            mojo.attributes = ['icons':'font']
            mojo.execute()

        then:
            outputDir.list().toList().isEmpty() == false
            outputDir.listFiles({File f -> f.getName().endsWith('html')} as FileFilter).length == 1
            assertEqualsStructure(srcDir.listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
            def asciidocs = []
            outputDir.eachFileRecurse(FileType.FILES) {
                if (it.getName() ==~ /.+html/) asciidocs << it
            }
            asciidocs.size() == 6
            // Checks that all imports are found in the respective baseDir
            for (File renderedFile in asciidocs) {
                assert renderedFile.text.contains('Unresolved directive') == false
            }
        cleanup:
            // Avoid possible false positives in other tests
            FileUtils.deleteDirectory(outputDir)
    }

    /**
     * Tests the behaviour when: 
     *  - complex paths are used
     *  - preserveDirectories = false
     *  - relativeBaseDir = false
     *
     *  Expected:
     *   - all documents are rendered in the same outputDirectory. 1 document is overwritten
     *   - all documents but 1 (in the root) are incorrectly rendered because they cannot find the imported file
     */
    def 'should not replicate source structure-complex paths'() {
        setup:
            File srcDir = new File('src/test/resources/src/asciidoctor/relative-path-treatment/../relative-path-treatment')
            File outputDir = new File('target/../target/asciidoctor-output-relative')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html5'
            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/relative-path-treatment/../relative-path-treatment"
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.sourceHighlighter = 'coderay'
            mojo.execute()

        then:
            outputDir.list().toList().isEmpty() == false
            // 1 file is missing because 2 share the same name and 1 is overwritten in outputDirectory
            def asciidocs  = outputDir.listFiles({File f -> f.getName().endsWith('html')} as FileFilter)
			asciidocs.length == 5
            // folders are copied anyway
            assertEqualsStructure(srcDir.listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
			for (File renderedFile in asciidocs) {
				if (renderedFile.getName() != 'HelloWorld.html') {
					assert renderedFile.text.contains('Unresolved directive')
				}
			}
        cleanup:
            // Avoid possible false positives in other tests
            FileUtils.deleteDirectory(outputDir)
    }

    /**
     * Tests the behaviour when: 
     *  - simple paths are used
     *  - preserveDirectories = true
     *  - relativeBaseDir = false
     *
     *  Expected:
     *   - all documents are rendered in the same folder structure found in the sourceDirectory
     *   - all documents but 1 (in the root) are incorrectly rendered because they cannot find the imported file
     */
    def 'should replicate source structure-no baseDir rewrite'() {
        setup:
            File srcDir = new File('src/test/resources/src/asciidoctor/relative-path-treatment')
            File outputDir = new File('target/asciidoctor-output-relative')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html5'
            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/relative-path-treatment"
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.imagesDir = '.'
            mojo.preserveDirectories = true
			mojo.baseDir = srcDir
            //mojo.relativeBaseDir = true
            mojo.sourceHighlighter = 'prettify'
            mojo.attributes = ['icons':'font']
            mojo.execute()

        then:
            outputDir.list().toList().isEmpty() == false
            assertEqualsStructure(srcDir.listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
            def asciidocs = []
            outputDir.eachFileRecurse(FileType.FILES) {
                if (it.getName() ==~ /.+html/) asciidocs << it
            }
            asciidocs.size() == 6
            // Looks for import errors in all files but the one in the root folder
            for (File renderedFile in asciidocs) {
                if (renderedFile.getName() != 'HelloWorld.html') {
                    assert renderedFile.text.contains('Unresolved directive')
                }
            }

        cleanup:
            // Avoids false positives in other tests
            FileUtils.deleteDirectory(outputDir)
    }

    /**
     * Tests the behaviour when: 
     *  - simple paths are used 
     *  - preserveDirectories = false
     *  - relativeBaseDir = true
     *
     *  Expected: all documents are correctly rendered in the same folder 
     */
    def 'should not replicate source structure-baseDir rewrite'() {
        setup:
            File srcDir = new File('src/test/resources/src/asciidoctor/relative-path-treatment')
            File outputDir = new File('target/asciidoctor-output-relative')

        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html'
            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/relative-path-treatment"
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.imagesDir = '.'
            mojo.preserveDirectories = false
            mojo.relativeBaseDir = true
            mojo.sourceHighlighter = 'prettify'
            mojo.attributes = ['icons':'font']
            mojo.execute()

        then:
            assertEqualsStructure(srcDir.listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
			// all files are rendered in the outputDirectory
            def asciidocs = outputDir.listFiles({File f -> f.getName().endsWith('html')} as FileFilter)
			// 1 file is missing because 2 share the same name and 1 is overwritten in outputDirectory
            asciidocs.length == 5
            // Checks that all imports are found correctly because baseDir is adapted for each file
            for (File renderedFile in asciidocs) {
                assert renderedFile.text.contains('Unresolved directive') == false
            }

        cleanup:
            // Avoids false positives in other tests
            FileUtils.deleteDirectory(outputDir)
    }

    def 'project-version test'() {
        given:
            File outputDir = new File( 'target/asciidoctor-output-project-version-test' )

            if (!outputDir.exists()) {
                outputDir.mkdir()
            }
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: ['project-version.adoc']
                ] as Resource]
            mojo.outputDirectory = outputDir
            mojo.backend = 'html'
            mojo.attributes['project-version'] = "1.0-SNAPSHOT"
            mojo.execute()
        then:
            File mainDocumentOutput = new File( outputDir, 'project-version.html' )
            String text = mainDocumentOutput.getText()
            assert text =~ /[vV]ersion 1\.0-SNAPSHOT/
            text.contains( "This is the project version: 1.0-SNAPSHOT" )
    }

    def "when no sources are set looks into the default folder"() {
        setup:
            File outputDir = new File(MULTIPLE_SOURCES_OUTPUT)

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            Exception e = thrown()
            // checks Windows and Unix paths
            e.getMessage().contains('src\\main\\asciidoc') || e.getMessage().contains('src/main/asciidoc')
    }

    def "directory is mandatory in resources"() {
        setup:
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/file-pattern/${System.currentTimeMillis()}")

            if (!outputDir.exists())
                outputDir.mkdir()
            else
                FileUtils.deleteDirectory(outputDir)
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)
            // only includes files in the root path
            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                ] as Resource]
            // excludes all, nothing at all is added
            mojo.resources = [[
                    excludes: ['**/**']
                ] as Resource]
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            Exception e = thrown(MojoExecutionException)
            e.message == "Found empty resource directory"
            def files = outputDir.listFiles()
            files.size() == 0
    }

    def "setting a single source document name pattern"() {
        setup:
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/file-pattern/${System.currentTimeMillis()}")

            if (!outputDir.exists())
                outputDir.mkdir()
            else
                FileUtils.deleteDirectory(outputDir)
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)
            // only includes files in the root path
            mojo.sources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    includes: ['attribute-*.adoc']
                ] as Resource]
            // excludes all, nothing at all is added
            mojo.resources = [[
                    directory: DEFAULT_SOURCE_DIRECTORY,
                    excludes: ['**/**']
                ] as Resource]
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            files.size() == 2
            files*.name.containsAll(['attribute-missing.html', 'attribute-undefined.html'])
    }

    def  "including multiple source documents by name pattern"() {
        setup:
            File srcDir = new File("$DEFAULT_SOURCE_DIRECTORY/relative-path-treatment")
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/multi-sources/includes")

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)
            mojo.sources = [[
                    directory: srcDir.getPath(),
                    includes: ['HelloWorld.adoc', '**/*3.adoc', '**/*4.adoc']
                ] as Resource]
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            files*.name.findAll({it.endsWith('html')}).containsAll(['HelloWorld.html',
                                                                    'HelloWorld3.html',
                                                                    'HelloWorld4.html'])
            // validate that resources are also copied respecting the original structure
            assertEqualsStructure(srcDir.listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
    }


    def  "excluding multiple source documents by name pattern"() {
        setup:
            File srcDir = new File("$DEFAULT_SOURCE_DIRECTORY/relative-path-treatment")
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/multi-sources/excludes")

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: srcDir.getPath(),
                    excludes: ['**/*2.adoc']
                ] as Resource]
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            // Same result as previous test
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            files*.name.findAll({it.endsWith('html')}).containsAll(['HelloWorld.html',
                                                                    'HelloWorld3.html',
                                                                    'HelloWorld4.html'])
            // validate that resources are also copied respecting the original structure
            assertEqualsStructure(srcDir.listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
    }

    def "some source directory does no exist"() {
        setup:
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/multi-sources/error-source-not-found")
            String nonExistingPath = "this/is/fake"

        if (!outputDir.exists())
            outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/multiple-sources/sources-1"
                ] as Resource, [
                    directory: nonExistingPath
                ] as Resource, [
                    directory: "$DEFAULT_SOURCE_DIRECTORY/multiple-sources/sources-2"
                ] as Resource]
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            Exception e = thrown(IllegalStateException)
            if (File.separatorChar != '/') {
                nonExistingPath = nonExistingPath.replaceAll('/', '\\\\')
            }
            e.message.contains(nonExistingPath)

            // folders are rendered in order, so the first one is done
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            files.size() == 1
            files*.name == ['sample-1.html']
    }

    def "more than one source folder rendered to a single directory"() {
        setup:
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/multi-sources/${System.currentTimeMillis()}")
            String relativeTestsPath = 'src/test/resources/src/asciidoctor/relative-path-treatment'

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/multiple-sources/sources-1"
                ] as Resource, [
                    directory: "$DEFAULT_SOURCE_DIRECTORY/multiple-sources/sources-2"
                ] as Resource, [
                    directory: relativeTestsPath
                ] as Resource]
            mojo.resources = [[
                    directory: relativeTestsPath
                ] as Resource]
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            assertEqualsStructure(new File(relativeTestsPath).listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            // includes 7 rendered AsciiDoc documents and 1 resource
            files.size() == 8
            files*.name.findAll({it.endsWith('html')}).containsAll(['HelloWorld.html',
                                                                    'HelloWorld2.html', 'HelloWorld22.html',
                                                                    'HelloWorld3.html', 'HelloWorld4.html',
                                                                    'sample-1.html', 'sample-2.html'])
    }

    def "more than one source folder keeping the original directory structure"() {
        setup:
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/multi-sources/${System.currentTimeMillis()}")
            String relativeTestsPath = 'src/test/resources/src/asciidoctor/relative-path-treatment'

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/multiple-sources/sources-1"
                ] as Resource, [
                    directory: "$DEFAULT_SOURCE_DIRECTORY/multiple-sources/sources-2"
                ] as Resource, [
                    directory: relativeTestsPath
            ] as Resource]
            mojo.resources = [[
                    directory: relativeTestsPath
                ] as Resource]
            mojo.preserveDirertories = true
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            assertEqualsStructure(new File(relativeTestsPath).listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            // includes 3 rendered AsciiDoc documents and 1 resource
            files.size() == 4
            files*.name.containsAll(['HelloWorld.groovy', 'HelloWorld.html', 'sample-1.html', 'sample-2.html'])
    }

    def "more than one resource is copied with filters"() {
        setup:
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/multi-sources/${System.currentTimeMillis()}")
            String relativeTestsPath = 'src/test/resources/src/asciidoctor/relative-path-treatment'

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/issue-78"
                ] as Resource]
            mojo.resources = [[
                    directory: "$DEFAULT_SOURCE_DIRECTORY/issue-78",
                    includes: ['**/*.adoc']
                ] as Resource, [
                    directory: relativeTestsPath,
                    excludes :['**/*.jpg']
                ] as Resource]
            mojo.preserveDirertories = true
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            // includes 2 rendered AsciiDoc documents and 3 resources
            files.size() == 5
            // from 'issue-78' directory
            // resource files obtained using the include
            files*.name.findAll({it.endsWith('html')}).containsAll(['main.html', 'image-test.html'])
            // 'images' folder is not copied because it's not included
            files*.name.findAll({it == 'images'}) ==  []
            // from 'relative-path-treatment' directory
            // all folders and files are created because only image files are excluded
            assertEqualsStructure(new File(relativeTestsPath).listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
            // images are excluded but not the rest of files
            FileUtils.listFiles(outputDir, ['groovy'] as String[], true).size == 5
            FileUtils.listFiles(outputDir, ["jpg"] as String[], true).size() == 0
    }

    def "render GitHub README alone"() {
        setup:
            File outputDir = new File("$MULTIPLE_SOURCES_OUTPUT/readme/${System.currentTimeMillis()}")

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: ".",
                    includes: ['*.adoc']
                ] as Resource]
            mojo.resources = [[
                    directory: ".",
                    excludes: ['**/**']
                ] as Resource]
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            // includes only 1 rendered AsciiDoc document
            files.size() == 1
            files.first().text.contains('Asciidoctor Maven Plugin')
    }

    def "files in ignored "() {
        setup:
            File outputDir = new File("target/asciidoctor-output/hidden/${System.currentTimeMillis()}")
            String relativeTestsPath = "$DEFAULT_SOURCE_DIRECTORY/relative-path-treatment"

            if (!outputDir.exists())
                outputDir.mkdir()
        when:
            AsciidoctorMojo mojo = new AsciidoctorMojo()
            mockPlexusContainer.initializeContext(mojo)

            mojo.sources = [[
                    directory: relativeTestsPath,
                ] as Resource]
            mojo.preserveDirertories = true
            mojo.backend = 'html5'
            mojo.outputDirectory = outputDir
            mojo.execute()
        then:
            assertEqualsStructure(new File(relativeTestsPath).listFiles(DIRECTORY_FILTER), outputDir.listFiles(DIRECTORY_FILTER))
            def files = outputDir.listFiles({File f -> f.isFile()} as FileFilter)
            files.size() == 2
            files*.name.containsAll(['HelloWorld.groovy','HelloWorld.html'])
            FileUtils.listFiles(outputDir, ['html'] as String[], true).findAll({it.name.startsWith("_")}).size == 0
    }

}
