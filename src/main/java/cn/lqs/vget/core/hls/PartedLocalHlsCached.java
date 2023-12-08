package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.common.utils.FfmpegUtils;
import cn.lqs.vget.core.common.utils.VFileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cn.lqs.vget.core.common.Constants.MERGE_ALL_SEGMENTS_NAME;
import static cn.lqs.vget.core.common.Constants.MERGE_OUTPUT_NAME;

/**
 * 本地缓存的分片m3u8
 */
@Slf4j
public class PartedLocalHlsCached {

    // 分片目录路径
    private final List<Path> partPaths;
    private final HlsVGet hlsVGet;

    protected PartedLocalHlsCached(List<Path> partPaths, HlsVGet hlsVGet) {
        this.partPaths = partPaths;
        this.hlsVGet = hlsVGet;
    }

    /**
     * 获取分片路径列表
     * @return 分片路径列表
     */
    public List<Path> getPartPaths() {
        return partPaths;
    }

    /**
     * 获取分片路径
     * @param part 分片序号
     * @return 分片路径
     */
    public Path getPartPath(int part) {
        return partPaths.get(part);
    }

    /**
     * 获取分片数量
     * @return 分片数量
     */
    public int countPart() {
        return partPaths.size();
    }

    /**
     * 获取拥有最多 TS 分片文件的 hls 视频块
     * @return {@link Path}
     */
    public Path getLargestPartPath() {
        File[] fs = new File(this.hlsVGet.getCacheDir()).listFiles();
        assert fs != null;
        int maxNum = 0;
        Path maxPath = null;
        for (File f : fs) {
            File[] subFs = f.listFiles();
            int num = subFs == null ? 0 : subFs.length;
            if (num > maxNum) {
                maxNum = num;
                maxPath = Path.of(this.hlsVGet.getCacheDir(), f.getName());
            }
        }
        return maxPath;
    }

    /**
     * list and sort all local segment files in the specified direction
     * @param dir the specified direction
     * @return sorted segments
     */
    private ArrayList<LocalSegment> getSortedLocalSegments(Path dir) {
        File[] files = dir.toFile().listFiles();
        assert files != null;

        // first search all segments
        ArrayList<LocalSegment> sorted = new ArrayList<>(1 << 6);
        for (File file : files) {
            if (file.getName().startsWith(M3u8.LOCAL_SEGMENT_NAME_PREFIX)) {
                String fn = file.getName();
                sorted.add(new LocalSegment(Integer.parseInt(fn.substring(fn.lastIndexOf('-') + 1)), file.getAbsolutePath()));
            }
        }
        // make sure the order
        sorted.sort(Comparator.comparingInt(LocalSegment::getOrder));
        return sorted;
    }

    public void mergeLargestPart(FfmpegUtils ffmpegUtils) throws IOException {
        Path largestPartPath = getLargestPartPath();
        String dir = largestPartPath.toAbsolutePath().toString();
        // generate file list
        StringBuilder sb = new StringBuilder();
        for (LocalSegment tsF : getSortedLocalSegments(largestPartPath)) {
            sb.append("file '").append(tsF.getPath()).append("'").append("\n");
        }

        File fileList = Path.of(dir, "file-list.txt").toFile();
        VFileUtils.writeStringToFile(sb.deleteCharAt(sb.length() - 1).toString(), fileList);

        ffmpegUtils.mergeTs(fileList, Path.of(dir, MERGE_OUTPUT_NAME));
    }

    public void merge2OneFile(String type) throws Exception{
        merge2OneFile(getLargestPartPath(), type);
    }
    /**
     *
     */
    public void merge2OneFile(Path dir, String type) throws Exception {
        byte[] videoHeadBytes = this.hlsVGet.fetchVideoHead();
        File tarF = dir.resolve(MERGE_ALL_SEGMENTS_NAME + "." + type).toFile();
        ArrayList<LocalSegment> localSegments = getSortedLocalSegments(dir);
        try (FileOutputStream fos = new FileOutputStream(tarF)){
            fos.write(videoHeadBytes);
            for (LocalSegment seg : localSegments) {
                try (FileInputStream fis = new FileInputStream(new File(seg.getPath()))){
                    fos.write(fis.readAllBytes());
                }
            }
        }
    }

    /**
     * Delete the merged shard directory and move the merged video files to the directory one level above the shard directory
     * @throws IOException IO Exception
     */
    public String clearMergedPartDirAndGetFinalVideo(String filename) throws IOException {
        for (Path partPath : partPaths) {
            File mergeMp4 = partPath.resolve(MERGE_OUTPUT_NAME).toFile();
            if (mergeMp4.exists()) {
                filename = (filename == null || "".equals(filename)) ? String.format("%s_%d.mp4", partPath.getFileName(), System.currentTimeMillis()) : filename;
                Path target = partPath.getParent().resolve(filename);
                File targetF = target.toFile();
                VFileUtils.translateFile(mergeMp4, targetF, true);
                VFileUtils.clearDir(partPath.toFile());
                return targetF.getAbsolutePath();
            }
        }
        return null;
    }
}
