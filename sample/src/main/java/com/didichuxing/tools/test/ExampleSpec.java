package com.didichuxing.tools.test;

@SuppressWarnings("WeakerAccess")
public final class ExampleSpec implements IInterface {

    public static String name = "test";
    public int id = 0;
    public int id2 = 0;

    public static ExampleSpec instance = new ExampleSpec();

    public ExampleSpec() {
        System.out.println("<init>");
    }

    public ExampleSpec(int i) {
        System.out.println("ExampleSpec(" + i + ")");
    }

    public ExampleSpec(int i, int y) {
        System.out.println("ExampleSpec(" + i + "," + y + ")");
    }

    @TestAnnotation
    public ExampleSpec(int i, int y, int z) {
        System.out.println("ExampleSpec(" + i + "," + y + "," + z + ")");
    }

    public ExampleSpec(String string) {
        System.out.println(string);
    }

    public static ExampleSpec getInstance() {
        return instance;
    }

    public void run() {
        System.out.println("run");
    }

    public void call() {
        System.out.println("call");
    }

    public void uncaught() {
        System.out.println("uncaught");
    }

    @TestAnnotation3
    public void uncaught2() {
        System.out.println("uncaught2");
        int i = 1 / 0;
    }

    public void timing() {
        System.out.println("timing");
    }

    public int timing2() {
        System.out.println("timing");
        return 0;
    }

    public int timing3() {
        System.out.println("timing");
        return 0;
    }

    @TestAnnotation(intValue = 100, strValue = "constructor")
    public void test() {
        System.out.println("test");
    }

    @TestAnnotation2(intValue = 200)
    public void test2() {
        System.out.println("test2");
    }

    @Override
    public void onCall() {
        System.out.println("onCall");
    }
}
