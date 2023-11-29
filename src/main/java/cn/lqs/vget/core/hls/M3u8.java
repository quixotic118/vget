package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.Video;

import java.util.List;

public interface M3u8 extends Video {

    final static String LOCAL_KEY_FILE_NAME = "file.key";
    final static String LOCAL_INDEX_FILE_NAME = "index";
    final static String LOCAL_SEGMENT_NAME_PREFIX = "segment-";
    final static String LOCAL_SEGMENT_NAME_POSTFIX = "";

    int version();

    double maxTsDuration();

    int sequence();

    String playlistType();

    boolean isAllowCache();

    List<TsSegment> segments();
}
