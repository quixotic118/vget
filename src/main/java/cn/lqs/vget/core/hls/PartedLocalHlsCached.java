package cn.lqs.vget.core.hls;

import cn.lqs.vget.core.common.utils.FfmpegUtils;
import cn.lqs.vget.core.common.utils.VFileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static cn.lqs.vget.core.common.Constants.MERGE_OUTPUT_NAME;

/**
 * 本地缓存的分片m3u8
 */
@Slf4j
public class PartedLocalHlsCached {

    // 分片目录路径
    private final List<Path> partPaths;

    protected PartedLocalHlsCached(List<Path> partPaths) {
        this.partPaths = partPaths;
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
        Path largestPartPath = null;
        int largestPartSize = 0;
        for (Path partPath : partPaths) {
            File[] pieces = partPath.toFile().listFiles();
            int pieceCount = pieces == null ? 0 : pieces.length;
            if (largestPartPath == null) {
                largestPartPath = partPath;
                largestPartSize = pieceCount;
                continue;
            }
            if (pieceCount > largestPartSize) {
                largestPartPath = partPath;
                largestPartSize = pieceCount;
            }
        }
        return largestPartPath;
    }


    public void mergeLargestPart(FfmpegUtils ffmpegUtils) throws IOException {
        final String dir = getLargestPartPath().toAbsolutePath().toString();

        File[] files = new File(dir).listFiles();
        assert files != null;

        // 先搜集所有 TS 片段
        ArrayList<LocalTsSegFile> sorted = new ArrayList<>(1 << 6);
        for (File file : files) {
            if (file.getName().startsWith(M3u8.LOCAL_SEGMENT_NAME_PREFIX)) {
                String fn = file.getName();
                sorted.add(new LocalTsSegFile(Integer.parseInt(fn.substring(fn.lastIndexOf('-') + 1)), file.getAbsolutePath()));
            }
        }
        // 确保顺序
        sorted.sort(Comparator.comparingInt(LocalTsSegFile::getOrder));
        // 生成 file list
        StringBuilder sb = new StringBuilder();
        for (LocalTsSegFile tsF : sorted) {
            sb.append("file '").append(tsF.getPath()).append("'").append("\n");
        }

        File fileList = Path.of(dir, "file-list.txt").toFile();
        VFileUtils.writeStringToFile(sb.deleteCharAt(sb.length() - 1).toString(), fileList);

        ffmpegUtils.mergeTs(fileList, Path.of(dir, MERGE_OUTPUT_NAME));
    }

    /**
     *删除已经完成合并的分片目录,并将合并后的视频文件移动到分片目录的上一级目录
     * @throws IOException IO异常
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
