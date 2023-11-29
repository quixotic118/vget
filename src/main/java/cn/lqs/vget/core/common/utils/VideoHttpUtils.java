package cn.lqs.vget.core.common.utils;

import cn.lqs.vget.core.common.HttpHeader;
import cn.lqs.vget.core.common.HttpHeaderNVs;

import java.net.http.HttpResponse;

public class VideoHttpUtils {

    public final static int ChunkSize = 2 * 1024 * 8;

    public static boolean isPossibleVideoResponse(HttpResponse<?> response) {
        return response.headers().firstValue(HttpHeaderNVs.ContentType)
                .map(mediaType-> mediaType.startsWith("video") || mediaType.equalsIgnoreCase(HttpHeaderNVs.ApplicationOctetStream))
                .orElse(false);
    }


    public static HttpHeader[] clearRange(HttpHeader[] headers) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].name().equals(HttpHeaderNVs.Range)) {
                headers[i] = new HttpHeader(HttpHeaderNVs.Range, HttpHeaderNVs.RangeFromZero);
                return headers;
            }
        }
        HttpHeader[] newHeaders = new HttpHeader[headers.length + 1];
        int i = 0;
        for (; i < headers.length; i++) {
            newHeaders[i] = headers[i];
        }
        newHeaders[i] = new HttpHeader(HttpHeaderNVs.Range, HttpHeaderNVs.RangeFromZero);
        return newHeaders;
    }
}
