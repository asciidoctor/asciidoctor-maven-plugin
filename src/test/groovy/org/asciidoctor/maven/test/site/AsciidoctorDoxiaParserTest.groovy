package org.asciidoctor.maven.test.site

import org.apache.maven.doxia.sink.Sink
import org.apache.maven.project.MavenProject
import org.asciidoctor.maven.site.AsciidoctorDoxiaParser
import org.codehaus.plexus.util.xml.Xpp3DomBuilder
import spock.lang.Specification

class AsciidoctorDoxiaParserTest extends Specification {

    public static final String TEST_DOCS_PATH = 'src/test/resources/src/asciidoctor'

    def "should convert html without any configuration"() {
        given:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/sample.asciidoc")
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock()

        when:
        parser.parse(new FileReader(srcAsciidoc), sink)

        then:
        String outputText = sink.sinkedText
        outputText.contains '<h1>Document Title</h1>'
        outputText.contains '<div class="ulist">'
        outputText.contains '<div class="listingblock">'
        outputText.contains "require 'asciidoctor'"
        // icons as text
        outputText.contains '<div class="title">Note</div>'
    }

    def "should convert html with an attribute"() {
        given:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/sample.asciidoc")
        Reader reader = new FileReader(srcAsciidoc)
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock('''
                    <configuration>
                        <asciidoc>
                            <attributes>
                                <icons>font</icons>
                            </attributes>
                        </asciidoc>
                    </configuration>''')

        when:
        parser.parse(reader, sink)

        then:
        String outputText = sink.sinkedText
        // :icons: font
        outputText.contains '<i class="fa icon-note" title="Note"></i>'
    }

    def "should convert html with baseDir option"() {
        given:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/main-document.adoc")
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock("""
                     <configuration>
                        <asciidoc>
                            <baseDir>${new File(srcAsciidoc.parent).absolutePath}</baseDir>
                        </asciidoc>
                     </configuration>""")

        when:
        parser.parse(new FileReader(srcAsciidoc), sink)

        then: 'include works'
        String outputText = sink.sinkedText
        outputText.contains '<h1>Include test</h1>'
        outputText.contains 'println "HelloWorld from Groovy on ${new Date()}"'
    }

    def "should convert html with relative baseDir option"() {
        given:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/main-document.adoc")
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock("""
                     <configuration>
                        <asciidoc>
                            <baseDir>${TEST_DOCS_PATH}</baseDir>
                        </asciidoc>
                     </configuration>""")

        when:
        parser.parse(new FileReader(srcAsciidoc), sink)

        then: 'include works'
        String outputText = sink.sinkedText
        outputText.contains '<h1>Include test</h1>'
        outputText.contains 'println "HelloWorld from Groovy on ${new Date()}"'
    }

    def "should convert html with templateDir option"() {
        given:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/sample.asciidoc")
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock("""
                     <configuration>
                        <asciidoc>
                            <templateDirs>
                                <dir>${TEST_DOCS_PATH}/templates</dir>
                            </templateDirs>
                        </asciidoc>
                     </configuration>""")

        when:
        parser.parse(new FileReader(srcAsciidoc), sink)

        then:
        String outputText = sink.sinkedText
        outputText.contains '<h1>Document Title</h1>'
        outputText.contains '<p class="custom-template ">'
    }

    def "should convert html with attributes and baseDir option"() {
        given:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/main-document.adoc")
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock("""
                    <configuration>
                        <asciidoc>
                            <baseDir>${new File(srcAsciidoc.parent).absolutePath}</baseDir>
                            <attributes>
                                <sectnums></sectnums>
                                <icons>font</icons>
                                <my-label>Hello World!!</my-label>
                            </attributes>
                        </asciidoc>
                    </configuration>""")

        when:
        parser.parse(new FileReader(srcAsciidoc), sink)

        then:
        String outputText = sink.sinkedText
        outputText.contains '<h1>Include test</h1>'
        outputText.contains '<h2 id="code">1. Code</h2>'
        outputText.contains '<h2 id="optional_section">2. Optional section</h2>'
        outputText.contains 'println "HelloWorld from Groovy on ${new Date()}"'
        outputText.contains 'Hello World!!'
        outputText.contains '<i class="fa icon-tip" title="Tip"></i>'
    }

    def "should process empty self-closing XML attributes"() {
        given:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/sample.asciidoc")
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock("""
                     <configuration>
                       <asciidoc>
                         <attributes>
                           <sectnums/>
                         </attributes>
                       </asciidoc>
                     </configuration>""")

        when:
        parser.parse(new FileReader(srcAsciidoc), sink)

        then:
        String outputText = sink.sinkedText
        outputText.contains '<h2 id="id_section_a">1. Section A</h2>'
        outputText.contains '<h3 id="id_section_a_subsection">1.1. Section A Subsection</h3>'
    }

    def "should process empty value XML attributes"() {
        given:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/sample.asciidoc")
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock("""
                     <configuration>
                       <asciidoc>
                         <attributes>
                           <sectnums></sectnums>
                         </attributes>
                       </asciidoc>
                     </configuration>""")

        when:
        parser.parse(new FileReader(srcAsciidoc), sink)

        then:
        String outputText = sink.sinkedText
        outputText.contains '<h2 id="id_section_a">1. Section A</h2>'
        outputText.contains '<h3 id="id_section_a_subsection">1.1. Section A Subsection</h3>'
    }

    def "should fail when logHandler failIf = WARNING"() {
        setup:
        final File srcAsciidoc = new File("$TEST_DOCS_PATH/errors/document-with-missing-include.adoc")
        final Sink sink = createSinkMock()

        AsciidoctorDoxiaParser parser = new AsciidoctorDoxiaParser()
        parser.@mavenProjectProvider = createMavenProjectMock("""
                     <configuration>
                       <asciidoc>
                         <logHandler>
                            <!-- <outputToConsole>false</outputToConsole> -->
                            <failIf>
                                <severity>WARN</severity>
                            </failIf>
                        </logHandler>
                       </asciidoc>
                     </configuration>""")

        when:
        parser.parse(new FileReader(srcAsciidoc), sink)

        then: 'issues with WARN and ERROR are returned'
        def e = thrown(org.apache.maven.doxia.parser.ParseException)
        e.message.contains('Found 4 issue(s) of severity WARN or higher during conversion')
    }

    private javax.inject.Provider<MavenProject> createMavenProjectMock(final String configuration = null) {
        MavenProject mp = [getGoalConfiguration: { pluginGroupId, pluginArtifactId, executionId, goalId ->
            configuration ? Xpp3DomBuilder.build(new StringReader(configuration)) : null
        },
                           getBasedir          : {
                               new File('.')
                           }] as MavenProject

        return [get: { mp }] as javax.inject.Provider<MavenProject>
    }

    /**
     * Creates a {@link Sink} mock that allows retrieving a text previously sinked.
     */
    private Sink createSinkMock() {
        String text
        return [rawText : { t ->
            text = t
        }, getSinkedText: {
            text
        }] as MySink
    }

    interface MySink extends Sink {
        String getSinkedText()
    }

}
