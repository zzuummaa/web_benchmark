package ru.zuma.http;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.http.HttpHeaders;
import java.util.Optional;

public class HttpResponse<T> {

    private HttpResponseStatus responseStatus;
    private HttpHeaders headers;
    private T body;

    public HttpResponse(HttpResponseStatus responseStatus) {
        this(responseStatus, null);
    }

    public HttpResponse(HttpResponseStatus responseStatus, T body) {
        this.responseStatus = responseStatus;
        this.body = body;
    }

    public HttpResponseStatus status() {
        return responseStatus;
    }

    public Optional<T> body() {
        return Optional.ofNullable(body);
    }
}
