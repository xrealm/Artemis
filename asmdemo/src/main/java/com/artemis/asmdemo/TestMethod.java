package com.artemis.asmdemo;


/**
 * Created by xrealm on 2020/8/13.
 */
public class TestMethod {

    private int ii = 0;

    @DebugTrace
    private void method1() {
        System.out.println("method1");
    }

    @DebugTrace(targetClass=TestMethod.class, methodName = "Haha")
    public void method2() {
        System.out.println("method2");
    }

    public void Haha() {

        System.out.println();

    }
}
