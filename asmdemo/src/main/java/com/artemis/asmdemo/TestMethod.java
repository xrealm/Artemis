package com.artemis.asmdemo;

/**
 * Created by xrealm on 2020/8/13.
 */
public class TestMethod {

    private void method1() {
        System.out.println("method1");
    }

    @DebugTrace
    private void method2() {
        System.out.println("method2");
    }
}
