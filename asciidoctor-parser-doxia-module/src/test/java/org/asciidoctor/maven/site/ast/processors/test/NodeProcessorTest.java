package org.asciidoctor.maven.site.ast.processors.test;

import org.asciidoctor.maven.site.ast.NodeProcessor;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(JUnitNodeProcessorExtension.class)
public @interface NodeProcessorTest {

    Class<? extends NodeProcessor> value();

}
