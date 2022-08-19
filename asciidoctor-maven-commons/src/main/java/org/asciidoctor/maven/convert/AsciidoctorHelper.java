package org.asciidoctor.maven.convert;

import org.apache.maven.project.MavenProject;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;

import java.util.Map;

/**
 * Utility class for re-usable logic.
 */
public class AsciidoctorHelper {

    /**
     * Adds attributes from a {@link Map} into a {@link AttributesBuilder} taking care of Maven's XML parsing special
     * cases like toggles, nulls, etc.
     *
     * @param attributes        map of Asciidoctor attributes
     * @param attributesBuilder AsciidoctorJ AttributesBuilder
     */
    public static void addAttributes(final Map<String, Object> attributes, AttributesBuilder attributesBuilder) {
        // TODO Figure out how to reliably set other values (like boolean values, dates, times, etc)
        for (Map.Entry<String, Object> attributeEntry : attributes.entrySet()) {
            addAttribute(attributeEntry.getKey(), attributeEntry.getValue(), attributesBuilder);
        }
    }

    /**
     * Adds properties from the {@link MavenProject} into a {@link AttributesBuilder} taking care of Maven's XML parsing special
     * cases like toggles, nulls, etc.
     *
     * @param project           Maven project
     * @param attributesBuilder AsciidoctorJ AttributesBuilder
     */
    public static void addMavenProperties(MavenProject project, AttributesBuilder attributesBuilder) {
        if (project.getProperties() != null) {
            for (Map.Entry<Object, Object> entry : project.getProperties().entrySet()) {
                attributesBuilder.attribute(((String) entry.getKey()).replaceAll("\\.", "-"), entry.getValue());
            }
        }
    }

    /**
     * Adds an attribute into a {@link AttributesBuilder} taking care of Maven's XML parsing special cases like
     * toggles, nulls, etc.
     *
     * @param attribute         Asciidoctor attribute name
     * @param value             Asciidoctor attribute value
     * @param attributesBuilder AsciidoctorJ AttributesBuilder
     */
    public static void addAttribute(String attribute, Object value, AttributesBuilder attributesBuilder) {
        // NOTE Maven interprets an empty value as null, so we need to explicitly convert it to empty string (see #36)
        // NOTE In Asciidoctor, an empty string represents a true value
        if (value == null || "true".equals(value)) {
            attributesBuilder.attribute(attribute, "");
        }
        // NOTE a value of false is effectively the same as a null value, so recommend the use of the string "false"
        else if ("false".equals(value)) {
            attributesBuilder.attribute(attribute, null);
        }
        // NOTE Maven can't assign a Boolean value from the XML-based configuration, but a client may
        else if (value instanceof Boolean) {
            attributesBuilder.attribute(attribute, Attributes.toAsciidoctorFlag((Boolean) value));
        } else {
            // Can't do anything about dates and times because all that logic is private in Attributes
            attributesBuilder.attribute(attribute, value);
        }
    }

}
