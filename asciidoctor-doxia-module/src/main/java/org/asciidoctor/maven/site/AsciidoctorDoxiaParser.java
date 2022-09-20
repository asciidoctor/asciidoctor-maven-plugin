package org.asciidoctor.maven.site;

import ast.NodesSinker;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.maven.doxia.module.markdown.MarkdownParser;
import org.apache.maven.doxia.module.xhtml.XhtmlParser;
import org.apache.maven.doxia.parser.AbstractTextParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.*;
import org.asciidoctor.ast.Document;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.log.LogRecordFormatter;
import org.asciidoctor.maven.log.LogRecordsProcessors;
import org.asciidoctor.maven.log.MemoryLogHandler;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

/**
 * This class is used by <a href="https://maven.apache.org/doxia/overview.html">the Doxia framework</a>
 * to handle the actual parsing of the AsciiDoc input files into HTML to be consumed/wrapped
 * by the Maven site generation process
 * (see <a href="https://maven.apache.org/plugins/maven-site-plugin/">maven-site-plugin</a>).
 *
 * @author jdlee
 * @author mojavelinux
 */
@Component(role = Parser.class, hint = AsciidoctorDoxiaParser.ROLE_HINT)
public class AsciidoctorDoxiaParser extends AbstractTextParser {

    @Inject
    protected Provider<MavenProject> mavenProjectProvider;

    /**
     * The role hint for the {@link AsciidoctorDoxiaParser} Plexus component.
     */
    public static final String ROLE_HINT = "asciidoc";

    @Inject
    private XhtmlParser xhtmlParser;
    @Inject
    private MarkdownParser.MarkdownHtmlParser markdownHtmlParser;


    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(Reader reader, Sink sink, String reference) throws ParseException {
        String source;
        try {
            if ((source = IOUtil.toString(reader)) == null) {
                source = "";
            }
        } catch (IOException ex) {
            getLog().error("Could not read AsciiDoc source: " + ex.getLocalizedMessage());
            return;
        }

        final MavenProject project = mavenProjectProvider.get();
        final Xpp3Dom siteConfig = getSiteConfig(project);
        final File siteDirectory = resolveSiteDirectory(project, siteConfig);

        // Doxia handles a single instance of this class and invokes it multiple times.
        // We need to ensure certain elements are initialized only once to avoid errors.
        // Note, this cannot be done in the constructor because mavenProjectProvider in set after construction.
        // And overriding init and other methods form parent classes does not work.
        final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        SiteConversionConfiguration conversionConfig = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, defaultOptions(siteDirectory), defaultAttributes());
        for (String require : conversionConfig.getRequires()) {
            requireLibrary(asciidoctor, require);
        }

        final LogHandler logHandler = getLogHandlerConfig(siteConfig);
        final MemoryLogHandler memoryLogHandler = asciidoctorLoggingSetup(asciidoctor, logHandler, siteDirectory);

        // QUESTION should we keep OptionsBuilder & AttributesBuilder separate for call to convertAsciiDoc?
        String asciidocHtml = convertAsciiDoc(asciidoctor, source, conversionConfig.getOptions());
        try {
            // process log messages according to mojo configuration
            new LogRecordsProcessors(logHandler, siteDirectory, errorMessage -> getLog().error(errorMessage))
                    .processLogRecords(memoryLogHandler);
        } catch (Exception exception) {
            throw new ParseException(exception.getMessage(), exception);
        }

        sink.body();

        Document document = asciidoctor.load(source, conversionConfig.getOptions());
        new NodesSinker(sink)
                .processNode(document, 0);
        customSeparator(sink);

        sink.rawText("<h1>Manual test</h1>");

        // Does nothing...no content added
        // head(sink, "Head");
        // <header>
        // header(sink, "Header");
        // No HTML, just the text
        // title(sink, "Title");

        // <h{level+1} id="{text}">
        // for level 0 or 6, No HTML, just the text
        // Asciidoctor has default 5 if more is set, we can resort to sectionTitle5
        // TODO: how to obtain H1?
        sectionTitle(sink, 0, "Section Title");
        sectionTitle(sink, 1, "Section Title");
        sectionTitle(sink, 2, "Section Title");

        // on unknown do paragraph
        paragraph(sink, "A paragraph line.", "Another paragraph line.");
        bold(sink, "bold");
        italics(sink, "italics");
        inline(sink, "inline");
        monospaced(sink, "monospaced");

        unorderedList(sink);
        numberedList(sink, 0);
        definitionList(sink);

        sink.figure();
        sink.text("figure");
        sink.figureGraphics("images/tiger.png");
        sink.figureCaption();
        sink.text("figure");
        sink.figureCaption_();
        sink.figure_();

