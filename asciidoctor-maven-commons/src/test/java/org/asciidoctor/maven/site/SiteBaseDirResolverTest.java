package org.asciidoctor.maven.site;

import java.io.File;
import java.io.StringReader;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SiteBaseDirResolverTest {

    private static final File BASE_DIR = new File(".");

    @Test
    void should_resolve_default_value() {
        final Xpp3Dom siteConfiguration = buildXpp3Dom(Map.of());

        SiteBaseDirResolver resolver = new SiteBaseDirResolver();
        File result = resolver.resolveBaseDir(BASE_DIR, siteConfiguration);

        assertThat(result.toString()).isEqualTo("src/site");
    }

    @ParameterizedTest
    @ValueSource(strings = {".", "something", "something/"})
    void should_resolve_from_siteDirectory(String mavenBaseDir) {
        final String siteDirectory = "./my-src/my-site";
        final Xpp3Dom siteConfiguration = buildXpp3Dom(Map.of("siteDirectory", siteDirectory));

        SiteBaseDirResolver resolver = new SiteBaseDirResolver();
        File result = resolver.resolveBaseDir(new File(mavenBaseDir), siteConfiguration);

        assertThat(result.toString()).isEqualTo(mavenBaseDir.equals(".") ? "my-src/my-site" : "something/my-src/my-site");
    }

    @Test
    void should_resolve_from_locale() {
        final String locale = "es";
        final Xpp3Dom siteConfiguration = buildXpp3Dom(Map.of("locales", locale));

        SiteBaseDirResolver resolver = new SiteBaseDirResolver();
        File result = resolver.resolveBaseDir(BASE_DIR, siteConfiguration);

        assertThat(result.toString()).isEqualTo("src/site/es");
    }

    @Test
    void should_resolve_from_siteDirectory_and_locale() {
        final String siteDirectory = "./my-src/my-site";
        final String locale = "es";
        final Xpp3Dom siteConfiguration = buildXpp3Dom(Map.of(
            "siteDirectory", siteDirectory,
            "locales", locale
        ));

        SiteBaseDirResolver resolver = new SiteBaseDirResolver();
        File result = resolver.resolveBaseDir(new File("some"), siteConfiguration);

        assertThat(result.toString()).isEqualTo("some/my-src/my-site/es");
    }

    @SneakyThrows
    private javax.inject.Provider<MavenProject> createMavenProjectMock() {
        MavenProject mockProject = Mockito.mock(MavenProject.class);
        when(mockProject.getBasedir()).thenReturn(new File("."));
        return () -> mockProject;
    }

    @SneakyThrows
    private static Xpp3Dom buildXpp3Dom(Map<String, String> config) {
        String configurationsXml = config.keySet()
            .stream()
            .map(k -> String.format("<%1$s>%2$s</%1$s>", k, config.get(k)))
            .collect(Collectors.joining());
        return Xpp3DomBuilder.build(new StringReader(String.format("<configuration>%s</configuration>", configurationsXml)));
    }

}
