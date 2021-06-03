package ru.zuma;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zuma.endpoint.RestEndpoint;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class EndpointHttpServerHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private static final AttributeKey<ru.zuma.http.HttpRequest> REQUEST_ATTRIBUTE_KEY;
    private static final AttributeKey<Boolean> IS_KEEP_ALIVE_ATTRIBUTE_KEY;
    private static final AttributeKey<ByteBuffer> BODY_BUFFER_ATTRIBUTE_KEY;

    private static <T> AttributeKey<T> tryInitAttributeKey(String keyName) {
        if (AttributeKey.exists(keyName)) {
            throw new IllegalStateException(AttributeKey.class.getName()
                    + " already contains attribute ket with name \"" + keyName + "\"");
        } else {
            return AttributeKey.valueOf(keyName);
        }
    }

    static {
        REQUEST_ATTRIBUTE_KEY = tryInitAttributeKey("request");
        IS_KEEP_ALIVE_ATTRIBUTE_KEY = tryInitAttributeKey("is_keep_alive");
        BODY_BUFFER_ATTRIBUTE_KEY = tryInitAttributeKey("body_string_builder");
    }

    private final int maxBodySize;
    private List<RestEndpoint> restEndpoints;

    public EndpointHttpServerHandler(List<RestEndpoint> restEndpoints) {
        this(restEndpoints, 1024);
    }

    public EndpointHttpServerHandler(List<RestEndpoint> restEndpoints, int maxBodySize) {
        this.restEndpoints = restEndpoints;
        this.maxBodySize = maxBodySize;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest request) {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());

            ctx.channel().attr(REQUEST_ATTRIBUTE_KEY).set(
                new ru.zuma.http.HttpRequest(
                    queryStringDecoder.path(),
                    queryStringDecoder.parameters(),
                    NettyRequestUtil.convertToCommon(request.method()),
                    NettyRequestUtil.convertToCommon(request.headers())
                )
            );
            ctx.channel().attr(IS_KEEP_ALIVE_ATTRIBUTE_KEY).set(HttpUtil.isKeepAlive(request));
        }

        if (msg instanceof HttpContent httpContent) {

            var contentBuffer = httpContent.content();
            var bodyBufAttribute = ctx.channel().attr(BODY_BUFFER_ATTRIBUTE_KEY);

            if (contentBuffer.isReadable()) {
                logger.info("{}", httpContent.content());
            }

            if (msg instanceof LastHttpContent trailer) {
                FullHttpResponse response;

                if (bodyBufAttribute.get() == null) {
                    response = handleRequest(ctx.channel(), httpContent.content());
                } else {
                    var sessionBuf = bodyBufAttribute.get();
                    contentBuffer.getBytes(
                        contentBuffer.readerIndex(),
                        sessionBuf.array(),
                        sessionBuf.arrayOffset(),
                        contentBuffer.readableBytes()
                    );
                    response = handleRequest(ctx.channel(), bodyBufAttribute.get());
                }

                writeResponse(ctx, response);
            } else if (contentBuffer.isReadable()) {
                if (bodyBufAttribute.get() == null) {
                    bodyBufAttribute.set(ByteBuffer.allocate(maxBodySize));
                }

                var sessionBuf = bodyBufAttribute.get();
                if (sessionBuf.remaining() < contentBuffer.readableBytes()) {
                    writeResponse(ctx, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER));
                } else {
                    contentBuffer.getBytes(
                        contentBuffer.readerIndex(),
                        sessionBuf.array(),
                        sessionBuf.arrayOffset(),
                        contentBuffer.readableBytes()
                    );
                }
            }
        }
    }

    private <T> FullHttpResponse handleRequest(Channel channel, T body) {
        var request = channel.attr(REQUEST_ATTRIBUTE_KEY).get();
        var response = restEndpoints.stream()
            .filter((e) -> e.getPath().equals(request.path()))
            .findFirst()
            .map((e) -> e.handleRequest(request, body));

        return toNettyResponse(response);
    }

    private FullHttpResponse toNettyResponse(Optional<ru.zuma.http.HttpResponse<Object>> httpResponseOptional) {
        if (httpResponseOptional.isEmpty()) {
            return new DefaultFullHttpResponse(
                    HTTP_1_1,
                    NOT_FOUND,
                    Unpooled.EMPTY_BUFFER
            );
        }

        var httpResponse = httpResponseOptional.get();

        ByteBuf bodyByteBuf = httpResponse
                .body()
                .map((o) -> Unpooled.copiedBuffer(o.toString(), CharsetUtil.UTF_8))
                .orElse(Unpooled.EMPTY_BUFFER);

        return new DefaultFullHttpResponse(
                HTTP_1_1,
                httpResponse.status(),
                bodyByteBuf
        );
    }

    private void writeResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE,
                Unpooled.EMPTY_BUFFER);
        ctx.write(response);
    }

    private void writeResponse(ChannelHandlerContext ctx, FullHttpResponse httpResponse) {
        boolean keepAlive = ctx.channel().attr(IS_KEEP_ALIVE_ATTRIBUTE_KEY).get();

        if (keepAlive) {
            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH,
                    httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaderNames.CONNECTION,
                    HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(httpResponse);

        if (!keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
