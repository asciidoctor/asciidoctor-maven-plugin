package org.asciidoctor.maven.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class AsciidoctorHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String HTML_MEDIA_TYPE = "text/html";
    public static final String HTML_EXTENSION = ".html";

    private final File directory;
    private final String defaultPage;

    public AsciidoctorHandler(final File workDir, final String defaultPage) {
        this.directory = workDir;

        if (defaultPage.contains(".")) {
            this.defaultPage = defaultPage;
        } else {
            this.defaultPage = addDefaultExtension(defaultPage);
        }
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest msg) throws Exception {

        if (msg.method() != HttpMethod.GET && msg.method() != HttpMethod.HEAD) {
            send(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED));
            return;
        }

        final File file = deduceFile(msg.uri());

        if (!file.exists()) {
            final ByteBuf body = Unpooled.copiedBuffer("<body><html>File not found: " + file.getPath() + "<body></html>", CharsetUtil.UTF_8);
            final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, body);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HTML_MEDIA_TYPE);
            send(ctx, response);
            return;
        }

        // HEAD means we already loaded the page, so we know is HTML
        if (msg.method() == HttpMethod.HEAD) {
            final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.RESET_CONTENT);

            final HttpHeaders headers = response.headers();
            // Test if retuning any size works
            headers.set(HttpHeaderNames.CONTENT_LENGTH, file.length());
            headers.set(HttpHeaderNames.EXPIRES, 0);
            headers.set(HttpHeaderNames.CONTENT_TYPE, HTML_MEDIA_TYPE);
            send(ctx, response);
            return;
        }

        final ByteBuf body;

        if (file.getName().endsWith("html")) {
            final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            body = Unpooled.copiedBuffer(addRefreshing(content), CharsetUtil.UTF_8);
        } else {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final FileInputStream fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, baos);
            body = Unpooled.copiedBuffer(FileUtils.readFileToByteArray(file));
            IOUtils.closeQuietly(fileInputStream);
        }

        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, body);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mediaType(file.getName()));
        send(ctx, response);
    }

    private String addRefreshing(final String html) {
        return html.replace("</body>", "<script src=\"http://livejs.com/live.js#html\"></script></body>");
    }

    private void send(final ChannelHandlerContext ctx, final DefaultFullHttpResponse response) {
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private File deduceFile(final String path) {
        if (path.isEmpty() || "/".equals(path)) {
            return new File(directory, defaultPage);
        }

        return new File(directory, path.contains(".") ? path : addDefaultExtension(path));
    }

    private static String addDefaultExtension(String path) {
        return path + HTML_EXTENSION;
    }

    private static String mediaType(final String name) {
        if (name.endsWith(".html")) {
            return HTML_MEDIA_TYPE;
        }
        if (name.endsWith(".js")) {
            return "text/javascript";
        }
        if (name.endsWith(".css")) {
            return "text/css";
        }
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        if (name.endsWith(".jpeg") || name.endsWith(".jpg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}
