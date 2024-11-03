package org.asciidoctor.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.jruby.internal.JRubyRuntimeContext;
import org.jruby.Ruby;

import javax.inject.Singleton;

@Singleton
public class AsciidoctorJFactory {

    Asciidoctor create(String gemPath, Log log) throws MojoExecutionException {
        Asciidoctor asciidoctor;
        if (gemPath == null) {
            asciidoctor = AsciidoctorJRuby.Factory.create();
        } else {
            // Replace Windows path separator to avoid paths with mixed \ and /.
            // This happens for instance when setting: <gemPath>${project.build.directory}/gems-provided</gemPath>
            // because the project's path is converted to string.
            String normalizedGemPath = (File.separatorChar == '\\') ? gemPath.replaceAll("\\\\", "/") : gemPath;
            asciidoctor = AsciidoctorJRuby.Factory.create(normalizedGemPath);
        }

        Ruby rubyInstance = null;
        try {
            rubyInstance = (Ruby) JRubyRuntimeContext.class.getMethod("get")
                .invoke(null);
        } catch (NoSuchMethodException e) {
            if (rubyInstance == null) {
                try {
                    rubyInstance = (Ruby) JRubyRuntimeContext.class.getMethod("get", Asciidoctor.class).invoke(null, asciidoctor);
                } catch (Exception e1) {
                    throw new MojoExecutionException("Failed to invoke get(AsciiDoctor) for JRubyRuntimeContext", e1);
                }

            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to invoke get for JRubyRuntimeContext", e);
        }

        String gemHome = rubyInstance.evalScriptlet("ENV['GEM_HOME']").toString();
        String gemHomeExpected = (gemPath == null || "".equals(gemPath)) ? "" : gemPath.split(java.io.File.pathSeparator)[0];

        if (!"".equals(gemHome) && !gemHomeExpected.equals(gemHome)) {
            log.warn("Using inherited external environment to resolve gems (" + gemHome + "), i.e. build is platform dependent!");
        }

        return asciidoctor;
    }
}
