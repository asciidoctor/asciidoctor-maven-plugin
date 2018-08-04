package org.asciidoctor.maven.test

import org.asciidoctor.maven.AsciidoctorRefreshMojo
import org.asciidoctor.maven.test.io.DoubleOuputStream
import org.asciidoctor.maven.test.io.PrefilledInputStream
import org.asciidoctor.maven.test.plexus.MockPlexusContainer
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

class AsciidoctorRefreshMojoTest extends Specification {

    def setupSpec() {
        MockPlexusContainer.initializeMockContext(AsciidoctorRefreshMojo)
    }

    def "auto render when source updated"() {
        setup:
            def srcDir = new File('target/test-classes/src/asciidoctor-refresh')
            def outputDir = new File('target/asciidoctor-refresh-output')

            srcDir.mkdirs()

            def inputLatch = new CountDownLatch(1)

            def originalOut = System.out
            def originalIn = System.in

            def newOut = new DoubleOuputStream(originalOut)
            def newIn = new PrefilledInputStream('exit\r\n'.bytes, inputLatch)

            System.setOut(new PrintStream(newOut))
            System.setIn(newIn)

            def content = new File(srcDir, 'content' + new Random(System.currentTimeMillis()).nextInt(1000) + '.asciidoc')

            if (content.exists())
                content.delete()

            content.withWriter{ it <<
                '''= Document Title

                This is test, only a test.'''.stripIndent() }

            def target = new File(outputDir, content.name.replace('.asciidoc', '.html'))

            def mojo = new AsciidoctorRefreshMojo()
            mojo.backend = 'html'
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            def mojoThread = new Thread(new Runnable() {
                @Override
                void run() {
                    mojo.execute()
                    println 'end'
                }
            })
            mojoThread.start()

            while (!new String(newOut.toByteArray()).contains('Rendered')) {
                Thread.sleep(200)
            }

            assert target.text.contains('This is test, only a test')

        when:
            content.withWriter{ it <<
                '''= Document Title

                Wow, this will be auto refreshed!'''.stripIndent() }

        then:
            while (!new String(newOut.toByteArray()).contains('Re-rendered ')) {
                Thread.sleep 500
            }
            assert target.text.contains('Wow, this will be auto refreshed')

        cleanup:
            System.setOut(originalOut)
            inputLatch.countDown()
            System.setIn(originalIn)

    }
}
