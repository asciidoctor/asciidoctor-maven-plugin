package org.asciidoctor.maven.site;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.log.LogRecordFormatter;
import org.asciidoctor.maven.log.MemoryLogHandler;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;

import java.io.File;

public class LogHandlerFactory {

    public LogHandler getConfiguration(Xpp3Dom siteConfig) {
        Xpp3Dom asciidoc = siteConfig == null ? null : siteConfig.getChild("asciidoc");
        return new SiteLogHandlerDeserializer().deserialize(asciidoc);
    }

    public MemoryLogHandler create(Asciidoctor asciidoctor, File siteDirectory, Logger logger) {

        final MemoryLogHandler memoryLogHandler = new MemoryLogHandler(false,
            logRecord -> logger.info(LogRecordFormatter.format(logRecord, siteDirectory)));
        asciidoctor.registerLogHandler(memoryLogHandler);
        // disable default console output of AsciidoctorJ
        java.util.logging.Logger.getLogger("asciidoctor").setUseParentHandlers(false);
        return memoryLogHandler;
    }
}
