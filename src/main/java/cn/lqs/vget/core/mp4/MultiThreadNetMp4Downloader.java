package cn.lqs.vget.core.mp4;

import cn.lqs.vget.core.common.HttpHeader;
import cn.lqs.vget.core.common.HttpHeaderNVs;
import cn.lqs.vget.core.common.utils.HttpUtils;
import cn.lqs.vget.core.common.utils.VideoHttpUtils;
import cn.lqs.vget.core.exceptions.FailDownloadException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadNetMp4Downloader extends NetMp4Downloader{

    private final String cacheDir;
    private final Path dst;

    protected MultiThreadNetMp4Downloader(HttpClient httpClient, HttpHeader[] customHeaders, String url, String cacheDir, Path dst) {
        super(httpClient, customHeaders, url);
        this.cacheDir = cacheDir;
        this.dst = dst;
    }

    @Override
    public Path download() throws FailDownloadException {
        HttpRequest request = HttpUtils.getRequest(getUrl(), VideoHttpUtils.clearRange(getCustomHeaders()));
        HttpResponse<Void> statusResp = null;
        try {
            statusResp = getHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new FailDownloadException(e);
        }
        if (HttpUtils.isSuccess(statusResp.statusCode())) {
            HttpHeaders headers = statusResp.headers();
            headers.firstValueAsLong(HttpHeaderNVs.ContentLength);
        }
        int blockSize = 4 * 1024 * 1024 * 8;
        ExecutorService es = Executors.newFixedThreadPool(10);
        return null;
    }

    @Override
    public Path download(ProgressWatcher watcher) {
        return null;
    }
}
