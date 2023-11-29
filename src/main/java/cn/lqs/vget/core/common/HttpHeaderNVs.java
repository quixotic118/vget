package cn.lqs.vget.core.common;

public class HttpHeaderNVs {

    public final static String UserAgent = "User-Agent";

    public final static String Referer = "Referer";

    public final static String Cookie = "Cookie";
    public final static String Range = "Range";
    public final static String RangeFromZero = "bytes=0-";

    public final static String ContentLength = "Content-Length";
    public final static String ContentType = "Content-Type";
    public final static String ContentRange = "Content-Range";
    public final static String ApplicationOctetStream = "application/octet-stream";

    public final static String[] DEFAULT_HEADERS = new String[]{
            UserAgent, UserAgents.Edge
    };


    public static class UserAgents {
        public final static String Edge = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78";

    }
}
