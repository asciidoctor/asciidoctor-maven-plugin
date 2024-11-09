package org.asciidoctor.maven.site;

import java.io.File;
import java.util.List;

import org.asciidoctor.Options;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public final class SiteConversionConfiguration {

    private final Xpp3Dom siteConfig;
    private final File siteBaseDir;
    private final Options options;
    private final List<String> requires;

    SiteConversionConfiguration(Xpp3Dom siteConfig,
                                File siteBaseDir,
                                Options options,
                                List<String> requires) {
        this.siteConfig = siteConfig;
        this.siteBaseDir = siteBaseDir;
        this.options = options;
        this.requires = requires;
    }

    public File getSiteBaseDir() {
        return siteBaseDir;
    }

    public Xpp3Dom getSiteConfig() {
        return siteConfig;
    }

    public Options getOptions() {
        return options;
    }

    public List<String> getRequires() {
        return requires;
    }
}
