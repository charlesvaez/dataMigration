package com.zwzt.migration.redis.test;

import com.zwzt.migration.redis.controller.TestController;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LinkedBlockingQueueTest {
//    private static final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    public static void main(String[] args) {
        final LinkedBlockingQueue queue = TestController.queue();

        new Thread() {
            @Override
            public void run() {
                while (true) {

                    try {
                        String key = System.currentTimeMillis() + "";
                        queue.put(key);
                        System.out.println("put key - " + key);

                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String value = queue.take() + "";
                        System.out.println("take value - " + value);

//                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }.start();


        try {
            TimeUnit.SECONDS.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
