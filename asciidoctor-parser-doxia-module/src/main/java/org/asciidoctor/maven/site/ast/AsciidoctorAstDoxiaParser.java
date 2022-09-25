package org.asciidoctor.maven.site.ast;

import org.apache.maven.doxia.parser.AbstractTextParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.*;
import org.asciidoctor.ast.Document;
import org.asciidoctor.maven.commons.StringUtils;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.log.LogRecordFormatter;
import org.asciidoctor.maven.log.MemoryLogHandler;
import org.asciidoctor.maven.site.SiteConversionConfiguration;
import org.asciidoctor.maven.site.SiteConversionConfigurationParser;
import org.asciidoctor.maven.site.SiteLogHandlerDeserializer;
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
@Component(role = Parser.class, hint = AsciidoctorAstDoxiaParser.ROLE_HINT)
public class AsciidoctorAstDoxiaParser extends AbstractTextParser {

    @Inject
    protected Provider<MavenProject> mavenProjectProvider;

    /**
     * The role hint for the {@link AsciidoctorAstDoxiaParser} Plexus component.
     */
    public static final String ROLE_HINT = "asciidoc";

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

        sink.body();
        Document document = asciidoctor.load(source, conversionConfig.getOptions());
        if (StringUtils.isNotBlank(reference))
            getLog().debug("Document loaded: " + reference);
        new NodesSinker(sink).processNode(document, 0);
        sink.body_();
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
