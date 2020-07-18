package com.artemis.cv.util;

import java.util.concurrent.ExecutorService;

/**
 * Created by xrealm on 2020/7/17.
 */
public class FaceThreadPool {

    private static ExecutorService sThreadPool;

    public static void setThreadPool(ExecutorService executorService) {
        sThreadPool = executorService;
    }

    public static void execute(Runnable runnable) {
        if (sThreadPool != null) {
            sThreadPool.execute(runnable);
        }
    }
}
