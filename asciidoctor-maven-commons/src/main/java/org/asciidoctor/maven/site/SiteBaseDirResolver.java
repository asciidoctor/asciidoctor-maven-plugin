package org.asciidoctor.maven.site;

import java.io.File;
import java.nio.file.Path;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the path where the root of the sources are located based on the
 * maven-site-plugin configuration.
 * Not to confuse with Asciidoctor's baseDir.
 *
 * @author abelsromero
 * @since 3.1.1
 */
public class SiteBaseDirResolver {

    private static final Logger logger = LoggerFactory.getLogger(SiteBaseDirResolver.class);

    public static File resolveBaseDir(File mavenBaseDir, Xpp3Dom siteConfig) {
        final String siteDirectory = resolveSiteDirectory(siteConfig);
        final String locale = resolveLocale(siteConfig);

        final Path path = Path.of(mavenBaseDir.getPath());

        if (siteDirectory != null && locale != null)
            return normalize(path, siteDirectory, locale);

        if (siteDirectory != null)
            return normalize(path, siteDirectory);

        if (locale != null)
            return normalize(path, "src/site", locale);

        return normalize(path, "src/site");
    }

    private static String resolveSiteDirectory(Xpp3Dom siteConfig) {
        if (siteConfig != null) {
            Xpp3Dom siteDirectoryNode = siteConfig.getChild("siteDirectory");
            if (siteDirectoryNode != null) {
                return siteDirectoryNode.getValue();
            }
        }
        return null;
    }

    private static String resolveLocale(Xpp3Dom siteConfig) {
        if (siteConfig != null) {
            final Xpp3Dom locales = siteConfig.getChild("locales");
            if (locales != null) {
                // We can support 1 locale: https://issues.apache.org/jira/browse/DOXIA-755
                String[] split = locales.getValue().split(",");
                if (split.length > 0) {
                    logger.warn("Multiple locales found: {}, this is not supported. Configure multiple plugin executions instead.", locales.getValue());
                }
                return split[0];
            }
        }
        return null;
    }

    private static File normalize(Path path, String... other) {
        for (String value : other)
            path = path.resolve(value);

        return path.normalize().toFile();
    }
}
