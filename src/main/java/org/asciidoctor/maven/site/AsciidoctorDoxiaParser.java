/*
 * Copyright 2015 The Ascidoctor Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.maven.site;

import org.apache.maven.doxia.module.xhtml.XhtmlParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.*;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * This class is used by <a href="https://maven.apache.org/doxia/overview.html">the Doxia framework</a>
 * to handle the actual parsing of the AsciiDoc input files into HTML to be consumed/wrapped
 * by the Maven site generation process
 * (see <a href="https://maven.apache.org/plugins/maven-site-plugin/">maven-site-plugin</a>).
 *
 * @author jdlee
 * @author mojavelinux
 */
@Component(role = Parser.class, hint = AsciidoctorDoxiaParser.ROLE_HINT)
public class AsciidoctorDoxiaParser extends XhtmlParser {

    @Inject
    protected Provider<MavenProject> mavenProjectProvider;

    /**
     * The role hint for the {@link AsciidoctorDoxiaParser} Plexus component.
     */
    public static final String ROLE_HINT = "asciidoc";

    protected final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(Reader reader, Sink sink) throws ParseException {
        String source = null;
        try {
            if ((source = IOUtil.toString(reader)) == null) {
                source = "";
            }
        } catch (IOException ex) {
            getLog().error("Could not read AsciiDoc source: " + ex.getLocalizedMessage());
            return;
        }

        MavenProject project = mavenProjectProvider.get();

        Xpp3Dom siteConfig = getSiteConfig(project);
        File siteDirectory = resolveSiteDirectory(project, siteConfig);
        SiteConversionConfiguration conversionConfig = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, defaultOptions(siteDirectory), defaultAttributes());

        for (String require : conversionConfig.getRequires()) {
            requireLibrary(require);
        }

        // QUESTION should we keep OptionsBuilder & AttributesBuilder separate for call to convertAsciiDoc?
        sink.rawText(convertAsciiDoc(source, conversionConfig.getOptions()));
    }

    protected Xpp3Dom getSiteConfig(MavenProject project) {
        return project.getGoalConfiguration("org.apache.maven.plugins", "maven-site-plugin", "site", "site");
    }

    protected File resolveSiteDirectory(MavenProject project, Xpp3Dom siteConfig) {
        File siteDirectory = new File(project.getBasedir(), "src/site");
        if (siteConfig != null) {
            Xpp3Dom siteDirectoryNode = siteConfig.getChild("siteDirectory");
            if (siteDirectoryNode != null) {
                siteDirectory = new File(siteDirectoryNode.getValue());
            }
        }
        return siteDirectory;
    }

    protected OptionsBuilder defaultOptions(File siteDirectory) {
        return OptionsBuilder.options()
                .backend("xhtml")
                .safe(SafeMode.UNSAFE)
                .baseDir(new File(siteDirectory, ROLE_HINT));
    }

    protected AttributesBuilder defaultAttributes() {
        return AttributesBuilder.attributes()
                .attribute("idprefix", "@")
                .attribute("showtitle", "@");
    }

    private void requireLibrary(String require) {
        if (!(require = require.trim()).isEmpty()) {
            try {
                asciidoctor.requireLibrary(require);
            } catch (Exception ex) {
                getLog().error(ex.getLocalizedMessage());
            }
        }
    }

    protected String convertAsciiDoc(String source, Options options) {
        return asciidoctor.convert(source, options);
    }

}
