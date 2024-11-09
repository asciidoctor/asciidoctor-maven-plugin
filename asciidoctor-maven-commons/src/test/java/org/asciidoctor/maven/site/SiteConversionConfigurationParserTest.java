package org.asciidoctor.maven.site;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.SafeMode;
import org.assertj.core.data.MapEntry;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;

import static org.asciidoctor.Options.*;
import static org.asciidoctor.maven.site.SiteConversionConfigurationParserTest.FakeMavenProjectBuilder.fakeMavenProjectBuilder;
import static org.assertj.core.api.Assertions.assertThat;

class SiteConversionConfigurationParserTest {

    private static final String ROLE_HINT = "asciidoc";

    final SiteBaseDirResolver siteBaseDirResolver = new SiteBaseDirResolver();
    final SiteConversionConfigurationParser configParser = new SiteConversionConfigurationParser(siteBaseDirResolver);

    @Test
    void should_return_default_configuration_when_site_xml_is_null() {
        // given
        final MavenProject project = fakeMavenProjectBuilder().build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNull();
        assertContainsDefaultOptions(configuration);
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires()).isEmpty();
    }

    @Test
    void should_return_default_configuration_when_asciidoc_xml_is_null() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.siteNode().build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        final Map<String, Object> optionsMap = assertContainsDefaultAttributes(configuration);
        assertThat((String) optionsMap.get(BACKEND)).isEqualTo("xhtml");
        assertThat((String) optionsMap.get(BASEDIR)).endsWith(defaultPluginSources());
        assertThat((Integer) optionsMap.get(SAFE)).isEqualTo(SafeMode.UNSAFE.getLevel());
        assertThat(configuration.getRequires()).isEmpty();
    }

    // TODO this should be the same?
    private CharSequence defaultSiteSources() {
        return String.join(File.separator, new String[]{"src", "site"});
    }

    private CharSequence defaultPluginSources() {
        return String.join(File.separator, new String[]{"src", "site", "asciidoc"});
    }

    @Test
    void should_return_simple_single_requires() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("requires")
            .addChild("require", "gem")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires())
            .containsExactly("gem");
    }

    @Test
    void should_return_multiple_requires() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("requires")
            .addChild("require", "gem_1", "gem_2", "gem_3")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires())
            .containsExactlyInAnyOrder("gem_1", "gem_2", "gem_3");
    }

    @Test
    void should_return_multiple_requires_when_defined_in_single_element() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("requires")
            .addChild("require", "gem_1,gem_2, gem_3")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires())
            .containsExactlyInAnyOrder("gem_1", "gem_2", "gem_3");
    }

    @Test
    void should_remove_empty_and_blank_requires() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("requires")
            .addChild("require", "gem_1,,gem_2", "", ",,", "gem_3")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires())
            .containsExactlyInAnyOrder("gem_1", "gem_2", "gem_3");
    }

    @Test
    void should_return_attributes() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("attributes")
            .addChild("imagesdir", "./images")
            .parent().addChild("source-highlighter", "coderay")
            .parent().addChild("sectnums")
            .parent().addChild("toc")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertThat(configuration.getRequires()).isEmpty();
        Map attributes = (Map) configuration.getOptions()
            .map()
            .get(ATTRIBUTES);
        assertThat(attributes).containsExactlyInAnyOrderEntriesOf(map(
            entry("idprefix", "@"),
            entry("imagesdir", "./images"),
            entry("sectnums", ""),
            entry("showtitle", "@"),
            entry("source-highlighter", "coderay"),
            entry("toc", "")
        ));
    }

    @Test
    void should_map_null_attributes_as_empty_string() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("attributes")
            .addChild("toc")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertThat(configuration.getRequires()).isEmpty();
        Map attributes = (Map) configuration.getOptions()
            .map()
            .get(ATTRIBUTES);
        assertThat(attributes).containsExactlyInAnyOrderEntriesOf(map(
            entry("idprefix", "@"),
            entry("showtitle", "@"),
            entry("toc", "")
        ));

    }

    @Test
    void should_map_true_boolean_attribute_as_empty_string_value() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("attributes")
            .addChild("toc", "true")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertThat(configuration.getRequires()).isEmpty();
        Map attributes = (Map) configuration
            .getOptions()
            .map()
            .get(ATTRIBUTES);
        assertThat(attributes).containsExactlyInAnyOrderEntriesOf(map(
            entry("idprefix", "@"),
            entry("showtitle", "@"),
            entry("toc", "")
        ));
    }

    @Test
    void should_map_false_boolean_attribute_as_null_value() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("attributes")
            .addChild("toc", "false")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertThat(configuration.getRequires()).isEmpty();
        Map attributes = (Map) configuration
            .getOptions()
            .map()
            .get(ATTRIBUTES);
        assertThat(attributes).containsExactlyInAnyOrderEntriesOf(map(
            entry("idprefix", "@"),
            entry("showtitle", "@"),
            entry("toc", null)
        ));
    }

    @Test
    void should_return_template_dirs_when_defined_as_templateDirs_dir() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("templateDirs")
            .addChild("dir", "path")
            .parent()
            .addChild("dir", "path2")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires()).isEmpty();

        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES, BACKEND, BASEDIR, SAFE, TEMPLATE_DIRS);
        assertThat(optionsMap.get(TEMPLATE_DIRS))
            .isEqualTo(Arrays.asList(
                new File("path").getAbsolutePath(),
                new File("path2").getAbsolutePath()
            ));
    }

    @Test
    void should_return_template_dirs_when_defined_as_template_dirs_dir() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("template_dirs")
            .addChild("dir", "path")
            .parent()
            .addChild("dir", "path2")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires()).isEmpty();

        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES, BACKEND, BASEDIR, SAFE, TEMPLATE_DIRS);
        assertThat(optionsMap.get(TEMPLATE_DIRS))
            .isEqualTo(Arrays.asList(
                new File("path").getAbsolutePath(),
                new File("path2").getAbsolutePath()
            ));
    }

    @Test
    void should_not_return_empty_template_dirs() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("template_dirs")
            .addChild("dir", "")
            .parent()
            .addChild("dir")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then: BASE_DIR option is not even added
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires()).isEmpty();
    }

    @Test
    void should_return_baseDir_dirs_when_defined_as_template_dirs_dir() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("baseDir", "path")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires()).isEmpty();

        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES, BACKEND, BASEDIR, SAFE);
        assertThat((String) optionsMap.get(BACKEND)).isEqualTo("xhtml");
        assertThat((Integer) optionsMap.get(SAFE)).isEqualTo(SafeMode.UNSAFE.getLevel());
        assertThat(optionsMap.get(BASEDIR))
            .isEqualTo(new File("path").getAbsolutePath());
    }

    @Test
    void should_return_any_configuration_inside_asciidoc_node_as_option() {
        // given
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
            .addChild("option-1", "value-1")
            .parent().addChild("option_2", "value-2")
            .parent().addChild("_option-3", "value-3")
            .parent().addChild("option-4_", "value-4")
            .parent().addChild("option.5", "value-5")
            .build();
        final MavenProject project = fakeMavenProjectBuilder().siteConfig(siteConfig).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultAttributes(configuration);
        assertThat(configuration.getRequires()).isEmpty();

        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap)
            .containsOnlyKeys(
                ATTRIBUTES, BACKEND, BASEDIR, SAFE,
                "option-1", "option_2", "_option-3", "option-4_", "option.5");
        assertThat(optionsMap.get("option-1")).isEqualTo("value-1");
        assertThat(optionsMap.get("option_2")).isEqualTo("value-2");
        assertThat(optionsMap.get("_option-3")).isEqualTo("value-3");
        assertThat(optionsMap.get("option-4_")).isEqualTo("value-4");
        assertThat(optionsMap.get("option.5")).isEqualTo("value-5");
    }

    @Test
    void should_return_and_format_any_maven_project_property_as_attribute_when_site_config_is_not_present() {
        // given
        final Map<String, String> projectProperties = new HashMap<>();
        projectProperties.put("mvn.property-test1", "value-1");
        projectProperties.put("mvn-property.test2", "value_2");
        final MavenProject project = fakeMavenProjectBuilder().properties(projectProperties).build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNull();
        assertContainsDefaultOptions(configuration);
        assertThat(configuration.getRequires()).isEmpty();

        final Map<String, Object> optionsMap = configuration.getOptions().map();
        Map attributes = (Map) optionsMap.get(ATTRIBUTES);
        assertThat(attributes).containsExactlyInAnyOrderEntriesOf(map(
            entry("mvn-property-test1", "value-1"),
            entry("mvn-property-test2", "value_2"),
            entry("idprefix", "@"),
            entry("showtitle", "@")
        ));
    }

    @Test
    void should_return_and_format_any_maven_project_property_as_attribute_when_site_config_is_present() {
        // given
        final Map<String, String> projectProperties = new HashMap<>();
        projectProperties.put("mvn.property-test1", "value-1");
        projectProperties.put("mvn-property.test2", "value_2");
        final Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode().build();
        final MavenProject project = fakeMavenProjectBuilder()
            .properties(projectProperties)
            .siteConfig(siteConfig)
            .build();

        // when
        SiteConversionConfiguration configuration = configParser.processAsciiDocConfig(project, ROLE_HINT);

        // then
        assertThat(configuration.getSiteBaseDir().getPath()).endsWith(defaultSiteSources());
        assertThat(configuration.getSiteConfig()).isNotNull();
        assertContainsDefaultOptions(configuration);
        assertThat(configuration.getRequires()).isEmpty();

        final Map<String, Object> optionsMap = configuration.getOptions().map();
        Map attributes = (Map) optionsMap.get(ATTRIBUTES);
        assertThat(attributes).containsExactlyInAnyOrderEntriesOf(map(
            entry("mvn-property-test1", "value-1"),
            entry("mvn-property-test2", "value_2"),
            entry("idprefix", "@"),
            entry("showtitle", "@")
        ));
    }

    private void assertContainsDefaultOptions(SiteConversionConfiguration configuration) {
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES, BACKEND, BASEDIR, SAFE);
        assertThat((String) optionsMap.get(BACKEND)).isEqualTo("xhtml");
        assertThat((String) optionsMap.get(BASEDIR)).endsWith(defaultPluginSources());
        assertThat((Integer) optionsMap.get(SAFE)).isEqualTo(SafeMode.UNSAFE.getLevel());
    }

    private static Map<String, Object> assertContainsDefaultAttributes(SiteConversionConfiguration configuration) {
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat((Map) optionsMap.get(ATTRIBUTES)).containsExactly(
            MapEntry.entry("idprefix", "@"),
            MapEntry.entry("showtitle", "@")
        );
        return optionsMap;
    }

    static class FakeMavenProjectBuilder {
        Xpp3Dom siteConfig = null;
        Map<String, String> properties = null;

        static FakeMavenProjectBuilder fakeMavenProjectBuilder() {
            return new FakeMavenProjectBuilder();
        }

        FakeMavenProjectBuilder siteConfig(Xpp3Dom siteConfig) {
            this.siteConfig = siteConfig;
            return this;
        }

        FakeMavenProjectBuilder properties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        MavenProject build() {
            final Model model = new Model();

            if (properties != null) {
                model.getProperties().putAll(properties);
            }

            final MavenProject project = new MavenProject(model);
            project.setFile(new File(".").getAbsoluteFile());

            final Build build = new Build();
            if (siteConfig != null) {
                final Plugin sitePlugin = new Plugin();
                sitePlugin.setGroupId("org.apache.maven.plugins");
                sitePlugin.setArtifactId("maven-site-plugin");

                final PluginExecution pluginExecution = new PluginExecution();
                pluginExecution.setId("site");
                pluginExecution.setGoals(List.of("site"));
                sitePlugin.setExecutions(List.of(pluginExecution));

                pluginExecution.setConfiguration(siteConfig);

                build.addPlugin(sitePlugin);
            }

            model.setBuild(build);
            return project;
        }
    }

    private MavenProject fakeProject() {
        return fakeProject(null);
    }

    private MavenProject fakeProject(Map<String, String> properties) {
        final Model model = new Model();

        if (properties != null) {
            model.getProperties().putAll(properties);
        }

        final MavenProject project = new MavenProject(model);
        final Plugin sitePlugin = new Plugin();
        sitePlugin.setGroupId("org.apache.maven.plugins");
        sitePlugin.setArtifactId("maven-site-plugin");
        final PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.setId("site");
        pluginExecution.setGoals(List.of("site"));
        sitePlugin.setExecutions(List.of(pluginExecution));
        final Build build = new Build();
        build.addPlugin(sitePlugin);
        model.setBuild(build);


        project.setFile(new File(".").getAbsoluteFile());

        return project;
    }

    private Map<String, Object> map(Map.Entry<String, Object>... entries) {
        final Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private AbstractMap.SimpleEntry<String, Object> entry(String key, Object value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }
}
