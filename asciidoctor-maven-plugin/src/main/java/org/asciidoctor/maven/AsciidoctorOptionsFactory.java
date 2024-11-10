package org.asciidoctor.maven;

import javax.inject.Singleton;
import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.maven.commons.AsciidoctorHelper;

import static org.asciidoctor.maven.commons.StringUtils.isBlank;
import static org.asciidoctor.maven.commons.StringUtils.isNotBlank;

/**
 * Creates an {@link OptionsBuilder} instance taking into consideration the project's
 * configurations to be used or further customized.
 * The instance also contains initialized {@link Attributes}.
 *
 * @author abelsromero
 * @since 3.1.1
 */
@Singleton
public class AsciidoctorOptionsFactory {

    /**
     * Creates an AttributesBuilder instance with the attributes defined in the configuration.
     *
     * @param configuration AsciidoctorMojo containing conversion configuration.
     * @param mavenProject  Current {@link MavenProject} instance.
     * @param log           The mojo's {@link Log} reference.
     * @return initialized {@link Attributes}.
     */
    private Attributes createAttributes(AsciidoctorMojo configuration, MavenProject mavenProject, Log log) {

        final AttributesBuilder attributesBuilder = Attributes.builder();

        if (configuration.isEmbedAssets()) {
            attributesBuilder.linkCss(false);
            attributesBuilder.dataUri(true);
        }

        AsciidoctorHelper.addProperties(mavenProject.getProperties(), attributesBuilder);
        AsciidoctorHelper.addAttributes(configuration.getAttributes(), attributesBuilder);

        if (isNotBlank(configuration.getAttributesChain())) {
            log.info("Attributes: " + configuration.getAttributesChain());
            attributesBuilder.arguments(configuration.getAttributesChain());
        }

        return attributesBuilder.build();
    }

    /**
     * Creates an OptionsBuilder instance with the options defined in the configuration.
     *
     * @param configuration AsciidoctorMojo containing conversion configuration.
     * @param mavenProject  Current {@link MavenProject} instance.
     * @param log           The mojo's {@link Log} reference.
     * @return initialized optionsBuilder.
     */
    OptionsBuilder create(AsciidoctorMojo configuration, MavenProject mavenProject, Log log) {

        final OptionsBuilder optionsBuilder = Options.builder()
            .backend(configuration.getBackend())
            .safe(SafeMode.UNSAFE)
            .standalone(configuration.standalone)
            .mkDirs(true);

        if (!isBlank(configuration.getEruby()))
            optionsBuilder.eruby(configuration.getEruby());

        if (configuration.isSourcemap())
            optionsBuilder.option(Options.SOURCEMAP, true);

        if (configuration.isCatalogAssets())
            optionsBuilder.option(Options.CATALOG_ASSETS, true);

        if (!configuration.isTemplateCache())
            optionsBuilder.option(Options.TEMPLATE_CACHE, false);

        if (configuration.getDoctype() != null)
            optionsBuilder.docType(configuration.getDoctype());

        if (configuration.getTemplateEngine() != null)
            optionsBuilder.templateEngine(configuration.getTemplateEngine());

        if (!configuration.getTemplateDirs().isEmpty())
            optionsBuilder.templateDirs(configuration.getTemplateDirs().toArray(new File[]{}));

        final Attributes attributes = createAttributes(configuration, mavenProject, log);
        return optionsBuilder.attributes(attributes);
    }

}
