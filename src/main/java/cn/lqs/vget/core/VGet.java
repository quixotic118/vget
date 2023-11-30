package cn.lqs.vget.core;

import cn.lqs.vget.core.common.HttpHeader;
import cn.lqs.vget.core.hls.HlsVGet;
import cn.lqs.vget.core.hls.M3u8Parser;
import cn.lqs.vget.core.hls.NetM3u8;
import cn.lqs.vget.core.mp4.NetMp4VGet;

import java.net.http.HttpClient;
import java.nio.file.Path;

public abstract class VGet {

    public static HlsVGet ofNetM3u8(String indexUrl, HttpClient httpClient) {
        return ofNetM3u8((NetM3u8) M3u8Parser.fromUrl(indexUrl, httpClient).parse(), httpClient);
    }

    public static HlsVGet ofNetM3u8(String indexUrl, HttpClient httpClient, HttpHeader[] httpHeaders) {
        return ofNetM3u8((NetM3u8) M3u8Parser.fromUrl(indexUrl, httpClient, httpHeaders).parse(), httpClient);
    }

    public static HlsVGet ofNetM3u8(NetM3u8 m3u8, HttpClient httpClient) {
        return new HlsVGet(m3u8, httpClient);
    }


    public static NetMp4VGet ofNetMp4(HttpClient httpClient, HttpHeader[] headers, String cacheDir) {
        return new NetMp4VGet(headers, httpClient, cacheDir);
    }

    public abstract VDownloader createDownloader(String url, Path dst);

    public abstract VDownloader createDownloader(String url, Path dst, int concurrency);
}
