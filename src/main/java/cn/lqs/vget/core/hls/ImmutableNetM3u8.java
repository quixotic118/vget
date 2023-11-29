package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.common.HttpHeader;

import java.util.ArrayList;
import java.util.List;

public class ImmutableNetM3u8 implements NetM3u8{

    private final HttpHeader[] customHeaders;
    private final String baseUrl;
    private final double maxDuration;
    private final int version;
    private final int sequence;
    private final String playlistType;
    private final boolean allowCache;
    private final ArrayList<TsSegment> segments;

    public double getMaxDuration() {
        return maxDuration;
    }

    public int getVersion() {
        return version;
    }

    public HttpHeader[] getCustomHeaders() {
        return customHeaders;
    }

    public int getSequence() {
        return sequence;
    }

    public String getPlaylistType() {
        return playlistType;
    }

    public ArrayList<TsSegment> getSegments() {
        return segments;
    }

    protected ImmutableNetM3u8(HttpHeader[] customHeaders, String baseUrl,
                               double maxDuration, int version, int sequence, String playlistType,
                               boolean allowCache, ArrayList<TsSegment> segments) {
        this.customHeaders = customHeaders;
        this.baseUrl = baseUrl;
        this.maxDuration = maxDuration;
        this.version = version;
        this.sequence = sequence;
        this.playlistType = playlistType;
        this.allowCache = allowCache;
        this.segments = segments;
    }

    public String getBaseUrl() {
        return baseUrl;
    }


    @Override
    public HttpHeader[] customHeaders() {
        return customHeaders;
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public double maxTsDuration() {
        return maxDuration;
    }

    @Override
    public int sequence() {
        return sequence;
    }

    @Override
    public String playlistType() {
        return playlistType;
    }

    @Override
    public boolean isAllowCache() {
        return allowCache;
    }

    @Override
    public List<TsSegment> segments() {
        return segments;
    }
}
