package cn.lqs.vget.core.mp4;

public interface ProgressWatcher {

    static int RATE = 100;

    void watch(long saved, long total);

    void startWatch();

    void stopWatch();

    boolean isRunning();

}
