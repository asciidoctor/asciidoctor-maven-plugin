package org.asciidoctor.maven.site;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.project.MavenProject;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.maven.commons.AsciidoctorHelper;
import org.asciidoctor.maven.commons.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * Extract Asciidoctor required configurations from Maven Site Plugin
 * configuration in POM file.
 *
 * @author mojavelinux
 * @author abelsromero
 * @since 2.0.0
 */
@Singleton
public class SiteConversionConfigurationParser {

    private final SiteBaseDirResolver siteBaseDirResolver;

    @Inject
    public SiteConversionConfigurationParser(SiteBaseDirResolver siteBaseDirResolver) {
        this.siteBaseDirResolver = siteBaseDirResolver;
    }

    public SiteConversionConfiguration processAsciiDocConfig(MavenProject mavenProject, String roleHint) {

        final AttributesBuilder presetAttributes = defaultAttributes();
        AsciidoctorHelper.addProperties(mavenProject.getProperties(), presetAttributes);
        final Attributes attributes = presetAttributes.build();

        final File siteDir = siteBaseDirResolver.resolveBaseDir(mavenProject.getBasedir(), getSiteConfig(mavenProject));
        final OptionsBuilder presetOptions = defaultOptions(siteDir, roleHint);

        final Xpp3Dom asciidocConfig = Optional.ofNullable(getSiteConfig(mavenProject))
            .map(node -> node.getChild("asciidoc"))
            .orElse(null);

        if (asciidocConfig == null) {
            final Options options = presetOptions.attributes(attributes).build();
            return new SiteConversionConfiguration(null, siteDir, options, Collections.emptyList());
        }

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
                            if (isNotBlank(value))
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
                    .map(node -> resolveProjectDir(mavenProject, node.getValue()))
                    .collect(Collectors.toList());
                presetOptions.templateDirs(dirs.toArray(new File[dirs.size()]));
            } else if ("baseDir".equals(optName)) {
                presetOptions.baseDir(resolveProjectDir(mavenProject, asciidocOpt.getValue()));
            } else {
                presetOptions.option(optName.replaceAll("(?<!_)([A-Z])", "_$1").toLowerCase(), asciidocOpt.getValue());
            }
        }

        final Options options = presetOptions.attributes(attributes).build();
        return new SiteConversionConfiguration(asciidocConfig, siteDir, options, gemsToRequire);
    }

    private Xpp3Dom getSiteConfig(MavenProject mavenProject) {
        return mavenProject.getGoalConfiguration("org.apache.maven.plugins", "maven-site-plugin", "site", "site");
    }

    private File resolveProjectDir(MavenProject mavenProject, String path) {
        final File filePath = new File(path);
        return !filePath.isAbsolute() ? new File(mavenProject.getBasedir(), filePath.toString()).getAbsoluteFile() : filePath;
    }

    // The possible baseDir based on configuration are:
    //
    // with nothing                : src/site + /asciidoc
    // with locale                 : src/site + {locale} +  /asciidoc
    // with siteDirectory          : {siteDirectory} + /asciidoc
    // with siteDirectory + locale : {siteDirectory} + {locale} + /asciidoc
    private OptionsBuilder defaultOptions(File siteDirectory, String roleHint) {
        return Options.builder()
            .backend("xhtml")
            .safe(SafeMode.UNSAFE)
            .baseDir(new File(siteDirectory, roleHint).getAbsoluteFile());
    }

    private AttributesBuilder defaultAttributes() {
        return Attributes.builder()
            .attribute("idprefix", "@")
            .attribute("showtitle", "@");
    }
}
