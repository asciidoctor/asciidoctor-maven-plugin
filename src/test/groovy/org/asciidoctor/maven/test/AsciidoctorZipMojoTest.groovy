package org.asciidoctor.maven.test

import org.asciidoctor.maven.AsciidoctorZipMojo
import spock.lang.Specification

import java.util.zip.ZipFile
/**
 *
 */
class AsciidoctorZipMojoTest extends Specification {
    def "zip it"() {
        given: 'an empty output directory'
            def outputDir = new File('target/asciidoctor-zip-output')
            outputDir.deleteDir()
            outputDir.mkdirs()

            def zip = new File('target/asciidoctor-zip.zip')
            zip.delete()

        when: 'zip mojo is called'
            def srcDir = new File('target/test-classes/src/asciidoctor-zip')
            srcDir.mkdirs()

            new File(srcDir, "sample.asciidoc").withWriter {
                it << '''
                Title
                =====
                test
                '''.stripIndent()
            }

            def mojo = new AsciidoctorZipMojo()
            mojo.backend = 'html'
            mojo.sourceDirectory = srcDir
            mojo.outputDirectory = outputDir
            mojo.zipDestination = zip
            mojo.zip = true
            mojo.execute()

        then: 'a zip is created'
            mojo.zipDestination.exists()

            def entries = new ZipFile(mojo.zipDestination).entries()
           entries.hasMoreElements()
		   def entryName = entries.nextElement().name
		   entryName == 'asciidoctor-zip/target/asciidoctor-zip-output/sample.html' || entryName == 'asciidoctor-zip/target\\asciidoctor-zip-output\\sample.html'
    }
}
