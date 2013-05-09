/*
 * Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidoc.maven;

<<<<<<< HEAD
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.asciidoc.maven.http.AsciidoctorHttpServer;
import org.asciidoctor.Asciidoctor;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Mojo(name = "http")
public class AsciidoctorHttpMojo extends AsciidoctorMojo {
    // copied from org.asciidoctor.AsciiDocDirectoryWalker.ASCIIDOC_REG_EXP_EXTENSION
    // should probably be configured in AsciidoctorMojo through @Parameter 'extension'
    private static final String ASCIIDOC_REG_EXP_EXTENSION = ".*\\.a((sc(iidoc)?)|d(oc)?)$";

    @Parameter(property = AsciidoctorMaven.PREFIX + "port", required = false)
    protected int port = 2000;

    @Parameter(property = AsciidoctorMaven.PREFIX + "interval", required = false)
    protected int interval = 2000; // 2s

    private Future<Asciidoctor> asciidoctor = null;
    private FileAlterationMonitor monitor = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // this is long because of JRuby startup
        createAsciidoctor();

        // start pooling
        startPolling();

        // and finally start server
        final AsciidoctorHttpServer server = new AsciidoctorHttpServer(getLog(), port, outputDirectory);
        server.start();

        // execute once to be able to server something
        doExecute();

        // wait for exit
        getLog().info("Type [Enter] to exit");
        new Scanner(System.in).nextLine();

        // stop all
        server.stop();
        stopMonitor();
    }

    private void stopMonitor() throws MojoExecutionException {
        if (monitor != null) {
            try {
                monitor.stop();
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private synchronized void doExecute() {
        try {
            FileUtils.deleteDirectory(outputDirectory);
        } catch (final IOException e) {
            // no-op: swallow it, shouldn't be a big deal, just not as clean as it should in some cases
        }

        try {
            final long start = System.nanoTime();
            super.execute();
            final long end = System.nanoTime();
            getLog().info("Re-rendered doc in " + TimeUnit.NANOSECONDS.toMillis(end - start) + "ms");
        } catch (final MojoExecutionException e) {
            getLog().error(e);
        } catch (final MojoFailureException e) {
            getLog().error(e);
        }
    }

    private void startPolling() throws MojoExecutionException {
        final FileAlterationObserver observer;
        if (sourceDirectory != null) {
            observer = new FileAlterationObserver(sourceDirectory, new RegexFileFilter(ASCIIDOC_REG_EXP_EXTENSION));
        } else if (sourceDocumentName != null) {
            observer = new FileAlterationObserver(sourceDocumentName.getParentFile(), new NameFileFilter(sourceDocumentName.getName()));
        } else {
            return;
        }

        monitor = new FileAlterationMonitor(interval);
        final FileAlterationListener listener = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(final File file) {
                getLog().info("File " + file.getAbsolutePath() + " created.");
                doExecute();
            }

            @Override
            public void onFileChange(final File file) {
                getLog().info("File " + file.getAbsolutePath() + " updated.");
                doExecute();
            }

            @Override
            public void onFileDelete(final File file) {
                getLog().info("File " + file.getAbsolutePath() + " deleted.");
                doExecute();
            }
        };

        observer.addListener(listener);
        monitor.addObserver(observer);
        try {
            monitor.start();
        } catch (final Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void createAsciidoctor() {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        asciidoctor = es.submit(new Callable<Asciidoctor>() {
            @Override
            public Asciidoctor call() throws Exception {
                return Asciidoctor.Factory.create();
            }
        });
        es.shutdown();
    }

    @Override
    protected Asciidoctor getAsciidoctorInstance() throws MojoExecutionException {
        try {
            return asciidoctor.get();
        } catch (final Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
=======
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.asciidoc.maven.http.AsciidoctorHttpServer;

@Mojo(name = "http")
public class AsciidoctorHttpMojo extends AsciidoctorRefreshMojo {
    @Override
    protected void doWork() throws MojoFailureException, MojoExecutionException {
        final AsciidoctorHttpServer server = new AsciidoctorHttpServer(getLog(), port, outputDirectory);
        server.start();

        super.doWork();

        server.stop();
>>>>>>> upstream/master
    }
}
