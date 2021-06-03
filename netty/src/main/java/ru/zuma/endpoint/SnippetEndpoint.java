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
import ru.zuma.storage.SnippetService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

public class SnippetEndpoint extends RestEndpointBase {
    private final SnippetService snippetService;

    public SnippetEndpoint(String path, SnippetService snippetService) {
        super(path);
        this.snippetService = snippetService;
    }

    @Override
    public HttpResponse<Object> handleRequest(HttpRequest request, Object body) {
        if (request.method() == HttpMethod.GET) {
            return handleGetRequest(request, body);
        } else if (request.method() == HttpMethod.POST) {
            return handlePostRequest(request, body);
        }

        return new HttpResponse<>(HttpResponseStatus.NOT_FOUND);
    }

    private HttpResponse<Object> handleGetRequest(HttpRequest request, Object body) {
        if (request.parameters().isEmpty()) {
            return new HttpResponse<>(HttpResponseStatus.NOT_IMPLEMENTED);
        }

        var id = request.parameters().get("id");
        if (id == null || id.isEmpty()) {
            return new HttpResponse<>(HttpResponseStatus.BAD_REQUEST);
        }

        Optional<String> snippet;
        try {
            snippet = snippetService.get(Integer.valueOf(id.get(0)));
        } catch (NumberFormatException e) {
            return new HttpResponse<>(HttpResponseStatus.BAD_REQUEST);
        }

        if (snippet.isEmpty()) {
            return new HttpResponse<>(HttpResponseStatus.NOT_FOUND);
        }

        return new HttpResponse<>(
                HttpResponseStatus.OK,
                JsonStream.serialize(
                    new Snippet(10, "Static snippet")
                ));
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
