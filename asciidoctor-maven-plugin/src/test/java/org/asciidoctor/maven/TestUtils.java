package org.asciidoctor.maven;

import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.pool.TypePool;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.model.Resource;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.asciidoctor.maven.process.SourceDocumentFinder.STANDARD_FILE_EXTENSIONS_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatPath;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.Mockito.when;

public class TestUtils {

    private static final ParametersInitializer parametersInitializer = new ParametersInitializer();

    @SneakyThrows
    public static AsciidoctorRefreshMojo newFakeRefreshMojo() {
        return mockAsciidoctorMojo(AsciidoctorRefreshMojo.class, null, null);
    }

    @SneakyThrows
    public static AsciidoctorMojo mockAsciidoctorMojo() {
        return mockAsciidoctorMojo(AsciidoctorMojo.class, null, null);
    }

    @SneakyThrows
    public static AsciidoctorHttpMojo mockAsciidoctorHttpMojo() {
        return mockAsciidoctorMojo(AsciidoctorHttpMojo.class, null, null);
    }

    @SneakyThrows
    public static AsciidoctorZipMojo mockAsciidoctorZipMojo() {
        return mockAsciidoctorMojo(AsciidoctorZipMojo.class, null, null);
    }

    @SneakyThrows
    public static AsciidoctorMojo mockAsciidoctorMojo(Map<String, String> mavenProperties) {
        return mockAsciidoctorMojo(AsciidoctorMojo.class, mavenProperties, null);
    }

    @SneakyThrows
    public static AsciidoctorMojo mockAsciidoctorMojo(LogHandler logHandler) {
        return mockAsciidoctorMojo(AsciidoctorMojo.class, null, logHandler);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static <T> T mockAsciidoctorMojo(Class<T> clazz, Map<String, String> mavenProperties, LogHandler logHandler) {
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        when(mavenProject.getBasedir()).thenReturn(new File("."));
        if (mavenProperties != null) {
            final Properties properties = new Properties();
            properties.putAll(mavenProperties);
            when(mavenProject.getProperties()).thenReturn(properties);
        }

        final AsciidoctorMojo mojo = (AsciidoctorMojo) clazz.getConstructor(new Class[]{}).newInstance();
        parametersInitializer.initialize(mojo);
        setVariableValueInObject(mojo, "log", new SystemStreamLog());
        mojo.project = mavenProject;
        if (logHandler != null)
            setVariableValueInObject(mojo, "logHandler", logHandler);

        return (T) mojo;
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

    /**
     * Validates that the folder structures under certain files contain the same
     * directories and file names.
     *
     * @param expected list of expected folders
     * @param actual   list of actual folders (the ones to validate)
     */
    public static void assertEqualsStructure(File[] expected, File[] actual) {

        List<File> sanitizedExpected = Arrays.stream(expected)
                .filter(TestUtils::isHidden)
                .collect(Collectors.toList());

        List<String> expectedNames = sanitizedExpected.stream().map(File::getName).collect(Collectors.toList());
        List<String> actualNames = Arrays.stream(actual).map(File::getName).collect(Collectors.toList());
        assertThat(expectedNames).containsExactlyInAnyOrder(actualNames.toArray(new String[]{}));

        for (File actualFile : actual) {
            File expectedFile = sanitizedExpected.stream()
                    .filter(f -> f.getName().equals(actualFile.getName()))
                    .findFirst()
                    .get();

            // check that at least the number of html files and asciidoc are the same in each folder
            File[] expectedChildren = Arrays.stream(expectedFile.listFiles(File::isDirectory))
                    .filter(f -> !f.getName().startsWith("_"))
                    .toArray(File[]::new);

            File[] htmls = actualFile.listFiles(f -> f.getName().endsWith("html"));
            if (htmls.length > 0) {
                File[] asciiDocs = expectedFile.listFiles(f -> f.getName().matches(STANDARD_FILE_EXTENSIONS_PATTERN));
                assertThat(htmls).hasSize(asciiDocs.length);
            }
            File[] actualChildren = actualFile.listFiles(File::isDirectory);
            assertEqualsStructure(expectedChildren, actualChildren);
        }
    }

    private static boolean isHidden(File file) {
        char firstChar = file.getName().charAt(0);
        return firstChar != '_' && firstChar != '.';
    }
}
