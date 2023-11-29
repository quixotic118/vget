package cn.lqs.vget.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class HttpLogger {

    private final static Logger log = LoggerFactory.getLogger(HttpLogger.class);

    public static void logRequest(HttpRequest request) {
        logRequest(request, log);
    }

    public static void logRequest(HttpRequest request, Logger log) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.method()).append(' ').append(request.version().isEmpty() ? "" : request.version().get()).append(" -> ").append(request.uri());
        log.info(sb.toString());
        if (log.isDebugEnabled()) {
            // 打印请求头
            sb.setLength(0);
            for (Map.Entry<String, List<String>> entry : request.headers().map().entrySet()) {
                sb.append('\t').append(entry.getKey()).append(":\t").append(entry.getValue()).append('\n');
            }
            System.out.print(sb.toString());
        }
    }

    public static void logResponse(HttpResponse<?> response) {
        logResponse(response, log);
    }

    public static void logResponse(HttpResponse<?> response, Logger log) {
        log.info("Resp :: {}. ", response.statusCode());
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
                sb.append('\t').append(entry.getKey()).append(":\t").append(entry.getValue()).append('\n');
            }
            System.out.print(sb.toString());
        }
    }

}
