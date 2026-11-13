package org.asciidoctor.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.asciidoctor.maven.io.TestFilesHelper.newOutputTestDirectory;
import static org.asciidoctor.maven.test.TestUtils.mockAsciidoctorMojo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.log.Severity;
import org.asciidoctor.maven.log.FailIf;
import org.asciidoctor.maven.log.LogHandler;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * Verifies that the {@link Asciidoctor} instance created for a conversion is released afterward,
 * so that the JRuby runtime and the mojo (and with it the Maven project and session) can be
 * garbage collected in long-lived JVMs like the Maven Daemon (mvnd).
 */
class AsciidoctorMojoInstanceLifecycleTest {

    private static final String DEFAULT_SOURCE_DIRECTORY = "target/test-classes/src/asciidoctor";

    @Test
    void should_unregister_logHandler_and_close_asciidoctor_after_conversion() throws Exception {
        // setup
        final RecordingAsciidoctorJFactory factory = new RecordingAsciidoctorJFactory();
        final AsciidoctorMojo mojo = mockAsciidoctorMojo();
        setVariableValueInObject(mojo, "asciidoctorJFactory", factory);
        mojo.backend = "html";
        mojo.sourceDirectory = new File(DEFAULT_SOURCE_DIRECTORY);
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.outputDirectory = newOutputTestDirectory("instance-lifecycle");

        // when
        mojo.execute();

        // then
        assertThat(factory.instances).hasSize(1);
        assertReleased(factory.instances.get(0));
    }

    @Test
    void should_unregister_logHandler_and_close_asciidoctor_when_conversion_fails() throws Exception {
        // setup
        final LogHandler logHandler = new LogHandler();
        final FailIf failIf = new FailIf();
        failIf.setSeverity(Severity.WARN);
        logHandler.setFailIf(failIf);

        final RecordingAsciidoctorJFactory factory = new RecordingAsciidoctorJFactory();
        final AsciidoctorMojo mojo = mockAsciidoctorMojo(logHandler);
        setVariableValueInObject(mojo, "asciidoctorJFactory", factory);
        mojo.backend = "html";
        mojo.sourceDirectory = new File(DEFAULT_SOURCE_DIRECTORY);
        mojo.sourceDocumentName = "errors/document-with-missing-include.adoc";
        mojo.outputDirectory = newOutputTestDirectory("instance-lifecycle");

        // when
        Throwable throwable = catchThrowable(mojo::execute);

        // then
        assertThat(throwable).isInstanceOf(MojoExecutionException.class);
        assertThat(factory.instances).hasSize(1);
        assertReleased(factory.instances.get(0));
    }

    @Test
    void should_use_a_new_asciidoctor_instance_for_each_execution() throws Exception {
        // setup
        final RecordingAsciidoctorJFactory factory = new RecordingAsciidoctorJFactory();
        final AsciidoctorMojo mojo = mockAsciidoctorMojo();
        setVariableValueInObject(mojo, "asciidoctorJFactory", factory);
        mojo.backend = "html";
        mojo.sourceDirectory = new File(DEFAULT_SOURCE_DIRECTORY);
        mojo.sourceDocumentName = "sample.asciidoc";
        mojo.outputDirectory = newOutputTestDirectory("instance-lifecycle");

        // when: executed twice, as the auto-refresh mojo does on every refresh
        mojo.execute();
        mojo.execute();

        // then
        assertThat(factory.instances).hasSize(2);
        assertThat(factory.instances.get(0)).isNotSameAs(factory.instances.get(1));
        factory.instances.forEach(AsciidoctorMojoInstanceLifecycleTest::assertReleased);
    }

    private static void assertReleased(Asciidoctor asciidoctor) {
        final InOrder inOrder = inOrder(asciidoctor);
        inOrder.verify(asciidoctor).registerLogHandler(any());
        inOrder.verify(asciidoctor).unregisterLogHandler(any());
        inOrder.verify(asciidoctor).close();
    }

    /**
     * Creates real {@link Asciidoctor} instances wrapped in Mockito spies to verify their usage.
     */
    static class RecordingAsciidoctorJFactory extends AsciidoctorJFactory {

        final List<Asciidoctor> instances = new ArrayList<>();

        @Override
        Asciidoctor create(String gemPath, Log log) throws MojoExecutionException {
            final Asciidoctor asciidoctor = spy(super.create(gemPath, log));
            instances.add(asciidoctor);
            return asciidoctor;
        }
    }
}
