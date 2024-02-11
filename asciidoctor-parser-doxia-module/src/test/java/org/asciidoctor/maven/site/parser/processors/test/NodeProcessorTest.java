package org.asciidoctor.maven.site.parser.processors.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.asciidoctor.maven.site.parser.NodeProcessor;
import org.junit.jupiter.api.extension.ExtendWith;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(JUnitNodeProcessorExtension.class)
public @interface NodeProcessorTest {

    Class<? extends NodeProcessor> value();

}
