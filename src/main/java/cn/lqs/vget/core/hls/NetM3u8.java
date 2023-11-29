package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.common.HttpHeader;

public interface NetM3u8 extends M3u8{

    HttpHeader[] customHeaders();

    String baseUrl();

}
