package org.asciidoctor.maven.test;

import org.apache.maven.plugins.annotations.Parameter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ParametersInitializerTest {

    private final ParametersInitializer initializer = new ParametersInitializer();

    @Test
    void should_return_same_instance() {
        final var instance = new Simple();
        var actual = initializer.initialize(instance);
        assertThat(actual).isEqualTo(instance);
    }

    @Nested
    class ShouldInitialize {

        @Test
        void string_with_default_value() {
            final var instance = new StringExampleMojo();
            var initialized = initializer.initialize(instance);
            assertThat(initialized.defaultValue).isEqualTo("a-value");
        }

        @Test
        void boolean_with_default_value() {
            final var instance = new BooleanExampleMojo();
            var initialized = initializer.initialize(instance);
            assertThat(initialized.defaultValue).isTrue();
        }

        @Test
        void int_with_default_value() {
            final var instance = new IntExampleMojo();
            var initialized = initializer.initialize(instance);
            assertThat(initialized.defaultValue).isEqualTo(1243);
        }

        @Test
        void properties_in_class_and_parent() {
            final var instance = new SubclassExampleMojo();
            var initialized = initializer.initialize(instance);
            assertThat(initialized.getDefaultValue()).isEqualTo("a-value");
            assertThat(initialized.getNonDefaultValue()).isNull();
            assertThat(initialized.anotherValue).isEqualTo("from-subclass");
        }
    }

    @Nested
    class ShouldNotInitialize {

        @Test
        void string_without_default_value() {
            final var instance = new StringExampleMojo();
            var initialized = initializer.initialize(instance);
            assertThat(initialized.nonDefaultValue).isNull();
        }

        @Test
        void boolean_without_default_value() {
            final var instance = new BooleanExampleMojo();
            var initialized = initializer.initialize(instance);
            assertThat(initialized.nonDefaultValue).isFalse();
        }

        @Test
        void int_without_default_value() {
            final var instance = new IntExampleMojo();
            var initialized = initializer.initialize(instance);
            assertThat(initialized.nonDefaultValue).isEqualTo(0);
        }
    }

    @Nested
    class ShouldFail {

        @Test
        void boolean_with_invalid_value() {
            final var instance = new FailingBooleanExampleMojo();
            Throwable t = catchThrowable(() -> initializer.initialize(instance));

            assertThat(t).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid boolean default: not-a-boolean");
        }

        @Test
        void int_with_invalid_value() {
            final var instance = new FailingIntExampleMojo();
            Throwable t = catchThrowable(() -> initializer.initialize(instance));

            assertThat(t).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid boolean default: not-an-int");
        }
    }


    class Simple {

        Simple() {
        }

    }

    class StringExampleMojo {

        @Parameter(defaultValue = "a-value")
        private String defaultValue;

        @Parameter
        private String nonDefaultValue;

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getNonDefaultValue() {
            return nonDefaultValue;
        }
    }

    class BooleanExampleMojo {

        @Parameter(defaultValue = "true")
        private boolean defaultValue;

        @Parameter
        private boolean nonDefaultValue;
    }

    class IntExampleMojo {

        @Parameter(defaultValue = "1243")
        private int defaultValue;

        @Parameter
        private int nonDefaultValue;
    }

    class FailingBooleanExampleMojo {

        @Parameter(defaultValue = "not-a-boolean")
        private boolean invalidValue;
    }

    class FailingIntExampleMojo {

        @Parameter(defaultValue = "not-an-int")
        private int invalidValue;
    }

    class SubclassExampleMojo extends StringExampleMojo {

        @Parameter(defaultValue = "from-subclass")
        private String anotherValue;
    }
}
