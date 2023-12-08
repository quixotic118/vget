package cn.lqs.vget.core.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private final static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

    public static String nowStr() {
        return DTF.format(LocalDateTime.now());
    }

    public static String nowDate() {
        return DTF.format(LocalDateTime.now());
    }

}
