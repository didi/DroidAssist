package com.didichuxing.tools.test;


public class Child extends Parent {

    //protected int age;

    public Child(int age) {
        super(age);
    }

    @Override
    protected void methodB() {
        super.methodB();
        int b = age;
        int a = super.age;
        super.age = 10;
        System.out.println("methodB" + a);
    }

    public static void main(String[] args) {
        Child child = new Child(1);
        child.methodB();
        child.methodA();
        System.out.println("child super class: " + child.getClass().getSuperclass());
        System.out.println();
    }
}
