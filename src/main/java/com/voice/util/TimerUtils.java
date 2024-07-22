package com.voice.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimerUtils {
    private static ScheduledThreadPoolExecutor s=new ScheduledThreadPoolExecutor(10);
    public static void startTimer(Integer delay, Integer period, Runnable task) {
        s.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
    }

    public static void stopTimer() {
        s.shutdownNow();
        s = new ScheduledThreadPoolExecutor(4);
    }
}
