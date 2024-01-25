package org.asciidoctor.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.maven.http.AsciidoctorHttpServer;

@Mojo(name = "http")
public class AsciidoctorHttpMojo extends AsciidoctorRefreshMojo {

    public static final String PREFIX = AsciidoctorMaven.PREFIX + "http.";

    @Parameter(property = PREFIX + "port", defaultValue = "2000")
    protected int port;

    @Parameter(property = PREFIX + "home", defaultValue = "index")
    protected String home;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        final AsciidoctorHttpServer server = new AsciidoctorHttpServer(getLog(), port, outputDirectory, home);

        startPolling();
        server.start();
        doWork();
        doWait();
        server.stop();
        stopMonitors();
    }

    public String getHome() {
        return home;
    }

    public void setHome(final String home) {
        this.home = home;
    }
}
