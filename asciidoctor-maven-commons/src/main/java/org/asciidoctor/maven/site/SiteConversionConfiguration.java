package org.asciidoctor.maven.site;

import java.util.List;

import org.asciidoctor.Options;

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
