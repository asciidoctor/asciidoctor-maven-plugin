package org.asciidoctor.maven.test;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.pool.TypePool;
import org.apache.maven.plugins.annotations.Parameter;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;

/**
 * Initialize values for properties in class using {@link Parameter} annotation.
 */
class ParametersInitializer {

    private static final ClassLoader CLASS_LOADER = ParametersInitializer.class.getClassLoader();

    /**
     * Returns instance of input class with fields initialized according to its
     * respective {@link org.apache.maven.plugins.annotations.Parameter}.
     */
    <T> T initialize(T instance) {
        try {
            // Use ByteBuddy because annotations is Class retention, not Runtime
            TypePool typePool = TypePool.Default.of(CLASS_LOADER);
            TypeDescription typeDescription = typePool.describe(instance.getClass().getName()).resolve();

            initParameterFields(instance, typeDescription);
            return instance;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void initParameterFields(T instance, TypeDescription typeDescription) throws IllegalAccessException {

        FieldList<FieldDescription.InDefinedShape> declaredFields = typeDescription.getDeclaredFields();

        for (FieldDescription field : declaredFields) {
            String value = getAnnotationByType(field);
            if (value != null) {
                final String typeName = field.getType().getTypeName();
                if (typeName.equals(String.class.getName())) {
                    if (value.length() > 0 && !value.startsWith("$")) {
                        // TODO support Maven variable: pass Map<String, Object> ?
                        setVariableValueInObject(instance, field.getName(), value);
                    }
                }
                if (typeName.equals("boolean")) {
                    if (value.equals("true")) {
                        setVariableValueInObject(instance, field.getName(), Boolean.TRUE);
                    } else if (!value.equals("false")) {
                        throw new RuntimeException("Invalid boolean default: " + value);
                    }
                }
                if (typeName.equals("int")) {
                    try {
                        setVariableValueInObject(instance, field.getName(), Integer.valueOf(value));
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid boolean default: " + value);
                    }
                }
                // TODO
                // if (field.getType().getTypeName().equals(File.class.getName())) {
            }
        }

        TypeDescription superClass = typeDescription.getSuperClass().asErasure();

        if (hasParent(superClass)) {
            initParameterFields(instance, superClass);
        }
    }

    private boolean hasParent(TypeDescription superClass) {
        return superClass != null && !superClass.getTypeName().equals(Object.class.getName());
    }

    // Make MojoReader
    private String getAnnotationByType(FieldDescription field) {
        for (AnnotationDescription declaredAnnotation : field.getDeclaredAnnotations()) {
            String annotationTypeName = declaredAnnotation.getAnnotationType().getName();
            if (annotationTypeName.equals(Parameter.class.getCanonicalName())) {
                AnnotationValue<?, ?> defaultValue = declaredAnnotation.getValue("defaultValue");
                String stringValue = defaultValue.toString();
                stringValue = stringValue.substring(1, stringValue.length() - 1).trim();
                return isBlank(stringValue) ? null : stringValue;
            }
        }
        return null;
    }
}
