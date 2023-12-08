package cn.lqs.vget.core.hls;

import java.util.Objects;

public record Segment(int order, String url, double duration, String extraInfo, int partition, EncryptInfo encryptInfo) {

    public String localSegName(int sequence) {
        return M3u8.LOCAL_SEGMENT_NAME_PREFIX + partition + "-" + (order + sequence - 1) + M3u8.LOCAL_SEGMENT_NAME_POSTFIX;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        return order == segment.order && partition == segment.partition && url.equals(segment.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, url, partition);
    }

    @Override
    public String toString() {
        return "TsSegment{" +
                "order=" + order +
                ", url='" + url + '\'' +
                ", duration=" + duration +
                ", extraInfo='" + extraInfo + '\'' +
                ", partition=" + partition +
                ", encryptInfo=" + encryptInfo +
                '}';
    }
}
