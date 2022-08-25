package org.asciidoctor.maven.extensions;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.*;
import org.asciidoctor.maven.test.processors.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class AsciidoctorJExtensionRegistryTest {

    private JavaExtensionRegistry javaExtensionRegistry;
    private AsciidoctorJExtensionRegistry pluginExtensionRegistry;


    @BeforeEach
    void beforeEach() {
        final Asciidoctor mockAsciidoctor = Mockito.mock(Asciidoctor.class);
        javaExtensionRegistry = Mockito.mock(JavaExtensionRegistry.class);
        Mockito.when(mockAsciidoctor.javaExtensionRegistry()).thenReturn(javaExtensionRegistry);
        pluginExtensionRegistry = new AsciidoctorJExtensionRegistry(mockAsciidoctor);
    }


    @Test
    void should_fail_when_not_an_extension() {
        final String className = String.class.getCanonicalName();

        Exception e = Assertions.catchException(() -> pluginExtensionRegistry.register(className, null));

        assertThat(e)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(String.format("'%s' is not a valid AsciidoctorJ processor class", className));
    }

    @Test
    void should_fail_when_extension_class_is_not_available() {
        final String className = "not.a.real.Class";

        Exception e = Assertions.catchException(() -> pluginExtensionRegistry.register(className, null));

        assertThat(e)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(String.format("'%s' not found in classpath", className));
    }

    @Test
    void should_register_a_DocinfoProcessor() {
        final Class<? extends DocinfoProcessor> clazz = MetaDocinfoProcessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, null);
        Mockito.verify(javaExtensionRegistry).docinfoProcessor(clazz);
    }

    @Test
    void should_register_a_Preprocessor() {
        final Class<? extends Preprocessor> clazz = ChangeAttributeValuePreprocessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, null);
        Mockito.verify(javaExtensionRegistry).preprocessor(clazz);
    }

    @Test
    void should_register_a_Postprocessor() {
        final Class<? extends Postprocessor> clazz = DummyPostprocessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, null);
        Mockito.verify(javaExtensionRegistry).postprocessor(clazz);
    }

    @Test
    void should_register_a_Treeprocessor() {
        final Class<? extends Treeprocessor> clazz = DummyTreeprocessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, null);
        Mockito.verify(javaExtensionRegistry).treeprocessor(clazz);
    }

    @Test
    void should_register_a_BlockProcessor() {
        final Class<? extends BlockProcessor> clazz = YellBlockProcessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, null);
        Mockito.verify(javaExtensionRegistry).block(clazz);
    }

    @Test
    void should_register_a_BlockProcessor_with_name() {
        final Class<? extends BlockProcessor> clazz = YellBlockProcessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, "block_name");
        Mockito.verify(javaExtensionRegistry).block("block_name", clazz);
    }

    @Test
    void should_register_a_IncludeProcessor() {
        final Class<? extends IncludeProcessor> clazz = UriIncludeProcessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, null);
        Mockito.verify(javaExtensionRegistry).includeProcessor(clazz);
    }

    @Test
    void should_register_a_BlockMacroProcessor() {
        final Class<? extends BlockMacroProcessor> clazz = GistBlockMacroProcessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, null);
        Mockito.verify(javaExtensionRegistry).blockMacro(clazz);
    }

    @Test
    void should_register_a_BlockMacroProcessor_with_name() {
        final Class<? extends BlockMacroProcessor> clazz = GistBlockMacroProcessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, "block_name");
        Mockito.verify(javaExtensionRegistry).blockMacro("block_name", clazz);
    }

    @Test
    void should_register_a_InlineMacroProcessor() {
        final Class<? extends InlineMacroProcessor> clazz = ManpageInlineMacroProcessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, null);
        Mockito.verify(javaExtensionRegistry).inlineMacro(clazz);
    }

    @Test
    void should_register_a_InlineMacroProcessor_with_name() {
        final Class<? extends InlineMacroProcessor> clazz = ManpageInlineMacroProcessor.class;
        final String className = clazz.getCanonicalName();

        pluginExtensionRegistry.register(className, "block_name");
        Mockito.verify(javaExtensionRegistry).inlineMacro("block_name", clazz);
    }
}
