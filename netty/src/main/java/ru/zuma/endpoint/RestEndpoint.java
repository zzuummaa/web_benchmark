package ru.zuma.endpoint;

import ru.zuma.http.HttpRequest;
import ru.zuma.http.HttpResponse;

public interface RestEndpoint {
    String getPath();

    HttpResponse<Object> handleRequest(HttpRequest request, Object body);
}
