package org.asciidoctor.maven.test;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.maven.AsciidoctorJFactory;
import org.asciidoctor.maven.AsciidoctorMojo;
import org.asciidoctor.maven.AsciidoctorOptionsFactory;
import org.asciidoctor.maven.log.LogHandler;
import org.asciidoctor.maven.process.CopyResourcesProcessor;
import org.asciidoctor.maven.process.SourceDocumentFinder;
import org.mockito.Mockito;

import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.Mockito.when;

class MojoMocker {

    private static final ParametersInitializer parametersInitializer = new ParametersInitializer();

    @SneakyThrows
    @SuppressWarnings("unchecked")
    <T> T mock(Class<T> clazz, Map<String, String> mavenProperties, LogHandler logHandler) {

        final AsciidoctorMojo mojo = (AsciidoctorMojo) clazz.getConstructor(new Class[]{}).newInstance();

        parametersInitializer.initialize(mojo);
        setVariableValueInObject(mojo, "log", new SystemStreamLog());
        setVariableValueInObject(mojo, "project", mockMavenProject(mavenProperties));
        setVariableValueInObject(mojo, "asciidoctorJFactory", new AsciidoctorJFactory());
        setVariableValueInObject(mojo, "asciidoctorOptionsFactory", new AsciidoctorOptionsFactory());
        setVariableValueInObject(mojo, "defaultResourcesProcessor", new CopyResourcesProcessor());
        setVariableValueInObject(mojo, "finder", new SourceDocumentFinder());

        if (logHandler != null)
            setVariableValueInObject(mojo, "logHandler", logHandler);

        return (T) mojo;
    }

    private MavenProject mockMavenProject(Map<String, String> mavenProperties) {
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        when(mavenProject.getBasedir()).thenReturn(new File("."));
        if (mavenProperties != null) {
            final Properties properties = new Properties();
            properties.putAll(mavenProperties);
            when(mavenProject.getProperties()).thenReturn(properties);
        }
        return mavenProject;
    }
}
