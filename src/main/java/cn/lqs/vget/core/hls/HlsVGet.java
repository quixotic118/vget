package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.common.HttpLogger;
import cn.lqs.vget.core.common.utils.HttpUtils;
import cn.lqs.vget.core.common.utils.VFileUtils;
import cn.lqs.vget.core.exceptions.FailDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.lqs.vget.core.common.ConcurrencyPolicy.AUTO;
import static cn.lqs.vget.core.common.ConcurrencyPolicy.SINGLE;

public class HlsVGet {

    private final static Logger log = LoggerFactory.getLogger(HlsVGet.class);

    private final NetM3u8 netM3u8;
    private final HttpClient httpClient;
    private String cacheDir;

    private volatile List<TsSegment> failedSegments;

    public HlsVGet(NetM3u8 netM3u8, HttpClient httpClient) {
        this.netM3u8 = netM3u8;
        this.httpClient = httpClient;
    }

    protected List<TsSegment> getFailedSegments() {
        return this.failedSegments;
    }

    public NetM3u8 getNetM3u8() {
        return netM3u8;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public HlsLocalCached download(String cacheDir, int concurrency) throws FailDownloadException {
        if (cacheDir == null) {
            throw new FailDownloadException("null cache directory.");
        }
        this.cacheDir = cacheDir;
        File dir = new File(this.cacheDir);
        if ((!dir.exists() && !dir.mkdir()) || (dir.exists() && !dir.isDirectory())) {
            throw new FailDownloadException("cache dir is not available!");
        }
        // VFileUtils.clearDir(dir);
        long startTs = System.currentTimeMillis();
        if (concurrency == SINGLE) {
            // 单线程下载
            singleThreadDownload();
        }else {
            // 多线程下载
            if (concurrency == AUTO || concurrency < 0) {
                concurrency = Runtime.getRuntime().availableProcessors();
            }
            multiThreadDownload(concurrency);
        }
        return new HlsLocalCached(this, System.currentTimeMillis() - startTs);
    }

    public HlsLocalCached download(String cacheDir) throws FailDownloadException{
        return download(cacheDir, SINGLE);
    }

    protected void retryFailedSegments() {
        if (this.cacheDir == null || this.failedSegments == null) {
            return;
        }
        int sequenceOrder = netM3u8.sequence() == 0 ? 0 : netM3u8.sequence();
        final ArrayList<TsSegment> newFailedSegments = new ArrayList<>();
        for (TsSegment segment : this.failedSegments) {
            downloadSegment(sequenceOrder, segment, true, newFailedSegments);
        }
        this.failedSegments = newFailedSegments;
    }

    private void multiThreadDownload(int n) {
        log.warn("begin multi thread download... Use {} Threads", n);
        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(n, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "hls-downloader-" + counter.getAndIncrement());
            }
        });
        final CountDownLatch latch = new CountDownLatch(netM3u8.segments().size());
        int sequenceOrder = netM3u8.sequence() == 0 ? 0 : netM3u8.sequence();
        log.info("the hls segment begin sequence -> [{}]", sequenceOrder);
        for (TsSegment segment : netM3u8.segments()) {
            pool.execute(()->{
                try {
                    downloadSegment(sequenceOrder, segment);
                } finally {
                    latch.countDown();
                }
            });
        }
        while (latch.getCount() > 0) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        pool.shutdown();
    }

    private void singleThreadDownload() {
        log.warn("begin single thread download...");
        int sequenceOrder = netM3u8.sequence() == 0 ? 0 : netM3u8.sequence();
        log.info("the hls segment begin sequence -> [{}]", sequenceOrder);
        for (TsSegment segment : netM3u8.segments()) {
            downloadSegment(sequenceOrder, segment);
        }
    }

    protected byte[] fetchKeyBytes(String keyUri) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = httpClient.send(HttpUtils.getRequest(keyUri, netM3u8.customHeaders()),
                HttpResponse.BodyHandlers.ofByteArray());
        HttpLogger.logResponse(response);
        if (HttpUtils.isSuccess(response.statusCode())) {
            return response.body();
        }
        throw new IOException("invalid response code " + response.statusCode());
    }

    private void downloadSegment(int sequenceOrder, TsSegment segment){
        downloadSegment(sequenceOrder, segment, false, null);
    }

    private void downloadSegment(int sequenceOrder, TsSegment segment, boolean retryMode, ArrayList<TsSegment> newFailedSegments) {
        Path dst = Path.of(this.cacheDir, segment.localTsName(sequenceOrder));
        try {
            log.info("Thread-[{}] Try download [{}] to local [{}]", Thread.currentThread().getName(), segment.url().split("\\?")[0], dst);
            HttpResponse<InputStream> response = httpClient.send(HttpUtils.getRequest(segment.url(),
                    netM3u8.customHeaders()), HttpResponse.BodyHandlers.ofInputStream());
            HttpLogger.logResponse(response);
            if (HttpUtils.isSuccess(response.statusCode())) {
                InputStream is = response.body();
                VFileUtils.copyInputStreamToFile(is, dst.toFile(), new byte[4 * 1024 * 8]);
                return;
            }
            throw new IOException("download failed! -> [" + segment.url() + "]");
        } catch (Exception e) {
            log.warn("download {} failed!.", dst.getFileName(), e);
            if (retryMode) {
                // 重试模式下应使用新的 failedSegments 存储
                newFailedSegments.add(segment);
            }else {
                // 不是重试模式下使用类的 failedSegments 存储
                if (this.failedSegments == null) {
                    this.failedSegments = new ArrayList<>();
                }
                this.failedSegments.add(segment);
            }
        }
    }
}
