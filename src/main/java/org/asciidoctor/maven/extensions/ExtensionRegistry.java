package org.asciidoctor.maven.extensions;

import org.asciidoctor.extension.Processor;

public interface ExtensionRegistry {

    /**
     * Checks if {@code extensionClassName} belongs to a valid {@link Processor}
     * class and if it can be found in the classpath
     * 
     * @param extensionClassName
     * @param blockName
     *            required when declaring
     * 
     */
    public abstract void register(String extensionClassName, String blockName);

}