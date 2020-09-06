package org.asciidoctor.maven;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.maven.http.AsciidoctorHttpServer;
import org.asciidoctor.maven.io.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

@Mojo(name = "http")
public class AsciidoctorHttpMojo extends AsciidoctorRefreshMojo {

    public static final String PREFIX = AsciidoctorMaven.PREFIX + "http.";

    @Parameter(property = PREFIX + "port")
    protected int port = 2000;

    @Parameter(property = PREFIX + "home", defaultValue = "index")
    protected String home;

    @Parameter(property = PREFIX + "reload-interval", defaultValue = "0")
    protected int autoReloadInterval;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final AsciidoctorHttpServer server = new AsciidoctorHttpServer(getLog(), port, outputDirectory, home);
        server.start();
        doWork();
        doWait();
        server.stop();
    }

    @Override
    protected void convertFile(final Asciidoctor asciidoctorInstance, final Map<String, Object> options, final File f) {
        asciidoctorInstance.convertFile(f, options);
        logConvertedFile(f);

        if (autoReloadInterval > 0 && backend.toLowerCase().startsWith("html")) {
            final String filename = f.getName();
            final File out = new File(outputDirectory, filename.substring(0, filename.lastIndexOf(".")) + ".html");
            if (out.exists()) {

                String content = null;

                { // read
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(out); // java asciidoctor render() doesn't work ATM so read the converted file instead of doing it in memory
                        content = IO.slurp(fis);
                    } catch (final Exception e) {
                        getLog().error(e);
                    } finally {
                        IOUtils.closeQuietly(fis);
                    }
                }

                if (content != null) { // convert + write
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(out);
                        fos.write(addRefreshing(content).getBytes());
                    } catch (final Exception e) {
                        getLog().error(e);
                    } finally {
                        IOUtils.closeQuietly(fos);
                    }
                }
            }
        }
    }

    private String addRefreshing(final String html) {
        return html.replace("</body>", "<script>setTimeout(\"location.reload(true);\"," + autoReloadInterval + ");</script>\n</body>");
    }

    public String getHome() {
        return home;
    }

    public void setHome(final String home) {
        this.home = home;
    }
}
