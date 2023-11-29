package cn.lqs.vget.core.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public final class FfmpegUtils {

    private final String execPath;

    private FfmpegUtils(String execPath) {
        this.execPath = execPath;
    }

    public static FfmpegUtils create(String execPath) {
        return new FfmpegUtils(execPath);
    }


    private void checkExecPath() {
        // todo check can be executed.
    }

    public void mergeTs(File fileList, Path output) throws IOException {
        exec(execPath, "-y", "-f", "concat", "-safe", "0", "-i", fileList.getAbsolutePath(), "-vcodec", "copy", "-acodec", "copy", output.toAbsolutePath().toString());
    }

    private void exec(String... cmd) throws IOException {
        Process process = Runtime.getRuntime().exec(cmd);
        log.info("Ffmpeg process running with PID [{}]", process.pid());
        handleProcessOutput(process, true);
        handleProcessOutput(process, false);
        try {
            log.info("Ffmpeg process exit with code {}", process.waitFor());
        } catch (InterruptedException e) {
            log.error("Ffmpeg process interrupted", e);
        } finally {
            if (process.isAlive()) {
                process.destroy();
            }
        }
    }

    private void handleProcessOutput(Process process, boolean errOutput) {
        new Thread(() -> {
            try (BufferedReader br = errOutput ? process.errorReader() : process.inputReader()) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                log.error("读取 Ffmepg 输出流异常", e);
            }
        }).start();
    }

}
