package cn.lqs.vget.core.mp4;

import cn.lqs.vget.core.common.*;
import cn.lqs.vget.core.common.utils.HttpUtils;
import cn.lqs.vget.core.common.utils.VideoHttpUtils;
import cn.lqs.vget.core.exceptions.FailDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class SimpleNetMp4Downloader extends NetMp4Downloader{

    private final static Logger log = LoggerFactory.getLogger(SimpleNetMp4Downloader.class);

    private final Path savedPt;

    protected SimpleNetMp4Downloader(HttpClient httpClient, HttpHeader[] customHeaders, String url, Path savedPt) {
        super(httpClient, customHeaders, url);
        this.savedPt = savedPt;
    }

    @Override
    public Path download() throws FailDownloadException {
        try {
            HttpResponse<InputStream> response = getHttpClient().send(HttpUtils.getRequest(this.getUrl(),
                    VideoHttpUtils.clearRange(this.getCustomHeaders())), HttpResponse.BodyHandlers.ofInputStream());
            HttpLogger.logResponse(response, log);
            if (HttpUtils.isSuccess(response.statusCode())) {
                try (FileOutputStream fos = new FileOutputStream(savedPt.toFile());
                     InputStream is = response.body()){
                    is.transferTo(fos);
                }
                return savedPt;
            }
            throw new FailDownloadException("error response code " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new FailDownloadException(e);
        }
    }

    @Override
    public Path download(ProgressWatcher watcher) throws FailDownloadException {
        if (watcher == null) {
            throw new FailDownloadException("null watcher");
        }
        log.info("use SimpleNetMp4Downloader download with watcher...");
        HttpResponse<InputStream> response = null;
        try {
            response = getHttpClient().send(HttpUtils.getRequest(this.getUrl(),
                    VideoHttpUtils.clearRange(this.getCustomHeaders())), HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            throw new FailDownloadException(e);
        }
        HttpLogger.logResponse(response, log);
        if (HttpUtils.isSuccess(response.statusCode())) {
            if (!VideoHttpUtils.isPossibleVideoResponse(response)) {
                throw new FailDownloadException("possible no video response!");
            }
            long total = response.headers().firstValueAsLong(HttpHeaderNVs.ContentLength).orElse(0);
            if (total > 0) {
                watcher.startWatch();
            }
            try (FileOutputStream fos = new FileOutputStream(savedPt.toFile());
                 InputStream is = response.body()) {
                byte[] buffer = new byte[VideoHttpUtils.ChunkSize];
                long offset = 0;
                int len;
                int counter = 1;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    offset += len;
                    if (counter == ProgressWatcher.RATE) {
                        watcher.watch(offset, total);
                        counter = 1;
                    }
                    counter++;
                }
            } catch (IOException e) {
                throw new FailDownloadException(e);
            } finally {
                if (watcher.isRunning()) {
                    watcher.stopWatch();
                }
            }
            return savedPt;
        }
        throw new FailDownloadException("error response code " + response.statusCode());
    }
}
