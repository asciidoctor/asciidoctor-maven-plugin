package org.asciidoctor.maven.site;

import org.apache.maven.project.MavenProject;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.maven.commons.AsciidoctorHelper;
import org.asciidoctor.maven.commons.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;
import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

public class SiteConversionConfigurationParser {

    private final MavenProject project;

    public SiteConversionConfigurationParser(MavenProject project) {
        this.project = project;
    }

    public SiteConversionConfiguration processAsciiDocConfig(Xpp3Dom siteConfig,
                                                      OptionsBuilder presetOptions,
                                                      AttributesBuilder presetAttributes) {

        if (siteConfig == null) {
            OptionsBuilder options = presetOptions.attributes(presetAttributes);
            return new SiteConversionConfiguration(options.get(), Collections.emptyList());
        }

        final Xpp3Dom asciidocConfig = siteConfig.getChild("asciidoc");
        if (asciidocConfig == null) {
            OptionsBuilder options = presetOptions.attributes(presetAttributes);
            return new SiteConversionConfiguration(options.get(), Collections.emptyList());
        }

        AsciidoctorHelper.addProperties(project.getProperties(), presetAttributes);

        final List<String> gemsToRequire = new ArrayList<>();
        for (Xpp3Dom asciidocOpt : asciidocConfig.getChildren()) {
            String optName = asciidocOpt.getName();

            if ("requires".equals(optName)) {
                Xpp3Dom[] requires = asciidocOpt.getChildren("require");
                if (requires.length > 0) {
                    for (Xpp3Dom requireNode : requires) {
                        if (requireNode.getValue().contains(",")) {
                            // <requires>time, base64</requires>
                            Stream.of(requireNode.getValue().split(","))
                                    .filter(StringUtils::isNotBlank)
                                    .map(String::trim)
                                    .forEach(value -> gemsToRequire.add(value));
                        } else {
                            // <requires>
                            //     <require>time</require>
                            // </requires>
                            String value = requireNode.getValue();
                            if (!isBlank(value))
                                gemsToRequire.add(value.trim());
                        }
                    }
                }
            } else if ("attributes".equals(optName)) {
                for (Xpp3Dom asciidocAttr : asciidocOpt.getChildren()) {
                    AsciidoctorHelper.addAttribute(asciidocAttr.getName(), asciidocAttr.getValue(), presetAttributes);
                }
            } else if ("templateDirs".equals(optName) || "template_dirs".equals(optName)) {
                List<File> dirs = Arrays.stream(asciidocOpt.getChildren("dir"))
                        .filter(node -> isNotBlank(node.getValue()))
                        .map(node -> resolveProjectDir(project, node.getValue()))
                        .collect(Collectors.toList());
                presetOptions.templateDirs(dirs.toArray(new File[dirs.size()]));
            } else if ("baseDir".equals(optName)) {
                presetOptions.baseDir(resolveProjectDir(project, asciidocOpt.getValue()));
            } else {
                presetOptions.option(optName.replaceAll("(?<!_)([A-Z])", "_$1").toLowerCase(), asciidocOpt.getValue());
            }
        }

        return new SiteConversionConfiguration(presetOptions.attributes(presetAttributes).get(), gemsToRequire);
    }

    private File resolveProjectDir(MavenProject project, String path) {
        File filePath = new File(path);
        if (!filePath.isAbsolute()) {
            filePath = new File(project.getBasedir(), filePath.toString());
        }
        return filePath;
    }

}
