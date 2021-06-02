package ru.zuma;

import ru.zuma.http.HttpMethod;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NettyRequestUtil {
    private static Map<io.netty.handler.codec.http.HttpMethod, HttpMethod> methodToCommonMap = Map.of(
        io.netty.handler.codec.http.HttpMethod.DELETE,  HttpMethod.DELETE,
        io.netty.handler.codec.http.HttpMethod.GET,     HttpMethod.GET,
        io.netty.handler.codec.http.HttpMethod.POST,    HttpMethod.POST,
        io.netty.handler.codec.http.HttpMethod.PUT,     HttpMethod.PUT,
        io.netty.handler.codec.http.HttpMethod.OPTIONS, HttpMethod.OPTIONS,
        io.netty.handler.codec.http.HttpMethod.HEAD,     HttpMethod.HEAD
    );
    private static Map<HttpMethod, io.netty.handler.codec.http.HttpMethod> nettyToMethodMap = methodToCommonMap
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getValue,
            Map.Entry::getKey
        ));

    public static HttpHeaders convertToCommon(io.netty.handler.codec.http.HttpHeaders headers) {
        var headersMap = StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(headers.iteratorAsString(), Spliterator.ORDERED),
            false
        ).collect(
            Collectors.toMap(Map.Entry::getKey,
            (e) -> List.of(e.getValue()),
            (a, b) -> { a.addAll(b); return a; })
        );

        return HttpHeaders.of(headersMap, (a, b) -> true);
    }

    public static HttpMethod convertToCommon(io.netty.handler.codec.http.HttpMethod method) {
        return methodToCommonMap.get(method);
    }

    public static io.netty.handler.codec.http.HttpMethod convertToNetty(HttpMethod method) {
        return nettyToMethodMap.get(method);
    }
}
