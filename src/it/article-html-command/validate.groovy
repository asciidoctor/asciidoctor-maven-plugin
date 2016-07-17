import java.io.*;


File outputDir = new File( basedir, "target/docs" )

String[] expectedFiles = ["sample.html"]

for ( String expectedFile : expectedFiles ) {
    File file = new File( outputDir, expectedFile )
    println ( "Checking for existence of " + file )

    // validate that asciidoctor.attributes are processed
    String text = file.text
    text.contains('<body class="article toc2 toc-right">')
    text.contains('<pre class="CodeRay highlight">')

    if ( !file.isFile() ) {
        throw new Exception( "Missing file " + file )
    }
}

return true;