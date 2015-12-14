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
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.internal.RubyUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used by the Doxia framework to handle the actual parsing of the
 * AsciiDoc input files into HTML to be consumed/wrapped by the site generation
 * process.
 *
 * @author jdlee
 * @author mojavelinux
 */
@Component(role = Parser.class, hint = AsciidoctorParser.ROLE_HINT)
public class AsciidoctorParser extends XhtmlParser {

    @Requirement
    protected MavenProject project;

    /**
     * The role hint for the {@link AsciidoctorParser} Plexus component.
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
        }
        catch (IOException ex) {
            getLog().error("Could not read AsciiDoc source: " + ex.getLocalizedMessage());
            return;
        }
        Xpp3Dom siteConfig = getSiteConfig(project);
        File siteDirectory = resolveSiteDirectory(project, siteConfig);
        OptionsBuilder options = processAsciiDocConfig(
                siteConfig,
                initOptions(project, siteDirectory),
                initAttributes(project, siteDirectory));
        // QUESTION should we keep OptionsBuilder & AttributesBuilder separate for call to convertAsciiDoc?
        sink.rawText(convertAsciiDoc(source, options));
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

    protected OptionsBuilder initOptions(MavenProject project, File siteDirectory) {
        return OptionsBuilder.options()
                .backend("xhtml")
                .safe(SafeMode.UNSAFE)
                .baseDir(new File(siteDirectory, ROLE_HINT));
    }

    protected AttributesBuilder initAttributes(MavenProject project, File siteDirectory) {
        return AttributesBuilder.attributes()
            .attribute("idprefix", "@")
            .attribute("showtitle", "@");
    }

    protected OptionsBuilder processAsciiDocConfig(Xpp3Dom siteConfig, OptionsBuilder options, AttributesBuilder attributes) {
        if (siteConfig == null) {
            return options.attributes(attributes);
        }

        Xpp3Dom asciidocConfig = siteConfig.getChild("asciidoc");
        if (asciidocConfig == null) {
            return options.attributes(attributes);
        }

        for (Xpp3Dom asciidocOpt : asciidocConfig.getChildren()) {
            String optName = asciidocOpt.getName();
            if ("attributes".equals(optName)) {
                for (Xpp3Dom asciidocAttr : asciidocOpt.getChildren()) {
                    attributes.attribute(asciidocAttr.getName(), asciidocAttr.getValue());
                }
            }
            else if ("requires".equals(optName)) {
                Xpp3Dom[] requires = asciidocOpt.getChildren("require");
                // supports variant:
                // <requires>
                //     <require>time</require>
                // </requires>
                if (requires.length > 0) {
                    for (Xpp3Dom require : requires) {
                        requireLibrary(require.getValue());
                    }
                }
                else {
                    // supports variant:
                    // <requires>time, base64</requires>
                    for (String require : asciidocOpt.getValue().split(",")) {
                        requireLibrary(require);
                    }
                }
            }
            else if ("templateDir".equals(optName) || "template_dir".equals(optName)) {
                options.templateDir(resolveTemplateDir(project, asciidocOpt.getValue()));
            }
            else if ("templateDirs".equals(optName) || "template_dirs".equals(optName)) {
                List<File> templateDirs = new ArrayList<File>();
                for (Xpp3Dom dir : asciidocOpt.getChildren("dir")) {
                    templateDirs.add(resolveTemplateDir(project, dir.getValue()));
                }
                if (!templateDirs.isEmpty()) {
                    options.templateDirs(templateDirs.toArray(new File[templateDirs.size()]));
                }
            }
            else {
                options.option(optName.replaceAll("(?<!_)([A-Z])", "_$1").toLowerCase(), asciidocOpt.getValue());
            }
        }
        return options.attributes(attributes);
    }

    protected String convertAsciiDoc(String source, OptionsBuilder options) {
        return asciidoctor.convert(source, options);
    }

    protected File resolveTemplateDir(MavenProject project, String path) {
        File templateDir = new File(path);
        if (!templateDir.isAbsolute()) {
            templateDir = new File(project.getBasedir(), templateDir.toString());
        }
        return templateDir;
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
}
