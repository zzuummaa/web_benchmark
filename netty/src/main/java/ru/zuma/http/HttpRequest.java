package ru.zuma.http;

import java.net.http.HttpHeaders;

public class HttpRequest {
    private String uri;
    private HttpMethod method;
    private HttpHeaders headers;
    private byte[] body;

    public HttpRequest(String uri, HttpMethod method, HttpHeaders headers) {
        this.uri = uri;
        this.method = method;
        this.headers = headers;
    }

    public String uri() {
        return uri;
    }

    public HttpMethod method() {
        return method;
    }

    public HttpHeaders headers() {
        return headers;
    }
}
