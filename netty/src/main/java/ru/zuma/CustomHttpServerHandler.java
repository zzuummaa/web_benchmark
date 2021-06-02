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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class CustomHttpServerHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private static final AttributeKey<ru.zuma.http.HttpRequest> REQUEST_ATTRIBUTE_KEY;
    private static final AttributeKey<Boolean> IS_KEEP_ALIVE_ATTRIBUTE_KEY;
    private static final AttributeKey<StringBuilder> BODY_STRING_BUILDER_ATTRIBUTE_KEY;

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
        BODY_STRING_BUILDER_ATTRIBUTE_KEY = tryInitAttributeKey("body_string_builder");
    }

    private List<RestEndpoint> restEndpoints;

    public CustomHttpServerHandler(List<RestEndpoint> restEndpoints) {
        this.restEndpoints = restEndpoints;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            var request = (DefaultHttpRequest) msg;
            ctx.channel().attr(REQUEST_ATTRIBUTE_KEY).set(
                new ru.zuma.http.HttpRequest(
                    request.uri(),
                    NettyRequestUtil.convertToCommon(request.method()),
                    NettyRequestUtil.convertToCommon(request.headers())
                )
            );
            ctx.channel().attr(IS_KEEP_ALIVE_ATTRIBUTE_KEY).set(HttpUtil.isKeepAlive(request));
        }

        if (msg instanceof HttpContent httpContent) {
            var bodyBuilderAttribute = ctx.channel().attr(BODY_STRING_BUILDER_ATTRIBUTE_KEY);

            if (msg instanceof LastHttpContent trailer) {
                FullHttpResponse response;

                if (bodyBuilderAttribute.get() == null) {
                    response = handleRequest(ctx.channel(), httpContent.content().toString(CharsetUtil.UTF_8));
                } else {
                    bodyBuilderAttribute.get().append(httpContent.content().toString(CharsetUtil.UTF_8));
                    response = handleRequest(ctx.channel(), bodyBuilderAttribute.get().toString());
                }

                writeResponse(ctx, response);
            } else {
                bodyBuilderAttribute.set(new StringBuilder(httpContent.content().toString(CharsetUtil.UTF_8)));
            }
        }
    }

    private FullHttpResponse handleRequest(Channel channel, String body) {
        var request = channel.attr(REQUEST_ATTRIBUTE_KEY).get();
        var response = restEndpoints.stream()
            .filter((e) -> e.getPath().equals(request.uri()))
            .findFirst()
            .map((e) -> e.handleRequest(request, body));

        return convertToNettyResponse(response);
    }

    private FullHttpResponse convertToNettyResponse(Optional<ru.zuma.http.HttpResponse<Object>> httpResponseOptional) {
        if (httpResponseOptional.isEmpty()) {
            return new DefaultFullHttpResponse(
                    HTTP_1_1,
                    NOT_FOUND,
                    Unpooled.EMPTY_BUFFER
            );
        }

        var httpResponse = httpResponseOptional.get();

        ByteBuf bodyByteBuf = (ByteBuf) httpResponse
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
