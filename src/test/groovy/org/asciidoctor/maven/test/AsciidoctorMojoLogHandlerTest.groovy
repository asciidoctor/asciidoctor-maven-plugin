package org.asciidoctor.maven.test

import org.apache.maven.plugin.MojoExecutionException
import org.asciidoctor.maven.AsciidoctorMojo
import org.asciidoctor.maven.log.LogHandler
import org.asciidoctor.maven.test.plexus.MockPlexusContainer
import spock.lang.Ignore
import spock.lang.Specification

import static org.asciidoctor.log.Severity.ERROR
import static org.asciidoctor.log.Severity.WARN

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

    def "should show Asciidoctor messages as info by default"() {
        setup:
        def originalOut = System.out
        def newOut = new ByteArrayOutputStream()
        System.setOut(new PrintStream(newOut))
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

        then:
        def messageLies = newOut.toString()
                .split('\n')
                .findAll { it.contains('asciidoctor:') }
        messageLies.size() == 4

        cleanup:
        System.setOut(originalOut)
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
        def consoleOutput = newOut.toString().readLines().findAll { it.startsWith('[info] asciidoctor') }
        consoleOutput[0].startsWith(fixOSseparator('[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 3: include file not found:'))
        consoleOutput[1].startsWith(fixOSseparator('[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 5: include file not found:'))
        consoleOutput[2].startsWith(fixOSseparator('[info] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 9: include file not found:'))
        consoleOutput[3].startsWith(fixOSseparator('[info] asciidoctor: WARN: errors/document-with-missing-include.adoc: line 25: no callout found for <1>'))

        cleanup:
        System.setOut(originalOut)
    }

    @Ignore
    def "should not fail & log errors as INFO when outputToConsole is set and doc contains messages without cursor and verbose is enabled"() {
        setup:
        def originalOut = System.out
        def newOut = new ByteArrayOutputStream()
        System.setOut(new PrintStream(newOut))

        String sourceDocument = 'errors/document-with-invalid-reference.adoc'
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
        mojo.enableVerbose = true
        mojo.logHandler = handler
        mojo.execute()

        then: 'output file exists & shows include error'
        def file = new File(outputDir, 'document-with-invalid-reference.html')
        file.exists()

        and: 'all messages (WARN) are logged as info'
        def consoleOutput = newOut.toString().readLines().findAll { it.startsWith('[info] asciidoctor') }
        consoleOutput.size() == 2
        consoleOutput[0].startsWith('[info] asciidoctor: WARN: invalid reference: ../path/some-file.adoc')
        consoleOutput[1].startsWith('[info] asciidoctor: WARN: invalid reference: section-id')

        cleanup:
        System.setOut(originalOut)
    }

    @Ignore
    def "should not fail & log verbose errors when gempath is set"() {
        setup:
        def originalOut = System.out
        def newOut = new ByteArrayOutputStream()
        System.setOut(new PrintStream(newOut))

        String sourceDocument = 'errors/document-with-invalid-reference.adoc'
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
        mojo.enableVerbose = true
        mojo.gemPath = System.getProperty("java.io.tmpdir")
        mojo.logHandler = handler
        mojo.execute()

        then: 'output file exists & shows include error'
        def file = new File(outputDir, 'document-with-invalid-reference.html')
        file.exists()

        and: 'all messages (WARN) are logged as info'
        def consoleOutput = newOut.toString().readLines().findAll { it.startsWith('[info] asciidoctor') }
        consoleOutput.size() == 2
        consoleOutput[0].startsWith('[info] asciidoctor: WARN: invalid reference: ../path/some-file.adoc')
        consoleOutput[1].startsWith('[info] asciidoctor: WARN: invalid reference: section-id')

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

        then: 'issues with WARN and ERROR are returned'
        def e = thrown(MojoExecutionException)
        e.message.contains('Found 4 issue(s) of severity WARN or higher during rendering')
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
        e.message.contains('Found 3 issue(s) of severity ERROR or higher during rendering')
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
        def originalOut = System.err
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

        consoleError.contains(fixOSseparator('[error] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 3: include file not found:'))
        consoleError.contains(fixOSseparator('[error] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 5: include file not found:'))
        consoleError.contains(fixOSseparator('[error] asciidoctor: ERROR: errors/document-with-missing-include.adoc: line 9: include file not found:'))

        cleanup:
        System.setErr(originalOut)
    }

    def "should fail and filter errors that match both severity and text"() {
        setup:
        def originalOut = System.err
        def newOut = new ByteArrayOutputStream()
        System.setErr(new PrintStream(newOut))

        String sourceDocument = 'errors/document-with-missing-include.adoc'
        File srcDir = new File(DEFAULT_SOURCE_DIRECTORY)
        File outputDir = new File("target/asciidoctor-output/${System.currentTimeMillis()}")
        def handler = new LogHandler()
        handler.failIf = [
                severity    : WARN,
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
        e.message.contains("Found 4 issue(s) matching severity WARN or higher and text 'no'")

        and:
        def consoleError = newOut.toString()
        consoleError.contains(fixOSseparator('[error] asciidoctor: WARN: errors/document-with-missing-include.adoc: line 25: no callout found for <1>'))

        cleanup:
        System.setErr(originalOut)
    }

    // `asciidoctor` JUL logger inherits a ConsoleHandler that needs to be disabled
    // to avoid redundant messages in error channel
    def "should not print default AsciidoctorJ messages"() {
        setup:
        def originalOut = System.err
        def newOut = new ByteArrayOutputStream()
        System.setErr(new PrintStream(newOut))

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

        and: 'no undesired messages appear in err channel'
        newOut.toString().size() == 0

        cleanup:
        System.setErr(originalOut)
    }

    private String fixOSseparator(String text) {
        File.separatorChar == '\\' ? text.replaceAll("/", "\\\\") : text
    }

}
