package org.asciidoctor.maven.extensions;

import org.asciidoctor.extension.Processor;

/**
 * Base interface for registering AsciidoctorJ extension in the plugin.
 *
 * @author abelsromero
 */
public interface ExtensionRegistry {

    /**
     * Registers an AsciidoctorJ extension by full class name.
     *
     * @param extensionClassName fully qualified name of the class implementing the extension
     * @param blockName          required when declaring
     * @throws RuntimeException if {@code extensionClassName} belongs to a valid {@link Processor},
     *                          class, or if it can be found in the classpath
     */
    void register(String extensionClassName, String blockName);

}
