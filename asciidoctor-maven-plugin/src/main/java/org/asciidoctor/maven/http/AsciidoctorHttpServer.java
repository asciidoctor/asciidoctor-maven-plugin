package org.asciidoctor.maven.http;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import org.apache.maven.plugin.logging.Log;

/**
 * HTTP server to expose AsciiDoc converted sources.
 */
public class AsciidoctorHttpServer {

    private static final String HOST = "localhost";
    private static final int THREAD_NUMBER = 3;
    private static final String THREAD_PREFIX = "asciidoctor-thread-";

    private final Log logger;
    private final int port;
    private final File workDir;
    private final String defaultPage;

    private ServerBootstrap bootstrap;
    private NioEventLoopGroup workerGroup;

    /**
     * Constructor.
     *
     * @param logger           server logger
     * @param port             server port
     * @param workingDirectory sources location
     * @param defaultPage      default page used for root (aka. index)
     */
    public AsciidoctorHttpServer(final Log logger, final int port, final File workingDirectory, final String defaultPage) {
        this.logger = logger;
        this.port = port;
        this.workDir = workingDirectory;
        this.defaultPage = defaultPage;
    }

    /**
     * Start server.
     *
     * @return server instance
     */
    public AsciidoctorHttpServer start() {
        final AtomicInteger threadId = new AtomicInteger(1);
        workerGroup = new NioEventLoopGroup(THREAD_NUMBER, runnable -> {
            final Thread t = new Thread(runnable, THREAD_PREFIX + threadId.getAndIncrement());
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            return t;
        });

        try {
            bootstrap = new ServerBootstrap();
            bootstrap
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_SNDBUF, 1024)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .group(workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) {
                            ch.pipeline()
                                    .addLast("decoder", new HttpRequestDecoder())
                                    .addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE))
                                    .addLast("encoder", new HttpResponseEncoder())
                                    .addLast("chunked-writer", new ChunkedWriteHandler())
                                    .addLast("asciidoctor", new AsciidoctorHandler(workDir, defaultPage));
                        }
                    })
                    .bind(port)
                    .addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            logger.error("Can't start HTTP server");
                        } else {
                            logger.info(String.format("Server started on http://%s:%s", HOST, port));
                        }
                    }).sync();
        } catch (final InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    /**
     * Stop server.
     */
    public void stop() {
        Future<?> shutdownGracefully = workerGroup.shutdownGracefully();
        logger.info("Server stopping...");
        try {
            shutdownGracefully.get();
            logger.info("Server stopped");
        } catch (InterruptedException e) {
            logger.error(e);
        } catch (ExecutionException e) {
            logger.error(e);
        }
    }
}
