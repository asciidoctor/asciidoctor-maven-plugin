package org.asciidoctor.maven.test

import lombok.SneakyThrows
import org.asciidoctor.maven.AsciidoctorHttpMojo
import org.asciidoctor.maven.io.DoubleOutputStream
import org.asciidoctor.maven.io.PrefilledInputStream
import org.asciidoctor.maven.io.TestFilesHelper
import org.asciidoctor.maven.test.plexus.MockPlexusContainer
import spock.lang.Specification

import java.nio.file.Files
import java.util.concurrent.CountDownLatch

class AsciidoctorHttpMojoTest extends Specification {

    def setupSpec() {
        MockPlexusContainer.initializeMockContext(AsciidoctorHttpMojo)
    }

    def "http front should let access converted files"() {
        setup:
            def srcDir = new File('target/test-classes/src/asciidoctor-http')
            def outputDir = TestFilesHelper.newOutputTestDirectory('http-mojo')

            srcDir.mkdirs()

            def inputLatch = new CountDownLatch(1)

            def originalOut = System.out
            def originalIn = System.in

            def newOut = new DoubleOutputStream(originalOut)
            def newIn = new PrefilledInputStream('exit\r\n'.bytes, inputLatch)

            System.setOut(new PrintStream(newOut))
            System.setIn(newIn)

            def httpPort = availablePort

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
            awaitTermination(mojoThread)
    }

    def "should return default page"() {
        setup:
            def srcDir = new File('target/test-classes/src/asciidoctor-http-default')
            def outputDir = TestFilesHelper.newOutputTestDirectory('http-mojo')

            srcDir.mkdirs()

            def inputLatch = new CountDownLatch(1)

            def originalOut = System.out
            def originalIn = System.in

            def newOut = new DoubleOutputStream(originalOut)
            def newIn = new PrefilledInputStream('exit\r\nexit\r\nexit\r\n'.bytes, inputLatch)

            def httpPort = availablePort

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
            awaitTermination(mojoThread)
    }

    def "should return 404 when file does not exist"() {
        setup:
            def emptySrcDir = new File('some_path')
            def outputDir = TestFilesHelper.newOutputTestDirectory('http-mojo')

            def inputLatch = new CountDownLatch(1)

            def originalOut = System.out
            def originalIn = System.in

            def newOut = new DoubleOutputStream(originalOut)
            def newIn = new PrefilledInputStream('exit\r\nexit\r\nexit\r\n'.bytes, inputLatch)

            def httpPort = availablePort

            System.setOut(new PrintStream(newOut))
            System.setIn(newIn)

            def mojo = new AsciidoctorHttpMojo()
            mojo.backend = 'html5'
            mojo.port = httpPort
            mojo.sourceDirectory = emptySrcDir
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
            HttpURLConnection connection = new URL("http://localhost:${httpPort}/").openConnection()
            def status = connection.getResponseCode()

        then:
            status == 404

        cleanup:
            System.setOut(originalOut)
            inputLatch.countDown()
            System.setIn(originalIn)
            awaitTermination(mojoThread)
    }

    def "should return 405 when method is not POST"() {
        setup:
            def emptySrcDir = new File('some_path')
            def outputDir = TestFilesHelper.newOutputTestDirectory('http-mojo')

            def inputLatch = new CountDownLatch(1)

            def originalOut = System.out
            def originalIn = System.in

            def newOut = new DoubleOutputStream(originalOut)
            def newIn = new PrefilledInputStream('exit\r\nexit\r\nexit\r\n'.bytes, inputLatch)

            def httpPort = availablePort

            System.setOut(new PrintStream(newOut))
            System.setIn(newIn)

            def mojo = new AsciidoctorHttpMojo()
            mojo.backend = 'html5'
            mojo.port = httpPort
            mojo.sourceDirectory = emptySrcDir
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
            HttpURLConnection connection = new URL("http://localhost:${httpPort}/").openConnection()
            connection.setRequestMethod("POST")
            def status = connection.getResponseCode()

        then:
            status == 405

        cleanup:
            System.setOut(originalOut)
            inputLatch.countDown()
            System.setIn(originalIn)
            awaitTermination(mojoThread)
    }

    def "should return 205 when method is HEAD and resource exists"() {
        setup:
            def emptySrcDir = new File('some_path')
            def outputDir = TestFilesHelper.newOutputTestDirectory('http-mojo')
            TestFilesHelper.createFileWithContent(outputDir,'index.html')

            def inputLatch = new CountDownLatch(1)

            def originalOut = System.out
            def originalIn = System.in

            def newOut = new DoubleOutputStream(originalOut)
            def newIn = new PrefilledInputStream('exit\r\nexit\r\nexit\r\n'.bytes, inputLatch)

            def httpPort = availablePort

            System.setOut(new PrintStream(newOut))
            System.setIn(newIn)

            def mojo = new AsciidoctorHttpMojo()
            mojo.backend = 'html5'
            mojo.port = httpPort
            mojo.sourceDirectory = emptySrcDir
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
            HttpURLConnection connection = new URL("http://localhost:${httpPort}/").openConnection()
            connection.setRequestMethod("HEAD")
            def status = connection.getResponseCode()

        then:
            status == 205

        cleanup:
            System.setOut(originalOut)
            inputLatch.countDown()
            System.setIn(originalIn)
            awaitTermination(mojoThread)
    }

    private int getAvailablePort() {
        ServerSocket socket = new ServerSocket(0)
        int port = socket.getLocalPort()
        socket.close()
        return port
    }

    @SneakyThrows
    private void awaitTermination(Thread thread) {
        int pollTime = 250;
        int ticks = (10 * 1000 / pollTime);
        while (thread.isAlive()) {
            ticks--;
            if (ticks == 0)
                throw new InterruptedException("Max wait time reached");
            else
                Thread.sleep(pollTime);
        }
    }
}
