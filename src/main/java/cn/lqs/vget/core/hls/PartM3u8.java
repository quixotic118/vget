package cn.lqs.vget.core.hls;

import java.util.List;

import static cn.lqs.vget.core.hls.M3u8Tags.*;

public class PartM3u8 implements M3u8{

    private final int partition;
    private final int parentSequence;
    private int version;
    private double maxTsDuration;
    private int sequence;
    private String playlistType;
    private boolean allowCache;
    private List<TsSegment> segments;

    private boolean hasDecoded = false;

    public void setHasDecoded(boolean hasDecoded) {
        this.hasDecoded = hasDecoded;
    }

    public PartM3u8(int partition, int parentSequence) {
        this.partition = partition;
        this.parentSequence = parentSequence;
    }

    public PartM3u8(int partition, int parentSequence, int version, double maxTsDuration, int sequence, String playlistType, boolean allowCache, List<TsSegment> segments) {
        this.partition = partition;
        this.parentSequence = parentSequence;
        this.version = version;
        this.maxTsDuration = maxTsDuration;
        this.sequence = sequence;
        this.playlistType = playlistType;
        this.allowCache = allowCache;
        this.segments = segments;
    }

    public String toM3u8String() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG_HEADER).append("\n");
        if (this.version > 0) {
            sb.append(TAG_VERSION).append(":").append(this.version).append("\n");
        }
        if (this.maxTsDuration > 0) {
            sb.append(TAG_TARGETDURATION).append(":").append(this.maxTsDuration).append("\n");
        }
        if (this.allowCache) {
            sb.append(TAG_ALLOW_CACHE).append(":").append(TAG_ALLOW_CACHE_TRUE).append("\n");
        }
        if (this.playlistType != null && this.playlistType.length() > 0) {
            sb.append(TAG_PLAYLIST_TYPE).append(":").append(TAG_PLAYLIST_TYPE_VOD).append("\n");
        }
        if (isEncrypt() && !hasDecoded) {
            TsSegment seg = this.segments.get(0);
            sb.append(TAG_KEY).append(":").append("METHOD").append("=").append(seg.encryptInfo().encryptedMethod())
                    .append(",URI=\"").append(M3u8.LOCAL_KEY_FILE_NAME).append("\"");
            if (seg.encryptInfo().encryptKeyIV() != null && seg.encryptInfo().encryptKeyIV().length() > 0) {
                sb.append(",IV=").append(seg.encryptInfo().encryptKeyIV());
            }
            sb.append("\n");
        }
        sb.append(TAG_SEQUENCE).append(":").append(this.sequence).append("\n");
        for (TsSegment segment : this.segments) {
            sb.append(TAG_INF).append(":").append(segment.duration()).append(",");
            if (segment.extraInfo() != null && segment.extraInfo().length() > 0) {
                sb.append(segment.extraInfo());
            }
            sb.append("\n").append(segment.localTsName(this.parentSequence)).append("\n");
        }
        return sb.append(TAG_END).toString();
    }

    public boolean isEncrypt() {
        if (this.segments != null && this.segments.size() > 0) {
            TsSegment seg = this.segments.get(0);
            return seg.encryptInfo() != null && !seg.encryptInfo().encryptedMethod().equals("NONE");
        }
        return false;
    }

    public String getKeyUri() {
        if (this.segments != null && this.segments.size() > 0) {
            return this.segments.get(0).encryptInfo().encryptKeyUri();
        }
        return "";
    }

    public String getIV() {
        if (this.segments != null && this.segments.size() > 0) {
            return this.segments.get(0).encryptInfo().encryptKeyIV();
        }
        return "";
    }

    @Override
    public int version() {
        return this.version;
    }

    @Override
    public double maxTsDuration() {
        return this.maxTsDuration;
    }

    @Override
    public int sequence() {
        return this.sequence;
    }

    @Override
    public String playlistType() {
        return this.playlistType;
    }

    @Override
    public boolean isAllowCache() {
        return this.allowCache;
    }

    @Override
    public List<TsSegment> segments() {
        return this.segments;
    }

    public int getPartition() {
        return partition;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public double getMaxTsDuration() {
        return maxTsDuration;
    }

    public void setMaxTsDuration(double maxTsDuration) {
        this.maxTsDuration = maxTsDuration;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getPlaylistType() {
        return playlistType;
    }

    public void setPlaylistType(String playlistType) {
        this.playlistType = playlistType;
    }

    public void setAllowCache(boolean allowCache) {
        this.allowCache = allowCache;
    }

    public List<TsSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<TsSegment> segments) {
        this.segments = segments;
    }

    @Override
    public String toString() {
        return "PartM3u8{" +
                "partition=" + partition +
                ", version=" + version +
                ", maxTsDuration=" + maxTsDuration +
                ", sequence=" + sequence +
                ", playlistType='" + playlistType + '\'' +
                ", allowCache=" + allowCache +
                ", segments=" + segments +
                '}';
    }
}
