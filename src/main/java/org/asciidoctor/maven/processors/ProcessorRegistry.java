package org.asciidoctor.maven.processors;

import org.asciidoctor.extension.Processor;

public interface ProcessorRegistry {

    /**
     * Checks if {@code processorClassName} belongs to a valid {@link Processor}
     * class and if it can be found in the classpath
     * 
     * @param processorClassName
     * @param blockName
     *            required when declaring
     * 
     */
    public abstract void register(String processorClassName, String blockName);

}