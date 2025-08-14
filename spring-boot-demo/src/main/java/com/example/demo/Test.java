package com.example.demo;

public class Test {


    private int x = 1;
    private static int bbb = 123;
    private int y = 100;
    private int z = 100000;
    private String name = "shouzhi";

    public static void sum(int  n, int m){
		int a = 1;
		int b = 2;
		int c = a + b;
		int d = c + n + m;
		System.out.println("c: " + c);
		System.out.println("d: " + d);
	}

    public static void main(String[] args) {
        sum(1, 2);
    }


    




}
