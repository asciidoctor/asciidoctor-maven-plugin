package org.asciidoctor.maven.site;

import java.io.File;
import java.nio.file.Path;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Calculates the path where the root of the sources are located based on the
 * maven-site-plugin configuration.
 * Not to confuse with Asciidoctor's baseDir.
 *
 * @author abelsromero
 * @since 3.1.1
 */
public class SiteBaseDirResolver {

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
                // For now,2 we support 1 locale
                String[] split = locales.getValue().split(",");
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
