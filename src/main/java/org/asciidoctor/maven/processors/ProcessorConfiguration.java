package org.asciidoctor.maven.processors;

import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.maven.AsciidoctorMaven;

/**
 * Holds a processor's configuration parameters in the pom.xml
 * 
 * @author abelsromero
 */
public class ProcessorConfiguration {

    public static final String PREFIX = AsciidoctorMaven.PREFIX + "processor.";
    
    /**
     * Fully qualified name of the processor
     */
    @Parameter(property = PREFIX + "className", required = true)
    private String className;

    /**
     * Optional. Block name in case of setting a Block, BlockMacro or
     * InlineMacro processor
     */
    @Parameter(property = PREFIX + "blockName")
    private String blockName;

    public ProcessorConfiguration() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

}
