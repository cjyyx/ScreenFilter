package com.cjyyxn.screenfilter.quicksetting;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.TileService;

import com.cjyyxn.screenfilter.AppConfig;
import com.cjyyxn.screenfilter.GlobalStatus;

public class QuickSettingScreenShot extends TileService {
    @Override
    public void onClick() {
        AppConfig.setTempControlMode(true);
        GlobalStatus.closeFilter();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            GlobalStatus.triggerScreenCap();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                AppConfig.setTempControlMode(false);
            }, 1300);
        }, 400);
    }
}