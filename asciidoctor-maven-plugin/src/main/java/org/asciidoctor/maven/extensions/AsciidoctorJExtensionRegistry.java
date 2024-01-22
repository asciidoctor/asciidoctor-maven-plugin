package org.asciidoctor.maven.extensions;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.extension.Treeprocessor;

/**
 * Class responsible for registering extensions.
 *
 * @author abelsromero
 */
public class AsciidoctorJExtensionRegistry implements ExtensionRegistry {

    private JavaExtensionRegistry javaExtensionRegistry;

    /**
     * Constructor.
     *
     * @param asciidoctorInstance Asciidoctor instance
     */
    public AsciidoctorJExtensionRegistry(Asciidoctor asciidoctorInstance) {
        javaExtensionRegistry = asciidoctorInstance.javaExtensionRegistry();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.asciidoctor.maven.processors.ProcessorRegistry#register(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void register(String extensionClassName, String blockName) {

        Class<? extends Processor> clazz;
        try {
            clazz = (Class<? extends Processor>) Class.forName(extensionClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("'" + extensionClassName + "' not found in classpath");
        }

        if (DocinfoProcessor.class.isAssignableFrom(clazz)) {
            javaExtensionRegistry.docinfoProcessor((Class<? extends DocinfoProcessor>) clazz);
        } else if (Preprocessor.class.isAssignableFrom(clazz)) {
            javaExtensionRegistry.preprocessor((Class<? extends Preprocessor>) clazz);
        } else if (Postprocessor.class.isAssignableFrom(clazz)) {
            javaExtensionRegistry.postprocessor((Class<? extends Postprocessor>) clazz);
        } else if (Treeprocessor.class.isAssignableFrom(clazz)) {
            javaExtensionRegistry.treeprocessor((Class<? extends Treeprocessor>) clazz);
        } else if (BlockProcessor.class.isAssignableFrom(clazz)) {
            if (blockName == null) {
                javaExtensionRegistry.block((Class<? extends BlockProcessor>) clazz);
            } else {
                javaExtensionRegistry.block(blockName, (Class<? extends BlockProcessor>) clazz);
            }
        } else if (IncludeProcessor.class.isAssignableFrom(clazz)) {
            javaExtensionRegistry.includeProcessor((Class<? extends IncludeProcessor>) clazz);
        } else if (BlockMacroProcessor.class.isAssignableFrom(clazz)) {
            if (blockName == null) {
                javaExtensionRegistry.blockMacro((Class<? extends BlockMacroProcessor>) clazz);
            } else {
                javaExtensionRegistry.blockMacro(blockName, (Class<? extends BlockMacroProcessor>) clazz);
            }
        } else if (InlineMacroProcessor.class.isAssignableFrom(clazz)) {
            if (blockName == null) {
                javaExtensionRegistry.inlineMacro((Class<? extends InlineMacroProcessor>) clazz);
            } else {
                javaExtensionRegistry.inlineMacro(blockName, (Class<? extends InlineMacroProcessor>) clazz);
            }
        } else {
            throw new RuntimeException("'" + extensionClassName + "' is not a valid AsciidoctorJ processor class");
        }
    }

}
