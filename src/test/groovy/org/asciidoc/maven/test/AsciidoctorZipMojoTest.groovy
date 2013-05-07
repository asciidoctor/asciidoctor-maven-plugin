package org.asciidoc.maven.test

import org.asciidoc.maven.AsciidoctorMojo
import org.asciidoc.maven.AsciidoctorZipMojo
import spock.lang.Specification

import java.util.zip.ZipFile

/**
 *
 */
class AsciidoctorZipMojoTest extends Specification {
    def "zip it"() {
        given: 'an empty output directory'
            def outputDir = new File('target/asciidoc-zip-output')
            outputDir.deleteDir()
            outputDir.mkdirs()

            def zip = new File('target/asciidoc-zip.zip')
            zip.delete()

        when: 'zip mojo is called'
            def srcDir = new File('target/test-classes/src/asciidoc-zip')
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
            entries.nextElement().name == 'asciidoc-zip/target/asciidoc-zip-output/sample.html'
    }
}
