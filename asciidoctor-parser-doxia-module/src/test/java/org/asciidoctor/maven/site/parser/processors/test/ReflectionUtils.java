package org.asciidoctor.maven.site.parser.processors.test;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtils {

    public static StringWriter extractField(Object sink, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = sink.getClass().getDeclaredField(fieldName);
        // We don't care to alter the instance, only lives during the test
        field.setAccessible(true);
        return (StringWriter) field.get(sink);
    }

    public static Field findField(Object testInstance, Class<?> clazz) {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (clazz.equals(field.getType())) return field;
        }
        return null;
    }

    public static void injectField(Object testInstance, Field field, Object value) throws IllegalAccessException {

        if (Modifier.isPrivate(field.getModifiers())) {
            field.setAccessible(true);
            field.set(testInstance, value);
            field.setAccessible(false);
        } else {
            field.set(testInstance, value);
        }
    }
}
