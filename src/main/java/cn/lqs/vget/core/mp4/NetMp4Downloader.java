package cn.lqs.vget.core.mp4;

import cn.lqs.vget.core.common.HttpHeader;
import cn.lqs.vget.core.exceptions.FailDownloadException;

import java.net.http.HttpClient;
import java.nio.file.Path;

public abstract class NetMp4Downloader {

    private final HttpClient httpClient;
    private final HttpHeader[] customHeaders;
    private final String url;

    protected NetMp4Downloader(HttpClient httpClient, HttpHeader[] customHeaders, String url) {
        this.httpClient = httpClient;
        this.customHeaders = customHeaders;
        this.url = url;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public HttpHeader[] getCustomHeaders() {
        return customHeaders;
    }

    public String getUrl() {
        return url;
    }

    public abstract Path download() throws FailDownloadException;

    public abstract Path download(ProgressWatcher watcher) throws FailDownloadException;
}
