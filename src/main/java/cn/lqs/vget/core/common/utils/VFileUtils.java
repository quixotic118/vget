package cn.lqs.vget.core.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class VFileUtils {

    private final static Logger log = LoggerFactory.getLogger(VFileUtils.class);

    public static void copyInputStreamToFile(InputStream is, File dst, byte[] buffer) throws IOException {
        int len;
        try (FileOutputStream fos = new FileOutputStream(dst)){
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        }
    }

    public static byte[] readFileToBytes(File src) throws IOException {
        try (FileInputStream fis = new FileInputStream(src)){
            return fis.readAllBytes();
        }
    }

    public static void writeByteToFile(byte[] bytes, File dst) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dst)){
            fos.write(bytes);
        }
    }

    public static void writeStringToFile(String text, File dst) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dst))){
            bw.write(text);
        }
    }

    public static void translateFile(File src, File dst, boolean delete) throws IOException{
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dst)){
            fis.getChannel().transferTo(0, fis.getChannel().size(), fos.getChannel());
        }
        if (!src.delete()) {
            log.warn("delete file [{}] failed after finish translate!", src.toString());
        }
    }

    public static void clearDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    clearDir(file);
                }
                if (!file.delete()) {
                    log.debug("delete file [{}] failed!", file.toString());
                }
            }
        }
        if (!dir.delete()) {
            log.debug("delete directory [{}] failed!", dir.toString());
        }
    }
}
