package org.asciidoctor.maven;

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
 *
 */
class ParametersInitializer {

    private static final ClassLoader CLASS_LOADER = ParametersInitializer.class.getClassLoader();

    /**
     * Returns instance of input class with fields initialized according to its
     * respective {@link org.apache.maven.plugins.annotations.Parameter}.
     */
    public <T> T initialize(T instance) {
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
                if (field.getType().getTypeName().equals(String.class.getName())) {
                    if (value.length() > 0 && !value.startsWith("$")) {
                        // TODO support Maven variable: pass Map<String, Object> ?
                        setVariableValueInObject(instance, field.getName(), value);
                    }
                }
                if (field.getType().getTypeName().equals("boolean")) {
                    // false is already the default
                    // TODO for PR, the booleans default should appear in XML plugin descriptor now
                    if (value.equals("true")) {
                        setVariableValueInObject(instance, field.getName(), Boolean.TRUE);
                    } else if (!value.equals("false")) {
                        throw new RuntimeException("Invalid boolean default: not-a-boolean");
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
