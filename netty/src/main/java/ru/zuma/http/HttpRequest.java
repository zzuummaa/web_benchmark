package ru.zuma.http;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private String path;
    private Map<String, List<String>> parameters;
    private HttpMethod method;
    private HttpHeaders headers;

    public HttpRequest(String path, Map<String, List<String>> parameters, HttpMethod method, HttpHeaders headers) {
        this.path = path;
        this.parameters = parameters;
        this.method = method;
        this.headers = headers;
    }

    public String path() {
        return path;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public HttpMethod method() {
        return method;
    }

    public HttpHeaders headers() {
        return headers;
    }
}
