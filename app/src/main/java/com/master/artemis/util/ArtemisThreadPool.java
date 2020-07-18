package com.master.artemis.util;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xrealm on 2020/7/17.
 */
public class ArtemisThreadPool {

    private static ExecutorService sThreadPool = new ThreadPoolExecutor(4, 10,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new ArtemisThreadFactory(), new ArtemisRejectedExecutionHandler());

    public static ExecutorService getThreadPool() {
        return sThreadPool;
    }

    private static class ArtemisThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public ArtemisThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "Art-pool-" + poolNumber.getAndIncrement() + "-t-";
        }

        @Override
        public Thread newThread(Runnable r) {

            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    private static class ArtemisRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Log.i("ArtemisThreadPool", "rejectedExecution: " + executor.getTaskCount());
        }
    }
}
