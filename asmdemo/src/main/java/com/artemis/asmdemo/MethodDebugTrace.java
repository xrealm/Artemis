package com.artemis.asmdemo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xrealm on 2020/8/13.
 */
public class MethodDebugTrace {

    private static ConcurrentHashMap<String, MethodInfo> map = new ConcurrentHashMap<>();

    public static synchronized void begin(String name) {
        MethodInfo info = new MethodInfo();
        info.start = System.currentTimeMillis();
        map.put(name, info);
        System.out.println(name + " begin");
    }

    public static synchronized void end(String name) {
        MethodInfo info = map.get(name);
        if (info != null) {
            long end = System.currentTimeMillis();
            System.out.println(name + " end. cost:" + (end - info.start));
        } else {
            System.out.println(name + " end. not begin");
        }
    }


    static class MethodInfo {
        long start;
    }
}