package org.asciidoctor.maven;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.DefaultMavenFileFilter;
import org.apache.maven.shared.filtering.DefaultMavenResourcesFiltering;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.mockito.Mockito;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.Mockito.when;

public class TestUtils {

    @SneakyThrows
    public static AsciidoctorRefreshMojo newFakeRefreshMojo() {
        return mockAsciidoctorMojo(AsciidoctorRefreshMojo.class, null);
    }

    @SneakyThrows
    public static AsciidoctorMojo mockAsciidoctorMojo() {
        return mockAsciidoctorMojo(AsciidoctorMojo.class, null);
    }

    @SneakyThrows
    public static AsciidoctorMojo mockAsciidoctorMojo(Map<String, String> mavenProperties) {
        return mockAsciidoctorMojo(AsciidoctorMojo.class, mavenProperties);
    }


    @SneakyThrows
    private static <T> T mockAsciidoctorMojo(Class<T> clazz, Map<String, String> mavenProperties) {
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        when(mavenProject.getBasedir()).thenReturn(new File("."));
        if (mavenProperties != null) {
            final Properties properties = new Properties();
            properties.putAll(mavenProperties);
            when(mavenProject.getProperties()).thenReturn(properties);
        }

        final BuildContext buildContext = new DefaultBuildContext();

        final DefaultMavenFileFilter mavenFileFilter = new DefaultMavenFileFilter();
        final ConsoleLogger plexusLogger = new ConsoleLogger();
        mavenFileFilter.enableLogging(plexusLogger);
        setVariableValueInObject(mavenFileFilter, "buildContext", buildContext);

        final DefaultMavenResourcesFiltering resourceFilter = new DefaultMavenResourcesFiltering();
        setVariableValueInObject(resourceFilter, "mavenFileFilter", mavenFileFilter);
        setVariableValueInObject(resourceFilter, "buildContext", buildContext);
        resourceFilter.initialize();
        resourceFilter.enableLogging(plexusLogger);

        final AsciidoctorMojo mojo = (AsciidoctorMojo) clazz.getConstructor(new Class[]{}).newInstance();
        setVariableValueInObject(mojo, "log", new SystemStreamLog());
        mojo.encoding = "UTF-8";
        mojo.project = mavenProject;
        mojo.outputResourcesFiltering = resourceFilter;

        return (T) mojo;
    }

    public static <T> Map<String, T> map(String key1, T value1) {
        return Collections.singletonMap(key1, value1);
    }

    public static Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static Map<String, Object> map(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    @SneakyThrows
    public static String readAsString(File file) {
        return IOUtils.toString(new FileReader(file));
    }

    @SneakyThrows
    public static void writeToFile(File parent, String filename, String... lines) {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(parent, filename));
        for (String line : lines) {
            IOUtils.write(line, fileOutputStream, StandardCharsets.UTF_8);
        }
    }

    public static class ResourceBuilder {
        private final Resource resource = new Resource();

        public ResourceBuilder directory(String directory) {
            resource.setDirectory(directory);
            return this;
        }

        public ResourceBuilder includes(String... includes) {
            resource.setIncludes(mutableList(includes));
            return this;
        }

        public ResourceBuilder excludes(String... excludes) {
            resource.setExcludes(mutableList(excludes));
            return this;
        }

        private List<String> mutableList(String[] includes) {
            List<String> list = new ArrayList<>();
            for (String include : includes) {
                list.add(include);
            }
            return list;
        }

        public ResourceBuilder targetPath(String targetPath) {
            resource.setTargetPath(targetPath);
            return this;
        }

        public Resource build() {
            return resource;
        }

        public static List<Resource> excludeAll() {
            return singletonList(new ResourceBuilder().directory(".").excludes("**/**").build());
        }
    }

}
