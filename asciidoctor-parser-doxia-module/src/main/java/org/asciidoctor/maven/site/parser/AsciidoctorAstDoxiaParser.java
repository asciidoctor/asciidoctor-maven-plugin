package org.asciidoctor.maven.site.parser;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.maven.doxia.parser.AbstractTextParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.maven.commons.StringUtils;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.log.LogRecordsProcessors;
import org.asciidoctor.maven.log.MemoryLogHandler;
import org.asciidoctor.maven.site.HeadParser;
import org.asciidoctor.maven.site.HeaderMetadata;
import org.asciidoctor.maven.site.LogHandlerFactory;
import org.asciidoctor.maven.site.SiteConversionConfiguration;
import org.asciidoctor.maven.site.SiteConversionConfigurationParser;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;


/**
 * This class is used by <a href="https://maven.apache.org/doxia/overview.html">the Doxia framework</a>
 * to handle the actual parsing of the AsciiDoc input files and pass the AST to converters (NodeProcessors).
 * (see <a href="https://maven.apache.org/plugins/maven-site-plugin/">maven-site-plugin</a>).
 *
 * @author abelsromero
 * @since 3.0.0
 */
@Component(role = Parser.class, hint = AsciidoctorAstDoxiaParser.ROLE_HINT)
public class AsciidoctorAstDoxiaParser extends AbstractTextParser {

    /**
     * The role hint for the {@link AsciidoctorAstDoxiaParser} Plexus component.
     */
    public static final String ROLE_HINT = "asciidoc";

    private static final Logger logger = LoggerFactory.getLogger(AsciidoctorAstDoxiaParser.class);

    private final MavenProject mavenProject;
    private final SiteConversionConfigurationParser siteConfigParser;
    private final LogHandlerFactory logHandlerFactory;

    @Inject
    public AsciidoctorAstDoxiaParser(MavenProject mavenProject,
                                     SiteConversionConfigurationParser siteConfigParser,
                                     LogHandlerFactory logHandlerFactory) {
        this.mavenProject = mavenProject;
        this.siteConfigParser = siteConfigParser;
        this.logHandlerFactory = logHandlerFactory;
    }

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
            logger.error("Could not read AsciiDoc source: {}", ex.getLocalizedMessage());
            return;
        }

        final SiteConversionConfiguration conversionConfig = siteConfigParser.processAsciiDocConfig(mavenProject, ROLE_HINT);
        final Xpp3Dom siteConfig = conversionConfig.getSiteConfig();
        final File siteDirectory = conversionConfig.getSiteBaseDir();

        // Doxia handles a single instance of this class and invokes it multiple times.
        // We need to ensure certain elements are initialized only once to avoid errors.
        // Note, this cannot be done in the constructor because mavenProjectProvider in set after construction.
        // And overriding init and other methods form parent classes does not work.
        final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        for (String require : conversionConfig.getRequires()) {
            requireLibrary(asciidoctor, require);
        }

        if (isNotBlank(reference))
            logger.debug("Document loaded: {}", reference);

        final LogHandler logHandler = logHandlerFactory.getConfiguration(siteConfig);
        final MemoryLogHandler memoryLogHandler = logHandlerFactory.create(asciidoctor, siteDirectory, logger);

        final Document document = asciidoctor.load(source, conversionConfig.getOptions());

        try {
            // process log messages according to mojo configuration
            if (!memoryLogHandler.isEmpty()) {
                logger.info("Issues found in: {}", reference);
                if (logHandler.getOutputToConsole() && StringUtils.isNotBlank(reference)) {
                    memoryLogHandler.processAll();
                }
                new LogRecordsProcessors(logHandler, siteDirectory, logger::error)
                    .processLogRecords(memoryLogHandler);
            }
        } catch (Exception exception) {
            throw new ParseException(exception.getMessage(), exception);
        }

        new HeadParser(sink)
            .parse(HeaderMetadata.from(document));

        new NodeSinker(sink)
            .sink(document);
    }

    private void requireLibrary(Asciidoctor asciidoctor, String require) {
        if (!(require = require.trim()).isEmpty()) {
            try {
                asciidoctor.requireLibrary(require);
            } catch (Exception ex) {
                logger.error(ex.getLocalizedMessage());
            }
        }
    }
}
