package org.asciidoctor.maven.site;

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
import org.asciidoctor.maven.commons.StringUtils;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.log.LogRecordsProcessors;
import org.asciidoctor.maven.log.MemoryLogHandler;
import org.asciidoctor.maven.site.SiteConverterDecorator.Result;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by <a href="https://maven.apache.org/doxia/overview.html">the Doxia framework</a>
 * to handle the actual parsing of the AsciiDoc input files into HTML to be consumed/wrapped
 * by the Maven site generation process
 * (see <a href="https://maven.apache.org/plugins/maven-site-plugin/">maven-site-plugin</a>).
 *
 * @author jdlee
 * @author mojavelinux
 */
@Component(role = Parser.class, hint = AsciidoctorConverterDoxiaParser.ROLE_HINT)
public class AsciidoctorConverterDoxiaParser extends AbstractTextParser {

    /**
     * The role hint for the {@link AsciidoctorConverterDoxiaParser} Plexus component.
     */
    static final String ROLE_HINT = "asciidoc";

    private static final Logger logger = LoggerFactory.getLogger(AsciidoctorConverterDoxiaParser.class);

    @Inject
    private MavenProject mavenProject;
    @Inject
    private SiteConversionConfigurationParser siteConfigParser;
    @Inject
    private LogHandlerFactory logHandlerFactory;
    @Inject
    private SiteConverterDecorator siteConverter;

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

        final LogHandler logHandler = logHandlerFactory.getConfiguration(siteConfig);
        final MemoryLogHandler memoryLogHandler = logHandlerFactory.create(asciidoctor, siteDirectory, logger);

        final Result headerMetadata = siteConverter.process(asciidoctor, source, conversionConfig.getOptions());

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
            .parse(headerMetadata.getHeaderMetadata());

        sink.rawText(headerMetadata.getHtml());
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
