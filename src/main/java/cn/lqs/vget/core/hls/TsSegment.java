package cn.lqs.vget.core.hls;

import java.util.Objects;

public record TsSegment(int order, String url, double duration, String extraInfo, int partition, EncryptInfo encryptInfo) {

    public String localTsName(int sequence) {
        return M3u8.LOCAL_SEGMENT_NAME_PREFIX + partition + "-" + (order + sequence - 1) + M3u8.LOCAL_SEGMENT_NAME_POSTFIX;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TsSegment tsSegment = (TsSegment) o;
        return order == tsSegment.order && partition == tsSegment.partition && url.equals(tsSegment.url);
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
