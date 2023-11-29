package cn.lqs.vget.core.mp4;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static cn.lqs.vget.core.common.utils.VideoHttpUtils.ChunkSize;

public class ConsoleProgressWatcher implements ProgressWatcher{

    private long preTs = System.currentTimeMillis();

    private final static double ChunkSizeMB = ((double) (ChunkSize * ProgressWatcher.RATE)) / (8 * 1024 * 1024);

    private final Thread printer;

    private int progress = 0;
    private double speed = 0.0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition SIGNAL = lock.newCondition();

    private boolean isRunning = false;

    public ConsoleProgressWatcher() {
        printer = new Thread(() -> {
            try {
                for (; ; ) {
                    lock.lock();
                    try {
                        SIGNAL.await();
                        System.out.printf("downloading %2d%%. current network speed %.2f MB/s\n", this.progress, this.speed);
                    }finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("打印线程结束");
            }finally {
                isRunning = false;
            }
        }, "console watcher");
    }

    @Override
    public void startWatch() {
        printer.start();
        isRunning = true;
    }

    @Override
    public void stopWatch() {
        printer.interrupt();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void watch(long saved, long total) {
        this.progress = (int) (((float) saved / total) * 100);
        long currentTs = System.currentTimeMillis();
        this.speed = (ChunkSizeMB / (currentTs - preTs)) * 1000;
        this.preTs = currentTs;
        lock.lock();
        try {
            SIGNAL.signal();
        }finally {
            lock.unlock();
        }

    }


}
