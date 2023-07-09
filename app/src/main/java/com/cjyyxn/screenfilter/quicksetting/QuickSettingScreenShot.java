package com.cjyyxn.screenfilter.quicksetting;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.TileService;

import com.cjyyxn.screenfilter.GlobalStatus;

public class QuickSettingScreenShot extends TileService {
    @Override
    public void onClick() {
        GlobalStatus.setTempControlMode(true);
        GlobalStatus.closeFilter();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                GlobalStatus.triggerScreenCap();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GlobalStatus.setTempControlMode(false);
                    }
                }, 800);
            }
        }, 400);
    }
}