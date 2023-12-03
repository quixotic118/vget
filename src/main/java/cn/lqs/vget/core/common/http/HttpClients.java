package cn.lqs.vget.core.common.http;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

public class HttpClients {

    private static HttpClient PROXY_CLIENT;

    public static HttpClient proxyClient() {
        if (PROXY_CLIENT == null) {
            PROXY_CLIENT = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(new InetSocketAddress("localhost", 7890)))
                    .connectTimeout(Duration.ofSeconds(1 << 4))
                    .build();
        }
        return PROXY_CLIENT;
    }
}
