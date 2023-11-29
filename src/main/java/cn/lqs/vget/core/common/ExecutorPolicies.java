package cn.lqs.vget.core.common;

import java.util.concurrent.ExecutorService;

public abstract class ExecutorPolicies {

    public static VGetExecutor SINGLE_THREAD = new VGetExecutor() {
        @Override
        public int maxConcurrency() {
            return 1;
        }

        @Override
        public ExecutorService getExecutorService() {
            return null;
        }

    };

}
