package org.asciidoctor.maven.test

import org.apache.maven.plugin.MojoExecutionException
import org.asciidoctor.maven.AsciidoctorMojo
import org.asciidoctor.maven.log.LogHandler
import org.asciidoctor.maven.test.plexus.MockPlexusContainer
import spock.lang.Specification

import static org.asciidoctor.log.Severity.ERROR
import static org.asciidoctor.log.Severity.WARN

/**
 *
 */
class AsciidoctorMojoLogHandlerTest extends Specification {

    static final String DEFAULT_SOURCE_DIRECTORY = 'target/test-classes/src/asciidoctor'

    def setupSpec() {
        MockPlexusContainer.initializeMockContext(AsciidoctorMojo)
    }

    def "should not fail when logHandler is not set"() {
        setup:
        String sourceDocument = 'errors/document-with-missing-include.adoc'
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY)
        File outputDir = new File("target/asciidoctor-output/${System.currentTimeMillis()}")

        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.backend = 'html'
        mojo.sourceDirectory = srcDir
        mojo.sourceDocumentName = sourceDocument
        mojo.outputDirectory = outputDir
        mojo.headerFooter = true
        mojo.attributes['toc'] = null
        mojo.execute()

        then: 'process completes but the document contains errors'
        def file = new File(outputDir, 'document-with-missing-include.html')
        file.exists()
        file.text.contains('<p>Unresolved directive in document-with-missing-include.adoc - include::unexistingdoc.adoc[]</p>')
    }

    def "should not fail & log errors as INFO when outputToConsole is set"() {
        setup:
        def originalOut = System.out
        def newOut = new ByteArrayOutputStream()
        System.setOut(new PrintStream(newOut))

        String sourceDocument = 'errors/document-with-missing-include.adoc'
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY)
        File outputDir = new File("target/asciidoctor-output/${System.currentTimeMillis()}")
        def handler = new LogHandler()
        handler.outputToConsole = true

        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.backend = 'html'
        mojo.sourceDirectory = srcDir
        mojo.sourceDocumentName = sourceDocument
        mojo.outputDirectory = outputDir
        mojo.headerFooter = true
        mojo.attributes['toc'] = null
        mojo.logHandler = handler
        mojo.execute()

        then: 'output file exists & shows include error'
        def file = new File(outputDir, 'document-with-missing-include.html')
        file.exists()
        file.text.contains('<p>Unresolved directive in document-with-missing-include.adoc - include::unexistingdoc.adoc[]</p>')

        and: 'all messages (ERR & WARN) are logged as info'
        def consoleOutput = newOut.toString()
        consoleOutput.contains('[info] asciidoctor: ERROR: errors\\document-with-missing-include.adoc: line 3: include file not found:')
        consoleOutput.contains('[info] asciidoctor: ERROR: errors\\document-with-missing-include.adoc: line 5: include file not found:')
        consoleOutput.contains('[info] asciidoctor: ERROR: errors\\document-with-missing-include.adoc: line 9: include file not found:')
        consoleOutput.contains('[info] asciidoctor: WARN: errors\\document-with-missing-include.adoc: line 25: no callout found for <1>')

        cleanup:
        System.setOut(originalOut)
    }

    def "should fail when logHandler failIf = WARNING"() {
        setup:
        String sourceDocument = 'errors/document-with-missing-include.adoc'
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY)
        File outputDir = new File("target/asciidoctor-output/${System.currentTimeMillis()}")
        def handler = new LogHandler()
        handler.failIf = [severity: WARN]

        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.backend = 'html'
        mojo.sourceDirectory = srcDir
        mojo.sourceDocumentName = sourceDocument
        mojo.outputDirectory = outputDir
        mojo.headerFooter = true
        mojo.attributes['toc'] = null
        mojo.logHandler = handler
        mojo.execute()

        then:
        def e = thrown(MojoExecutionException)
        e.message.contains('Found 1 issue(s) of severity WARN during rendering')
    }

    def "should fail when logHandler failIf = ERROR"() {
        setup:
        String sourceDocument = 'errors/document-with-missing-include.adoc'
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY)
        File outputDir = new File("target/asciidoctor-output/${System.currentTimeMillis()}")
        def handler = new LogHandler()
        handler.failIf = [severity: ERROR]

        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.backend = 'html'
        mojo.sourceDirectory = srcDir
        mojo.sourceDocumentName = sourceDocument
        mojo.outputDirectory = outputDir
        mojo.headerFooter = true
        mojo.attributes['toc'] = null
        mojo.logHandler = handler
        mojo.execute()

        then:
        def e = thrown(MojoExecutionException)
        e.message.contains('Found 3 issue(s) of severity ERROR during rendering')
    }

    def "should not fail if containsText does not match any message"() {
        setup:
        String sourceDocument = 'errors/document-with-missing-include.adoc'
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY)
        File outputDir = new File("target/asciidoctor-output/${System.currentTimeMillis()}")
        def handler = new LogHandler()
        handler.failIf = [containsText: 'here is some random text']

        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.backend = 'html'
        mojo.sourceDirectory = srcDir
        mojo.sourceDocumentName = sourceDocument
        mojo.outputDirectory = outputDir
        mojo.headerFooter = true
        mojo.attributes['toc'] = null
        mojo.logHandler = handler
        mojo.execute()

        then:
        def file = new File(outputDir, 'document-with-missing-include.html')
        file.exists()
        file.text.contains('<p>Unresolved directive in document-with-missing-include.adoc - include::unexistingdoc.adoc[]</p>')
    }

    def "should fail when containsText matches"() {
        setup:
        def originalOut = System.out
        def newOut = new ByteArrayOutputStream()
        System.setErr(new PrintStream(newOut))

        String sourceDocument = 'errors/document-with-missing-include.adoc'
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY)
        File outputDir = new File("target/asciidoctor-output/${System.currentTimeMillis()}")
        def handler = new LogHandler()
        handler.failIf = [containsText: 'include file not found']

        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.backend = 'html'
        mojo.sourceDirectory = srcDir
        mojo.sourceDocumentName = sourceDocument
        mojo.outputDirectory = outputDir
        mojo.headerFooter = true
        mojo.attributes['toc'] = null
        mojo.logHandler = handler
        mojo.execute()

        then:
        new File(outputDir, 'document-with-missing-include.html').exists()
        def e = thrown(MojoExecutionException)
        e.message.contains("Found 3 issue(s) containing 'include file not found'")

        and: 'all messages (ERR & WARN) are logged as error'
        def consoleError = newOut.toString()

        consoleError.contains('[error] asciidoctor: ERROR: errors\\document-with-missing-include.adoc: line 3: include file not found:')
        consoleError.contains('[error] asciidoctor: ERROR: errors\\document-with-missing-include.adoc: line 5: include file not found:')
        consoleError.contains('[error] asciidoctor: ERROR: errors\\document-with-missing-include.adoc: line 9: include file not found:')

        cleanup:
        System.setErr(originalOut)
    }

    def "should fail and filter errors that match both severity and text"() {
        setup:
        def originalOut = System.out
        def newOut = new ByteArrayOutputStream()
        System.setErr(new PrintStream(newOut))

        String sourceDocument = 'errors/document-with-missing-include.adoc'
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY)
        File outputDir = new File("target/asciidoctor-output/${System.currentTimeMillis()}")
        def handler = new LogHandler()
        handler.failIf = [
                severity       : WARN,
                containsText: 'no'
        ]

        when:
        AsciidoctorMojo mojo = new AsciidoctorMojo()
        mojo.backend = 'html'
        mojo.sourceDirectory = srcDir
        mojo.sourceDocumentName = sourceDocument
        mojo.outputDirectory = outputDir
        mojo.headerFooter = true
        mojo.attributes['toc'] = null
        mojo.logHandler = handler
        mojo.execute()

        then:
        new File(outputDir, 'document-with-missing-include.html').exists()
        def e = thrown(MojoExecutionException)
        e.message.contains("Found 1 issue(s) matching severity WARN and text 'no'")

        and:
        def consoleError = newOut.toString()
        consoleError.contains('[error] asciidoctor: WARN: errors\\document-with-missing-include.adoc: line 25: no callout found for <1>')

        cleanup:
        System.setErr(originalOut)
    }

}
