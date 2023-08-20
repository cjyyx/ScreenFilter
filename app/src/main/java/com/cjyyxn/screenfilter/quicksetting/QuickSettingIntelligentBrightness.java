package com.cjyyxn.screenfilter.quicksetting;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.cjyyxn.screenfilter.AppConfig;

import java.util.Timer;
import java.util.TimerTask;

public class QuickSettingIntelligentBrightness extends TileService {

    @Override
    public void onCreate() {
        super.onCreate();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    int toggleState = getQsTile().getState();

                    if (AppConfig.isIntelligentBrightnessOpenMode()) {
                        // 智能亮度打开模式
                        if (toggleState == Tile.STATE_INACTIVE) {
                            // 如果磁贴为未激活状态，改成激活
                            getQsTile().setState(Tile.STATE_ACTIVE);
                        }
                    } else {
                        // 智能亮度关闭模式
                        if (toggleState == Tile.STATE_ACTIVE) {
                            // 如果磁贴为激活状态，改成未激活
                            getQsTile().setState(Tile.STATE_INACTIVE);
                        }
                    }
                    getQsTile().updateTile();
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        };
        new Timer().schedule(task, 0, 2000);
    }

    // 点击的时候
    @Override
    public void onClick() {

        int toggleState = getQsTile().getState();

        // 如果磁贴为激活状态 被点击 则动作为关闭智能亮度
        if (toggleState == Tile.STATE_ACTIVE) {
            AppConfig.setIntelligentBrightnessOpenMode(false);
            Toast.makeText(this, "关闭智能亮度", Toast.LENGTH_SHORT).show();
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        // 如果磁贴为未激活状态 被点击 则动作为开启智能亮度
        else if (toggleState == Tile.STATE_INACTIVE) {
            AppConfig.setIntelligentBrightnessOpenMode(true);
            Toast.makeText(this, "打开智能亮度", Toast.LENGTH_SHORT).show();
            getQsTile().setState(Tile.STATE_ACTIVE);
        }
        getQsTile().updateTile();
    }
}
