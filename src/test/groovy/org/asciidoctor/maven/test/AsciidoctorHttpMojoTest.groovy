package org.asciidoctor.maven.test

import org.asciidoctor.maven.AsciidoctorHttpMojo
import org.asciidoctor.maven.AsciidoctorMojo
import org.asciidoctor.maven.test.io.DoubleOuputStream
import org.asciidoctor.maven.test.io.PrefilledInputStream
import org.asciidoctor.maven.test.plexus.MockPlexusContainer
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

class AsciidoctorHttpMojoTest extends Specification {

    def setupSpec() {
        MockPlexusContainer.initializeMockContext(AsciidoctorHttpMojo)
    }

    def "http front should let access rendered files"() {
        setup:
            def srcDir = new File('target/test-classes/src/asciidoctor-http')
            def outputDir = new File('target/asciidoctor-http-output')

            srcDir.mkdirs()

            def inputLatch = new CountDownLatch(1)

            def originalOut = System.out
            def originalIn = System.in

            def newOut = new DoubleOuputStream(originalOut)
            def newIn = new PrefilledInputStream('exit\r\n'.bytes, inputLatch)

            System.setOut(new PrintStream(newOut))
            System.setIn(newIn)

            def httpPort = new AsciidoctorMojoTestHelper().availablePort

            def content = new File(srcDir, "content.asciidoc")
            content.withWriter{ it <<
                '''Document Title
                ==============

                This is test, only a test.'''.stripIndent() }

            def mojo = new AsciidoctorHttpMojo()
            mojo.backend = 'html5'
            mojo.port = httpPort
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.headerFooter = true
            mojo.home = 'index'
            def mojoThread = new Thread(new Runnable() {
                @Override
                void run() {
                    mojo.execute()
                }
            })
            mojoThread.start()

            while (!new String(newOut.toByteArray()).contains('Type ')) {
                Thread.sleep(200)
            }

        when:
            def html = "http://localhost:${httpPort}/content".toURL().text

        then:
            assert html.contains('This is test, only a test')
            assert html.contains('</html>')

        cleanup:
            System.setOut(originalOut)
            inputLatch.countDown()
            System.setIn(originalIn)
    }

    def "default page"() {
        setup:
            def srcDir = new File('target/test-classes/src/asciidoctor-http-default')
            def outputDir = new File('target/asciidoctor-http-default-output')

            srcDir.mkdirs()

            def inputLatch = new CountDownLatch(1)

            def originalOut = System.out
            def originalIn = System.in

            def newOut = new DoubleOuputStream(originalOut)
            def newIn = new PrefilledInputStream('exit\r\n'.bytes, inputLatch)

            def httpPort = new AsciidoctorMojoTestHelper().availablePort

            System.setOut(new PrintStream(newOut))
            System.setIn(newIn)

            def content = new File(srcDir, "content.asciidoc")
            content.withWriter{ it <<
                    '''Document Title
                    ==============

                    DEFAULT.'''.stripIndent() }

            def mojo = new AsciidoctorHttpMojo()
            mojo.backend = 'html5'
            mojo.port = httpPort
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.headerFooter = true
            mojo.home = 'content'
            def mojoThread = new Thread(new Runnable() {
                @Override
                void run() {
                    mojo.execute()
                }
            })
            mojoThread.start()

            while (!new String(newOut.toByteArray()).contains('Type ')) {
                Thread.sleep(200)
            }

        when:
            def html = "http://localhost:${httpPort}/".toURL().text

        then:
            assert html.contains('DEFAULT')

        cleanup:
            System.setOut(originalOut)
            inputLatch.countDown()
            System.setIn(originalIn)
    }
}
