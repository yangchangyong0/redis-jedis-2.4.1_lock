package com;

/**
 * Created by ycy on 16/3/31.
 */
public class Test {
  private static   class  TestThread extends Thread{
        @Override
        public void run() {
            while (true){
                System.out.println(2);
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            Thread s=new TestThread();
            s.start();
        }

    }
}
