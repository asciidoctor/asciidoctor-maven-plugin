package org.asciidoctor.maven.extensions;

/**
 * Holds an extension's configuration parameters in the pom.xml
 *
 * @author abelsromero
 */
public class ExtensionConfiguration {

    /**
     * Fully qualified name of the extension
     */
    private String className;

    /**
     * Optional. Block name in case of setting a Block, BlockMacro or
     * InlineMacro processor
     */
    private String blockName;

    public ExtensionConfiguration() {
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
