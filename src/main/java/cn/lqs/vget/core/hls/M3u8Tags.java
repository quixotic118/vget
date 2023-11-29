package cn.lqs.vget.core.hls;

public class M3u8Tags {

    public final static String TAG_VERSION = "#EXT-X-VERSION";
    public final static String TAG_SEQUENCE = "#EXT-X-MEDIA-SEQUENCE";

    public final static String TAG_TARGETDURATION = "#EXT-X-TARGETDURATION";
    public final static String TAG_DISCONTINUITY = "#EXT-X-DISCONTINUITY";
    public final static String TAG_PLAYLIST_TYPE = "#EXT-X-PLAYLIST-TYPE";
    public final static String TAG_PLAYLIST_TYPE_VOD = "VOD";
    public final static String TAG_PLAYLIST_TYPE_EVENT = "EVENT";
    public final static String TAG_HEADER = "#EXTM3U";
    public final static String TAG_END = "#EXT-X-ENDLIST";
    public final static String TAG_INF = "#EXTINF";
    public final static String TAG_KEY = "#EXT-X-KEY";
    public final static String TAG_ALLOW_CACHE = "#EXT-X-ALLOW-CACHE";
    public final static String TAG_ALLOW_CACHE_TRUE = "YES";
    public final static String TAG_ALLOW_CACHE_FALSE = "NO";
    public final static String TAG_STREAM_INF = "#EXT-X-STREAM-INF";
}
