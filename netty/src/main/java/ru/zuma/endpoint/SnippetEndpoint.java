package ru.zuma.endpoint;


import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.JsonException;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import ru.zuma.http.HttpMethod;
import ru.zuma.http.HttpRequest;
import ru.zuma.http.HttpResponse;
import ru.zuma.model.Snippet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class SnippetEndpoint extends RestEndpointBase {

    public SnippetEndpoint(String path) {
        super(path);
    }

    @Override
    public HttpResponse<Object> handleRequest(HttpRequest request, Object body) {
        if (request.method() == HttpMethod.POST) {
            return handlePostRequest(request, body);
        }

        return new HttpResponse<>(HttpResponseStatus.NOT_FOUND);
    }

    private HttpResponse<Object> handlePostRequest(HttpRequest request, Object body) {
        Snippet snippet = null;
        try {
            if (body instanceof ByteBuf buffer) {
                snippet = JsonIterator
                        .parse(toByteArray(buffer))
                        .read(Snippet.class);
            } else if (body instanceof ByteBuffer buffer) {
                snippet = JsonIterator
                        .parse(buffer.array(), buffer.arrayOffset(), buffer.arrayOffset() + buffer.remaining())
                        .read(Snippet.class);
            }
        } catch (IOException | JsonException e) {
            return new HttpResponse<>(
                    HttpResponseStatus.BAD_REQUEST,
                    JsonStream.serialize(
                            Map.of("error", e.getMessage())
                    ));
        }

        if (snippet == null) {
            return new HttpResponse<>(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    JsonStream.serialize(
                            Map.of("error", "Unknown body class: " + body.getClass().getName())
                    ));
        }

        if (snippet.getId() == null || snippet.getSnippet() == null) {
            return new HttpResponse<>(HttpResponseStatus.BAD_REQUEST);
        }

        return new HttpResponse<>(HttpResponseStatus.OK);
    }

    private byte[] toByteArray(ByteBuf buf) {
        byte[] arr = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), arr);
        return arr;
    }

    private byte[] toByteArray(ByteBuffer buf) {
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        return arr;
    }
}
