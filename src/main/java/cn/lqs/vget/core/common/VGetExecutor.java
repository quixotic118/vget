package cn.lqs.vget.core.common;

import java.util.concurrent.ExecutorService;

public interface VGetExecutor {

    int maxConcurrency();

    ExecutorService getExecutorService();
}
