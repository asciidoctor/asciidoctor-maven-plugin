/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.asciidoc.maven.site;

import org.apache.maven.doxia.module.site.AbstractSiteModule;
import org.apache.maven.doxia.module.site.SiteModule;
import org.codehaus.plexus.component.annotations.Component;

/**
 *
 * @author jdlee
 */
@Component( role = SiteModule.class, hint = AsciidoctorParser.ROLE_HINT )
public class AsciidoctorSiteModule extends AbstractSiteModule {

    /**
     * The source directory for Markdown files.
     */
    public static final String SOURCE_DIRECTORY = "asciidoc";
    /**
     * The extension for Markdown files.
     */
    public static final String FILE_EXTENSION = "adoc";

    /**
     * Build a new instance of {@link MarkdownSiteModule}.
     */
    public AsciidoctorSiteModule() {
        super(SOURCE_DIRECTORY, FILE_EXTENSION, AsciidoctorParser.ROLE_HINT);
    }
}
