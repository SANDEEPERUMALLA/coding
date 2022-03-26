package com.threading;

public class SampleThread implements Runnable {

    @Override
    public void run() {

        while (true) {
            if( Thread.currentThread().isInterrupted()) {
                System.out.println("Thread interrupted !!!");
                System.out.println("Exiting");
                break;
            }
            System.out.println("Running !!!");
        }

    }
}
