package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.common.HttpHeader;

import java.net.URI;
import java.net.http.HttpClient;

public interface M3u8Parser {

    M3u8 parse();

    static NetM3u8Parser fromUrl(String indexUrl, HttpClient httpClient) {
        return new NetM3u8Parser(URI.create(indexUrl), httpClient, null);
    }

    static NetM3u8Parser fromUrl(String indexUrl, HttpClient httpClient, HttpHeader[] httpHeaders) {
        return new NetM3u8Parser(URI.create(indexUrl), httpClient, httpHeaders);
    }
}
