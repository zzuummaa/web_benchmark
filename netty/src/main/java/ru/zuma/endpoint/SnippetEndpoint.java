package ru.zuma.endpoint;


import io.netty.handler.codec.http.HttpResponseStatus;
import ru.zuma.http.HttpRequest;
import ru.zuma.http.HttpResponse;

public class SnippetEndpoint extends RestEndpointBase {

    public SnippetEndpoint(String path) {
        super(path);
    }

    @Override
    public HttpResponse<Object> handleRequest(HttpRequest request, Object body) {
        return new HttpResponse(HttpResponseStatus.NOT_IMPLEMENTED);
    }
}
