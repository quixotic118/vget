package cn.lqs.vget.core.mp4;

import cn.lqs.vget.core.VGet;
import cn.lqs.vget.core.common.HttpHeader;

import java.net.http.HttpClient;
import java.nio.file.Path;

import static cn.lqs.vget.core.common.ConcurrencyPolicy.SINGLE;

public class NetMp4VGet extends VGet {

    private HttpHeader[] headers;
    private HttpClient httpClient;

    private String cacheDir;

    public NetMp4VGet(HttpHeader[] headers, HttpClient httpClient, String cacheDir) {
        this.headers = headers;
        this.httpClient = httpClient;
        this.cacheDir = cacheDir;
    }

    protected void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    protected void setHeaders(HttpHeader[] headers) {
        this.headers = headers;
    }

    protected void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public NetMp4Downloader createDownloader(String url, Path dst) {
        return createDownloader(url, dst, SINGLE);
    }

    @Override
    public NetMp4Downloader createDownloader(String url, Path dst, int concurrency) {
        if (concurrency == SINGLE) {
            return new SimpleNetMp4Downloader(httpClient, headers, url, dst);
        }
        return new MultiThreadNetMp4Downloader(httpClient, headers, url, cacheDir, dst);
    }

}
