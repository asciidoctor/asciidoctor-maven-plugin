package org.asciidoctor.maven.site;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.maven.doxia.parser.AbstractTextParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.maven.commons.StringUtils;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.log.LogRecordFormatter;
import org.asciidoctor.maven.log.LogRecordsProcessors;
import org.asciidoctor.maven.log.MemoryLogHandler;
import org.asciidoctor.maven.site.SiteConverterDecorator.Result;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.asciidoctor.maven.site.SiteBaseDirResolver.resolveBaseDir;

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

    private static final Logger logger = LoggerFactory.getLogger(AsciidoctorConverterDoxiaParser.class);

    @Inject
    protected Provider<MavenProject> mavenProjectProvider;

    /**
     * The role hint for the {@link AsciidoctorConverterDoxiaParser} Plexus component.
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
            logger.error("Could not read AsciiDoc source: {}", ex.getLocalizedMessage());
            return;
        }

        final MavenProject project = mavenProjectProvider.get();
        final Xpp3Dom siteConfig = getSiteConfig(project);
        final File siteDirectory = resolveBaseDir(project.getBasedir(), siteConfig);

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
        final MemoryLogHandler memoryLogHandler = asciidoctorLoggingSetup(asciidoctor, siteDirectory);

        final SiteConverterDecorator siteConverter = new SiteConverterDecorator(asciidoctor);
        final Result headerMetadata = siteConverter.process(source, conversionConfig.getOptions());

        try {
            // process log messages according to mojo configuration
            if (!memoryLogHandler.isEmpty()) {
                logger.info("Issues found in: {}", reference);
                if (logHandler.getOutputToConsole() && StringUtils.isNotBlank(reference)) {
                    memoryLogHandler.processAll();
                }
                new LogRecordsProcessors(logHandler, siteDirectory, errorMessage -> logger.error(errorMessage))
                    .processLogRecords(memoryLogHandler);
            }
        } catch (Exception exception) {
            throw new ParseException(exception.getMessage(), exception);
        }

        new HeadParser(sink)
            .parse(headerMetadata.getHeaderMetadata());

        sink.rawText(headerMetadata.getHtml());
    }

    private MemoryLogHandler asciidoctorLoggingSetup(Asciidoctor asciidoctor, File siteDirectory) {

        final MemoryLogHandler memoryLogHandler = new MemoryLogHandler(false,
            logRecord -> logger.info(LogRecordFormatter.format(logRecord, siteDirectory)));
        asciidoctor.registerLogHandler(memoryLogHandler);
        // disable default console output of AsciidoctorJ
        java.util.logging.Logger.getLogger("asciidoctor").setUseParentHandlers(false);
        return memoryLogHandler;
    }

    private LogHandler getLogHandlerConfig(Xpp3Dom siteConfig) {
        Xpp3Dom asciidoc = siteConfig == null ? null : siteConfig.getChild("asciidoc");
        return new SiteLogHandlerDeserializer().deserialize(asciidoc);
    }

    protected Xpp3Dom getSiteConfig(MavenProject project) {
        return project.getGoalConfiguration("org.apache.maven.plugins", "maven-site-plugin", "site", "site");
    }


    // The possible baseDir based on configuration are:
    //
    // with nothing                : src/site + /asciidoc
    // with locale                 : src/site + {locale} +  /asciidoc
    // with siteDirectory          : {siteDirectory} + /asciidoc
    // with siteDirectory + locale : {siteDirectory} + {locale} + /asciidoc
    protected OptionsBuilder defaultOptions(File siteDirectory) {
        return Options.builder()
            .backend("xhtml")
            .safe(SafeMode.UNSAFE)
            .baseDir(new File(siteDirectory, ROLE_HINT).getAbsoluteFile());
    }

    protected AttributesBuilder defaultAttributes() {
        return Attributes.builder()
            .attribute("idprefix", "@")
            .attribute("showtitle", "@");
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
