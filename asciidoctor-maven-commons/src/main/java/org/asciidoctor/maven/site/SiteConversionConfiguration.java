package org.asciidoctor.maven.site;

import org.asciidoctor.Options;

import java.util.List;

public class SiteConversionConfiguration {

    private final Options options;
    private final List<String> requires;

    SiteConversionConfiguration(Options options, List<String> requires) {
        this.options = options;
        this.requires = requires;
    }

    public Options getOptions() {
        return options;
    }

    public List<String> getRequires() {
        return requires;
    }
}