        sections(sink);

        sink.division();
        sink.horizontalRule();
        sink.pageBreak();

        // <time datetime="{time}">{text}
        time(sink, "2008-02-14 20:00", "Valentines day");
        // No html element
        table(sink);

        sink.blockquote();
        sink.paragraph();
        sink.text("Hello quote");
        sink.paragraph_();
        sink.blockquote_();

        // code blocks
        // TODO extract language (check when language has a default set)
        sink.rawText("<div class=\"source\">" +
                "<pre class=\"prettyprint\">" +
                "<code class=\"language-ruby\">" +
                "def sum_eq_n?(arr, n)\n" +
                "  return true if arr.empty? && n == 0\n" +
                "  arr.product(arr).reject { |a,b| a == b }.any? { |a,b| a + b == n }\n" +
                "end" +
                "</span>" +
                "</code>" +
                "</pre>"
                + "</div>");

        sink.rawText("<div class=\"source\">" +
                "<pre class=\"prettyprint\">" +
                "<code class=\"language-java\">" +
                "class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\"); \n" +
                "    }\n" +
                "}" +
                "</span>" +
                "</code>" +
                "</pre>"
                + "</div>");


        // Both parsers are the same "MarkDown extends Xhtml"
        // sink.rawText(asciidocHtml);
        customSeparator(sink);
        asciidocHtml = asciidocHtml.replaceAll("class=\".+?\"", "");
        asciidocHtml = "<div>" + asciidocHtml + "</div>";
        xhtmlParser.parse(new CharSequenceReader(asciidocHtml), sink, reference);
//        markdownHtmlParser.parse(new CharSequenceReader(asciidocHtml), sink, reference);

//        sink.rawText(asciidocHtml);
        /***/
        sink.body_();
    }

    private static void customSeparator(Sink sink) {
        sink.rawText("<div>==========================================================</div>");
    }

    private void inline(Sink sink, String text) {
        sink.inline();
        sink.text(text);
        sink.inline();
    }

    private void bold(Sink sink, String text) {
        sink.bold();
        sink.text(text);
        sink.bold_();
    }

    private void italics(Sink sink, String text) {
        sink.italic();
        sink.text(text);
        sink.italic_();
    }

    private void monospaced(Sink sink, String text) {
        sink.monospaced();
        sink.text(text);
        sink.monospaced_();
    }

    private static void sections(Sink sink) {
        sink.section1();
        sink.text("Section 1");
        sink.section2();
        sink.text("Section 2");
        sink.section3();
        sink.text("Section 3");
        sink.section4();
        sink.text("Section 4");
        sink.section5();
        sink.text("Section 5");
        sink.section6();
        sink.text("Section 6");
        sink.section6_();
        sink.section5_();
        sink.section4_();
        sink.section3_();
        sink.section2_();
        sink.section1_();
    }

    private static void paragraph(Sink sink, String... lines) {
        sink.paragraph();
        for (String line : lines) {
            sink.text(line);
            sink.lineBreak();
            sink.text(line);
            sink.lineBreakOpportunity();
            sink.nonBreakingSpace();
        }
        sink.paragraph_();
    }

    private static void time(Sink sink, String time, String text) {
        sink.time(time);
        sink.text(text);
        sink.time_();
    }

    private static void table(Sink sink) {
        sink.table();

        sink.tableRows(new int[]{0}, true);

        sink.tableRow();
        sink.tableHeaderCell();
        sink.text("header 1");
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text("header 2");
        sink.tableHeaderCell_();
        sink.tableRow_();

        sink.tableRow();
        sink.tableCell();
        sink.text("cell 1,1");
        sink.tableCell_();
        sink.tableCell();
        sink.text("cell 1,2");
        sink.tableCell_();
        sink.tableRow_();

        sink.tableRow();
        sink.tableCell();
        sink.text("cell 2,1");
        sink.tableCell_();
        sink.tableCell();
        sink.text("cell 2,2");
        sink.tableCell_();
        sink.tableRow_();

        sink.tableRow();
        sink.tableCell();
        sink.text("cell 3,1");
        sink.tableCell_();
        sink.tableCell();
        sink.text("cell 3,2");
        sink.tableCell_();
        sink.tableRow_();

        sink.tableRows_();

        sink.tableCaption();
        sink.text("Table caption");
        sink.tableCaption_();

        sink.table_();
    }

    private static void sectionTitle(Sink sink, int level, String sectionTitle) {
        String text = sectionTitle + " " + level;
        switch (level) {
            case 0:
                sink.sectionTitle();
                sink.text(text);
                sink.sectionTitle_();
                break;
            case 1:
                sink.sectionTitle1();
                sink.text(text);
                sink.sectionTitle1_();
                break;
            case 2:
                sink.sectionTitle2();
                sink.text(text);
                sink.sectionTitle2_();
                break;
            case 3:
                sink.sectionTitle3();
                sink.text(text);
                sink.sectionTitle3_();
                break;
            case 4:
                sink.sectionTitle4();
                sink.text(text);
                sink.sectionTitle4_();
                break;
            case 5:
                sink.sectionTitle5();
                sink.text(text);
                sink.sectionTitle5_();
                break;
            case 6:
                sink.sectionTitle6();
                sink.text(text);
                sink.sectionTitle6_();
                break;
        }
    }

    private static void title(Sink sink, String title) {
        sink.title();
        sink.text(title);
        sink.title_();
    }

    private static void head(Sink sink, String text) {
        sink.head();
        sink.text(text);
        sink.head_();
    }

    private static void header(Sink sink, String text) {
        sink.header();
        sink.text(text);
        sink.header_();
    }

    /**
     * @param numberingStyle 0: 1. 2.
     *                       1: a. b.
     *                       2: A. B.
     *                       3: i. ii.
     *                       >: 1. 2.
     */
    private static void numberedList(Sink sink, int numberingStyle) {
        sink.numberedList(numberingStyle);
        sink.numberedListItem();
        sink.text("First");
        sink.numberedListItem_();
        sink.numberedListItem();
        sink.text("Second");
        sink.numberedListItem_();
        sink.numberedList_();
    }

    private static void unorderedList(Sink sink) {
        sink.list();
        sink.listItem();
        sink.text("item 1");
        sink.listItem_();
        sink.listItem();
        sink.text("item 2");
        sink.listItem_();
        sink.listItem();
        sink.text("item 3");
        sink.listItem_();
        sink.list_();
    }

    private static void definitionList(Sink sink) {
        sink.definitionList();

        sink.definedTerm();
        sink.text("definedTerm");
        sink.definedTerm_();
        sink.definition();
        sink.text("definition");
        sink.definition_();

        sink.definitionListItem();
        sink.text("Def item 1");
        sink.definitionListItem_();

        sink.definitionListItem();
        sink.text("Def item 2");
        sink.definitionListItem_();
        sink.definitionListItem();
        sink.text("Def item 3");
        sink.definitionListItem_();
        sink.definitionList_();
    }


    private MemoryLogHandler asciidoctorLoggingSetup(Asciidoctor asciidoctor, LogHandler logHandler, File siteDirectory) {

        final MemoryLogHandler memoryLogHandler = new MemoryLogHandler(logHandler.getOutputToConsole(), siteDirectory,
                logRecord -> getLog().info(LogRecordFormatter.format(logRecord, siteDirectory)));
        asciidoctor.registerLogHandler(memoryLogHandler);
        // disable default console output of AsciidoctorJ
        Logger.getLogger("asciidoctor").setUseParentHandlers(false);
        return memoryLogHandler;
    }

    private LogHandler getLogHandlerConfig(Xpp3Dom siteConfig) {
        Xpp3Dom asciidoc = siteConfig == null ? null : siteConfig.getChild("asciidoc");
        return new SiteLogHandlerDeserializer().deserialize(asciidoc);
    }

    protected Xpp3Dom getSiteConfig(MavenProject project) {
        return project.getGoalConfiguration("org.apache.maven.plugins", "maven-site-plugin", "site", "site");
    }

    protected File resolveSiteDirectory(MavenProject project, Xpp3Dom siteConfig) {
        File siteDirectory = new File(project.getBasedir(), "src/site");
        if (siteConfig != null) {
            Xpp3Dom siteDirectoryNode = siteConfig.getChild("siteDirectory");
            if (siteDirectoryNode != null) {
                siteDirectory = new File(siteDirectoryNode.getValue());
            }
        }
        return siteDirectory;
    }

    protected OptionsBuilder defaultOptions(File siteDirectory) {
        return OptionsBuilder.options()
                .backend("xhtml")
                .safe(SafeMode.UNSAFE)
                .baseDir(new File(siteDirectory, ROLE_HINT));
    }

    protected AttributesBuilder defaultAttributes() {
        return AttributesBuilder.attributes()
                .attribute("idprefix", "@")
                .attribute("showtitle", "@");
    }

    private void requireLibrary(Asciidoctor asciidoctor, String require) {
        if (!(require = require.trim()).isEmpty()) {
            try {
                asciidoctor.requireLibrary(require);
            } catch (Exception ex) {
                getLog().error(ex.getLocalizedMessage());
            }
        }
    }

    protected String convertAsciiDoc(Asciidoctor asciidoctor, String source, Options options) {
        return asciidoctor.convert(source, options);
    }

}
