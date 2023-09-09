package com.cjyyxn.screenfilter.utils;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class TimerControl {
    private final Runnable timerTask;
    private Timer timer = null;
    private boolean isRunning = false;

    public TimerControl(Runnable timerTask) {
        this.timerTask = timerTask;
        isRunning = false;
    }

    public void start(
            long delay,
            long period
    ) {
        if (!isRunning) {
            isRunning = true;
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
//                    Log.d("ccjy", "timer 被调用");
                    timerTask.run();
                }
            };
            timer.schedule(task, delay, period);
        }
    }

    public void stop() {
        if (isRunning && (timer != null)) {
            isRunning = false;
            timer.cancel();
        }
    }

}
