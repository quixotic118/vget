package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.common.*;
import cn.lqs.vget.core.common.utils.HttpUtils;
import cn.lqs.vget.core.exceptions.HttpUrlHrefException;
import cn.lqs.vget.core.exceptions.M3uTagParsedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static cn.lqs.vget.core.common.utils.HttpUtils.isSuccess;
import static cn.lqs.vget.core.hls.M3u8Tags.*;


public class NetM3u8Parser implements M3u8Parser {

    private final static Logger log = LoggerFactory.getLogger(NetM3u8Parser.class);

    private final URI indexUri;
    private final HttpHeader[] httpHeaders;
    private final HttpClient httpClient;

    protected NetM3u8Parser(URI indexUri, HttpClient httpClient, HttpHeader[] httpHeaders) {
        this.indexUri = indexUri;
        this.httpHeaders = httpHeaders;
        this.httpClient = httpClient;
    }

    public URI getIndexUri() {
        return indexUri;
    }

    public String getReferer() {
        return Arrays.stream(httpHeaders)
                .filter(httpHeader -> HttpHeaderNVs.Referer.equals(httpHeader.name()))
                .findFirst()
                .map(HttpHeader::value).orElse(null);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    private M3u8 fetchAndParse() {
        log.info("Try fetch index m3u8 content by send request to {}.", indexUri);
        HttpRequest request = HttpUtils.getRequest(this.indexUri, this.httpHeaders);
        try {
            return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenApply(response -> {
                        HttpLogger.logResponse(response, log);
                        if (isSuccess(response.statusCode())) {
                            log.info("Try parse index m3u8 response.");
                            Stream<String> ss = response.body();
                            try {
                                return parseSs(ss, indexUri.toString());
                            } catch (HttpUrlHrefException | M3uTagParsedException e) {
                                log.error("parse m3u8 index file fail!", e);
                            }
                        }
                        return null;
                    }).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("fetch m3u8 index file failed!", e);
        }
        return null;
    }

    private M3u8 parseSs(Stream<String> ss, String baseUrl) throws M3uTagParsedException, HttpUrlHrefException {
        Iterator<String> iter = ss.iterator();
        // 检查头标签
        final String firstT = findNextSafely(iter);
        if (!TAG_HEADER.equals(firstT)) {
            throw new M3uTagParsedException("except m3u8 head tag but get " + firstT);
        }
        // 检查第二个标签 判断是否 master playlist
        String secTag = findNextSafely(iter);
        if (TAG_STREAM_INF.equals(secTag)) {
            throw new M3uTagParsedException("not support master playlist for now!");
        }
        // 参数
        // 最大的片段时长
        double maxDuration = 0;
        int version = 0;
        int sequence = 0;
        String playlistType = "";
        String videoHeadUrl = "";
        boolean allowCache = false;
        EncryptInfo encryptInfo = null;
        // 遍历所有的头部标签
        while (secTag != null) {
            if (secTag.startsWith(TAG_TARGETDURATION)) {
                maxDuration = Double.parseDouble(extractHTagValue(secTag));
            } else if (secTag.startsWith(TAG_VERSION)) {
                version = Integer.parseInt(extractHTagValue(secTag));
            } else if (secTag.startsWith(TAG_SEQUENCE)) {
                sequence = Integer.parseInt(extractHTagValue(secTag));
            } else if (secTag.startsWith(TAG_PLAYLIST_TYPE)) {
                String s = extractHTagValue(secTag);
                if (!TAG_PLAYLIST_TYPE_VOD.equals(s)) {
                    throw new M3uTagParsedException("only support VOD type for now. but got " + s);
                }
                playlistType = TAG_PLAYLIST_TYPE_VOD;
            } else if (secTag.startsWith(TAG_ALLOW_CACHE)) {
                allowCache = extractHTagValue(secTag).equals(TAG_ALLOW_CACHE_TRUE);
            } else if (secTag.startsWith(TAG_KEY)) {
                encryptInfo = parseEncryptInfoFromKeyTag(secTag, baseUrl);
            } else if (secTag.startsWith(TAG_X_MAP)) {
                videoHeadUrl = UrlUtils.mockHref(baseUrl, extractVideoHeadUrl(secTag));
            } else if (secTag.startsWith(TAG_INF)) {
                // 跳出头部标签, 进入 segment 标签解析
                break;
            } else {
                log.warn("read unknown tag [{}]", secTag);
            }
            secTag = findNextSafely(iter);
        }
        ArrayList<Segment> segments = parseAllSegments(secTag, iter, baseUrl, encryptInfo);
        return new ImmutableNetM3u8(httpHeaders, baseUrl, maxDuration, version, sequence, playlistType, allowCache, segments, videoHeadUrl);
    }

    private ArrayList<Segment> parseAllSegments(String secTag, Iterator<String> iter, String baseUrl, EncryptInfo encryptCtx) throws HttpUrlHrefException, M3uTagParsedException {
        ArrayList<Segment> segments = new ArrayList<>(1 << 6);
        // 解析所有的 segment
        int counter = 1;
        int partition = 0;
        while (secTag != null) {
            if (!secTag.startsWith(TAG_INF)) {
                if (secTag.startsWith(TAG_END)) {
                    // 结束标记
                    break;
                } else if (secTag.startsWith(TAG_DISCONTINUITY)) {
                    partition++;
                } else if (secTag.startsWith(TAG_KEY)) {
                    encryptCtx = parseEncryptInfoFromKeyTag(secTag, baseUrl);
                } else {
                    log.warn("unknown tag [{}].", secTag);
                }
                secTag = findNextSafely(iter);
                continue;
            }
            var inf = secTag.substring(TAG_INF.length() + 1).trim();
            String[] spInf = inf.split(",");
            var tsDuration = Double.parseDouble(spInf[0]);
            var extraInfo = "";
            if (spInf.length > 1) {
                extraInfo = spInf[1];
            }
            var tsUrl = findNextSafely(iter);
            segments.add(new Segment(counter, UrlUtils.mockHref(baseUrl, tsUrl), tsDuration, extraInfo, partition, encryptCtx));
            counter++;
            secTag = findNextSafely(iter);
        }
        return segments;
    }

    private String extractVideoHeadUrl(String tagStr) {
        tagStr = tagStr.substring(TAG_X_MAP.length()).trim();
        int startIdx = -1, endIdx = -1;
        for (int i = 0; i < tagStr.length(); i++) {
            if (tagStr.charAt(i) == '"') {
                if (startIdx == -1) {
                    startIdx = i;
                    continue;
                }
                endIdx = i;
                break;
            }
        }
        if (startIdx == -1 || endIdx == -1) {
            log.warn("Parse Tag [{}] failed! with value -> [{}].", TAG_X_MAP, tagStr);
            return "";
        }
        return tagStr.substring(startIdx + 1, endIdx);
    }

    private EncryptInfo parseEncryptInfoFromKeyTag(String keyTag, String baseUrl) throws M3uTagParsedException, HttpUrlHrefException {
        String encryptedMethod = "NONE";
        String encryptKeyUri = "";
        String encryptKeyIV = "";
        label:
        for (String attr : keyTag.substring(TAG_KEY.length() + 1).split(",")) {
            if (attr.isEmpty()) {
                continue;
            }
            attr = attr.trim();
            int i = attr.indexOf("=");
            String[] kv = new String[]{
                    attr.substring(0, i),
                    attr.substring(i + 1)
            };
            switch (kv[0]) {
                case "METHOD" -> {
                    if ("NONE".equals(kv[1])) {
                        break label;
                    } else if ("AES-128".equals(kv[1])) {
                        encryptedMethod = "AES-128";
                        continue;
                    }
                    throw new M3uTagParsedException("not support encrypt method " + kv[1]);
                }
                case "URI" -> encryptKeyUri = UrlUtils.mockHref(baseUrl, kv[1].replaceAll("\"", ""));
                case "IV" -> encryptKeyIV = kv[1];
            }
        }
        return new EncryptInfo(encryptedMethod, encryptKeyUri, encryptKeyIV);
    }

    /**
     * 获取下一个非空的字符串
     */
    private String findNextSafely(Iterator<String> iter) {
        while (iter.hasNext()) {
            String next = iter.next();
            if (!next.trim().isEmpty()) {
                return next;
            }
        }
        return null;
    }

    private String extractHTagValue(String hTag) {
        return hTag.split(":")[1].trim();
    }

    @Override
    public M3u8 parse() {
        return fetchAndParse();
    }
}
