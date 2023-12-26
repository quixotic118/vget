package cn.lqs.vget.core.common;

import cn.lqs.vget.core.common.utils.VFileUtils;
import cn.lqs.vget.core.hls.M3u8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static cn.lqs.vget.core.common.Constants.MERGE_OUTPUT_NAME;

@Deprecated
public class Ffmpeg {

    private final static Logger log = LoggerFactory.getLogger(Ffmpeg.class);



    private final String exec;

    private Ffmpeg(String exec) {
        this.exec = exec;
    }

    public static Ffmpeg of(String exec) {
        return new Ffmpeg(exec);
    }

    public void mergeAV(String audio, String video, String output) throws IOException {
        String execCommand = String.format("%s -y -i %s -i %s -c:v copy -c:a aac -strict experimental %s", exec, audio, video, output);
        log.info("Try to execute command -> {}", execCommand);
        Process process = Runtime.getRuntime().exec(execCommand);
        new Thread(() -> {
            try {
                process.getInputStream().transferTo(System.out);
            } catch (IOException e) {
                log.error("error", e);
            }
        }).start();
        new Thread(() -> {
            try {
                process.getErrorStream().transferTo(System.err);
            } catch (IOException e) {
                log.error("error", e);
            }
        }).start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            log.error("process interrupted", e);
        }finally {
            process.destroy();
        }
    }

    public void mergePartM3u8(Path dir) throws Exception {
        if (dir == null) {
            return;
        }
        File dirF = dir.toFile();
        if (dirF.exists() && dirF.isDirectory()) {
            // 生成 file list
            ArrayList<Ts> sorted = new ArrayList<>(1 << 6);
            for (File seg : Objects.requireNonNull(dirF.listFiles())) {
                String fn = seg.getName();
                if (fn.startsWith(M3u8.LOCAL_SEGMENT_NAME_PREFIX)) {
                    int order = Integer.parseInt(fn.substring(fn.lastIndexOf('-') + 1));
                    sorted.add(new Ts(dir.resolve(fn).toAbsolutePath().toString(), order));
                }
            }
            sorted.sort(Comparator.comparingInt(o -> o.order));
            StringBuilder sb = new StringBuilder();
            for (Ts ts : sorted) {
                sb.append("file '").append(ts.name).append("'").append("\n");
            }
            File fileList = dir.resolve("file-list.txt").toFile();
            VFileUtils.writeStringToFile(sb.deleteCharAt(sb.length() - 1).toString(), fileList);
            String[] cmdArr = splitToCmd(exec + " -y -f concat  -safe 0  -i "
                    + fileList.toPath().toAbsolutePath() + " -vcodec copy -acodec copy " + dir.resolve(MERGE_OUTPUT_NAME).toAbsolutePath().toString());
            System.out.println(Arrays.toString(cmdArr));
            Process process = Runtime.getRuntime().exec(cmdArr);
            System.out.println("exec command -> " + process.info().command().orElse(""));
            System.out.println("exec commandLine -> " + process.info().commandLine().orElse(""));
            System.out.println("exec arguments -> " + process.info().arguments());
            log.warn("ffmpeg process-[{}] start...", process.pid());
            new Thread(()->{
                try {
                    try (BufferedReader br = process.inputReader()) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                } catch (IOException e) {
                    log.error("translate input stream to standard failed!", e);
                }
            }).start();
            new Thread(()->{
                try {
                    try (BufferedReader br = process.errorReader()) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.err.println(line);
                        }
                    }
                } catch (IOException e) {
                    log.error("translate output stream to standard failed!", e);
                }
            }).start();
            // 防止出现阻塞
            while (process.isAlive()) {
                log.info("ffmpeg process is running...");
                process.waitFor(5, TimeUnit.SECONDS);
            }
            log.warn("ffmpeg process destroy.");
            process.destroy();
        }
    }

    private String[] splitToCmd(String line) {
        String[] ss = line.split(" ");
        ArrayList<String> list = new ArrayList<>(ss.length);
        for (String s : ss) {
            s = s.trim();
            if (s.length() > 0) {
                list.add(s);
            }
        }
        return list.toArray(new String[0]);
    }

    static record Ts(String name, int order) {
        @Override
        public String toString() {
            return "Ts{" +
                    "name='" + name + '\'' +
                    ", order=" + order +
                    '}';
        }
    }
}
