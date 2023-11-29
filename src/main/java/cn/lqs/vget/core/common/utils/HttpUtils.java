package cn.lqs.vget.core.common.utils;

import cn.lqs.vget.core.common.HttpHeader;
import cn.lqs.vget.core.common.HttpHeaderNVs;
import cn.lqs.vget.core.common.HttpLogger;

import java.net.URI;
import java.net.http.HttpRequest;

public final class HttpUtils {

    public static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    public static HttpRequest getRequest(String uri, HttpHeader[] customHeaders) {
        return getRequest(URI.create(uri), customHeaders);
    }

    public static HttpRequest getRequest(URI uri, HttpHeader[] customHeaders) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .headers(HttpHeaderNVs.DEFAULT_HEADERS);
        if (customHeaders != null) {
            for (HttpHeader header : customHeaders) {
                builder.setHeader(header.name(), header.value());
                for (String value : header.values()) {
                    builder.header(header.name(), value);
                }
            }
        }
        HttpRequest request = builder.build();
        HttpLogger.logRequest(request);
        return request;
    }

}
