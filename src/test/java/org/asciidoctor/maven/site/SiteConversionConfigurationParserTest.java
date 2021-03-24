package org.asciidoctor.maven.site;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.asciidoctor.Options.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SiteConversionConfigurationParserTest {

    @Test
    public void should_return_default_configuration_when_site_xml_is_null() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(null, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES);
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
        assertThat(configuration.getRequires()).isEmpty();
    }

    @Test
    public void should_return_default_configuration_when_asciidoc_xml_is_null() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.siteNode()
                .build();
        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES);
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
        assertThat(configuration.getRequires()).isEmpty();
    }

    @Test
    public void should_return_simple_single_requires() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("requires")
                .addChild("require", "gem")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES);
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
        assertThat(configuration.getRequires())
                .containsExactly("gem");
    }

    @Test
    public void should_return_multiple_requires() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("requires")
                .addChild("require", "gem_1", "gem_2", "gem_3")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES);
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
        assertThat(configuration.getRequires())
                .containsExactlyInAnyOrder("gem_1", "gem_2", "gem_3");
    }

    @Test
    public void should_return_multiple_requires_when_defined_in_single_element() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("requires")
                .addChild("require", "gem_1,gem_2, gem_3")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES);
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
        assertThat(configuration.getRequires())
                .containsExactlyInAnyOrder("gem_1", "gem_2", "gem_3");
    }

    @Test
    public void should_remove_empty_and_blank_requires() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("requires")
                .addChild("require", "gem_1,,gem_2", "", ",,", "gem_3")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES);
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
        assertThat(configuration.getRequires())
                .containsExactlyInAnyOrder("gem_1", "gem_2", "gem_3");
    }

    @Test
    public void should_return_attributes() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("attributes")
                .addChild("imagesdir", "./images")
                .parent().addChild("source-highlighter", "coderay")
                .parent().addChild("sectnums")
                .parent().addChild("toc", null)
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        Map attributes = (Map) configuration.getOptions()
                .map()
                .get(ATTRIBUTES);
        assertThat(attributes).
                containsExactlyInAnyOrderEntriesOf(map(
                        entry("imagesdir", "./images"),
                        entry("source-highlighter", "coderay"),
                        entry("sectnums", ""),
                        entry("toc", "")
                ));
    }

    @Test
    public void should_map_null_attributes_as_empty_string() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("attributes")
                .addChild("toc", null)
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        Map attributes = (Map) configuration
                .getOptions()
                .map()
                .get(ATTRIBUTES);
        assertThat(attributes).
                containsExactlyInAnyOrderEntriesOf(map(
                        entry("toc", "")
                ));
    }

    @Test
    public void should_map_true_boolean_attribute_as_empty_string_value() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("attributes")
                .addChild("toc", "true")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        Map attributes = (Map) configuration
                .getOptions()
                .map()
                .get(ATTRIBUTES);
        assertThat(attributes).
                containsExactlyInAnyOrderEntriesOf(map(
                        entry("toc", "")
                ));
    }

    @Test
    public void should_map_false_boolean_attribute_as_null_value() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("attributes")
                .addChild("toc", "false")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap)
                .hasSize(1);
        Map attributes = (Map) optionsMap.get(ATTRIBUTES);
        assertThat(attributes).
                containsExactlyInAnyOrderEntriesOf(map(
                        entry("toc", null)
                ));
    }

    @Test
    public void should_return_template_dirs_when_defined_as_templateDirs_dir() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("templateDirs")
                .addChild("dir", "path")
                .parent()
                .addChild("dir", "path2")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES, TEMPLATE_DIRS);
        assertThat(optionsMap.get(TEMPLATE_DIRS))
                .isEqualTo(Arrays.asList(
                        new File("path").getAbsolutePath(),
                        new File("path2").getAbsolutePath()
                ));
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
    }

    @Test
    public void should_return_template_dirs_when_defined_as_template_dirs_dir() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("template_dirs")
                .addChild("dir", "path")
                .parent()
                .addChild("dir", "path2")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES, TEMPLATE_DIRS);
        assertThat(optionsMap.get(TEMPLATE_DIRS))
                .isEqualTo(Arrays.asList(
                        new File("path").getAbsolutePath(),
                        new File("path2").getAbsolutePath()
                ));
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
    }

    @Test
    public void should_not_return_empty_template_dirs() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("template_dirs")
                .addChild("dir", "")
                .parent()
                .addChild("dir", null)
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES);
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
    }

    @Test
    public void should_return_baseDir_dirs_when_defined_as_template_dirs_dir() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("baseDir", "path")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();

        assertThat(optionsMap).containsOnlyKeys(ATTRIBUTES, BASEDIR);
        assertThat(optionsMap.get(BASEDIR))
                .isEqualTo(new File("path").getAbsolutePath());
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
    }

    @Test
    public void should_return_any_configuration_inside_asciidoc_node_as_option() {
        // given
        final MavenProject project = fakeProject();
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode()
                .addChild("option-1", "value-1")
                .parent().addChild("option_2", "value-2")
                .parent().addChild("_option-3", "value-3")
                .parent().addChild("option-4_", "value-4")
                .parent().addChild("option.5", "value-5")
                .build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap)
                .containsOnlyKeys(ATTRIBUTES, "option-1", "option_2", "_option-3", "option-4_", "option.5");
        assertThat(optionsMap.get("option-1")).isEqualTo("value-1");
        assertThat(optionsMap.get("option_2")).isEqualTo("value-2");
        assertThat(optionsMap.get("_option-3")).isEqualTo("value-3");
        assertThat(optionsMap.get("option-4_")).isEqualTo("value-4");
        assertThat(optionsMap.get("option.5")).isEqualTo("value-5");
        assertThat((Map) optionsMap.get(ATTRIBUTES)).isEmpty();
    }

    @Test
    public void should_return_and_format_any_maven_project_property_as_attribute() {
        // given
        final Map<String, String> projectProperties = new HashMap<>();
        projectProperties.put("mvn.property-test1", "value-1");
        projectProperties.put("mvn-property.test2", "value_2");
        final MavenProject project = fakeProject(projectProperties);
        OptionsBuilder emptyOptions = OptionsBuilder.options();
        AttributesBuilder emptyAttributes = AttributesBuilder.attributes();
        Xpp3Dom siteConfig = Xpp3DoomBuilder.asciidocNode().build();

        // when
        SiteConversionConfiguration configuration = new SiteConversionConfigurationParser(project)
                .processAsciiDocConfig(siteConfig, emptyOptions, emptyAttributes);

        // then
        final Map<String, Object> optionsMap = configuration.getOptions().map();
        assertThat(optionsMap)
                .containsOnlyKeys(ATTRIBUTES);
        Map attributes = (Map) optionsMap.get(ATTRIBUTES);
        assertThat(attributes).containsExactlyInAnyOrderEntriesOf(map(
                entry("mvn-property-test1", "value-1"),
                entry("mvn-property-test2", "value_2")
        ));
    }

    private MavenProject fakeProject() {
        return fakeProject(null);
    }

    private MavenProject fakeProject(Map<String, String> properties) {
        MavenProject project;
        if (properties != null) {
            final Model model = new Model();
            model.getProperties().putAll(properties);
            project = new MavenProject(model);
        } else {
            project = new MavenProject();
        }
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
