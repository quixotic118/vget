package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.common.utils.CryptoUtils;
import cn.lqs.vget.core.common.utils.VFileUtils;
import cn.lqs.vget.core.exceptions.DecodeFailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class HlsLocalCached {

    private final static Logger log = LoggerFactory.getLogger(HlsLocalCached.class);

    private final HlsVGet hlsVGet;
    private final long downloadCosts;

    private int countPartitionCache = 0;

    public HlsLocalCached(HlsVGet hlsVGet, long downloadCosts) {
        this.hlsVGet = hlsVGet;
        this.downloadCosts = downloadCosts;
    }

    public boolean isAllCached() {
        return hlsVGet.getFailedSegments() == null || hlsVGet.getFailedSegments().size() == 0;
    }

    public void retryFailedDownload() {
        this.hlsVGet.retryFailedSegments();
    }

    public long getDownloadCosts() {
        return downloadCosts;
    }

    public String getCachedDir() {
        return this.hlsVGet.getCacheDir();
    }

    public int countPartition() {
        if (countPartitionCache == 0) {
            int max = 0;
            for (Segment segment : this.hlsVGet.getNetM3u8().segments()) {
                if (segment.partition() > max) {
                    max = segment.partition();
                }
            }
            countPartitionCache = max + 1;
        }
        return countPartitionCache;
    }

    private List<PartM3u8> partition() {
        Map<Integer, List<Segment>> sortMap = new HashMap<>();
        for (Segment segment : this.hlsVGet.getNetM3u8().segments()) {
            if (!sortMap.containsKey(segment.partition())) {
                sortMap.put(segment.partition(), new ArrayList<Segment>(1 << 4));
            }
            sortMap.get(segment.partition()).add(segment);
        }
        List<PartM3u8> partM3u8s = new ArrayList<>(1 << 2);
        for (Map.Entry<Integer, List<Segment>> entry : sortMap.entrySet()) {
            NetM3u8 netM3u8 = this.hlsVGet.getNetM3u8();
            PartM3u8 partM3u8 = new PartM3u8(entry.getKey(), netM3u8.sequence());
            partM3u8.setAllowCache(true);
            partM3u8.setVersion(netM3u8.version());
            partM3u8.setSegments(entry.getValue());
            partM3u8.setSequence(0);
            partM3u8.setPlaylistType(M3u8Tags.TAG_PLAYLIST_TYPE_VOD);
            partM3u8.setMaxTsDuration(netM3u8.maxTsDuration());
            partM3u8s.add(partM3u8);
        }
        return partM3u8s;
    }

    public PartedLocalHlsCached standardizePartition(){
        return standardizePartition(false);
    }

    /**
     * standardize the partitions
     * @param autoDecode enable auto decode?
     * @return {@link PartedLocalHlsCached}
     */
    public PartedLocalHlsCached standardizePartition(boolean autoDecode) {
        String cachedDir = getCachedDir();
        List<Path> partDirs = new ArrayList<>(1 << 2);
        for (PartM3u8 partM3u8 : partition()) {
            try {
                Path dir = Path.of(cachedDir, "part-" + partM3u8.getPartition());
                if (!dir.toFile().exists() && !dir.toFile().mkdirs()) {
                    throw new IOException("create dir fail! " + dir.toString());
                }
                byte[] bytes = null;
                byte[] iv = null;
                if (partM3u8.isEncrypt()) {
                    bytes = this.hlsVGet.fetchKeyBytes(partM3u8.getKeyUri());
                    if (!autoDecode) {
                        // 写入加密文件
                        Path fileKey = dir.resolve(M3u8.LOCAL_KEY_FILE_NAME);
                        VFileUtils.writeByteToFile(bytes, fileKey.toFile());
                    } else {
                        iv = extractIVBytes(partM3u8.getIV());
                    }
                }
                // translate segment
                for (Segment segment : partM3u8.getSegments()) {
                    String fn = segment.localSegName(this.hlsVGet.getNetM3u8().sequence());
                    File srcF = Path.of(cachedDir, fn).toFile();
                    if (!srcF.exists()) {
                        continue;
                    }
                    if (autoDecode && partM3u8.isEncrypt()) {
                        byte[] enc = VFileUtils.readFileToBytes(srcF);
                        byte[] dec = CryptoUtils.aesDecode(enc, bytes, iv);
                        VFileUtils.writeByteToFile(dec, dir.resolve(fn).toFile());
                        if (!srcF.delete()) {
                            log.warn("source file delete failed! [{}]", srcF);
                        }
                        partM3u8.setHasDecoded(true);
                        continue;
                    }
                    VFileUtils.translateFile(srcF, dir.resolve(fn).toFile(), true);
                }
                // 写入 index 文件
                VFileUtils.writeStringToFile(partM3u8.toM3u8String(), dir.resolve(M3u8.LOCAL_INDEX_FILE_NAME).toFile());
                partDirs.add(dir);
            } catch (IOException | InterruptedException | DecodeFailException e) {
                log.error("standardize partition-{} failed!", partM3u8.getPartition(), e);
            }
        }
        return new PartedLocalHlsCached(partDirs, hlsVGet);
    }

    private byte[] extractIVBytes(String iv) throws DecodeFailException {
        if (iv == null || iv.length() == 0) {
            log.debug("set IV to zero sixteen bytes.");
            return new byte[16];
        }
        iv = iv.trim();
        iv = iv.startsWith("0x") ? iv.substring(2) : iv;
        if (iv.length() != 32) {
            throw new DecodeFailException("IV length need 32 but got " + iv.length());
        }
        return HexFormat.of().withUpperCase().parseHex(iv);
    }

}
