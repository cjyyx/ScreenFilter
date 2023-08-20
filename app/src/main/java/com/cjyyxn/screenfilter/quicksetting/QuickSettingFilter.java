package com.cjyyxn.screenfilter.quicksetting;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.cjyyxn.screenfilter.AppConfig;

import java.util.Timer;
import java.util.TimerTask;

public class QuickSettingFilter extends TileService {

    @Override
    public void onCreate() {
        super.onCreate();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    int toggleState = getQsTile().getState();

                    if (AppConfig.isFilterOpenMode()) {
                        // 滤镜打开模式
                        if (toggleState == Tile.STATE_INACTIVE) {
                            // 如果磁贴为未激活状态，改成激活
                            getQsTile().setState(Tile.STATE_ACTIVE);
                        }
                    } else {
                        // 滤镜关闭模式
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

        // 如果磁贴为激活状态 被点击 则动作为关闭滤镜
        if (toggleState == Tile.STATE_ACTIVE) {
            AppConfig.setFilterOpenMode(false);
            Toast.makeText(this, "关闭屏幕滤镜", Toast.LENGTH_SHORT).show();
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        // 如果磁贴为未激活状态 被点击 则动作为开启滤镜
        else if (toggleState == Tile.STATE_INACTIVE) {
            AppConfig.setFilterOpenMode(true);
            Toast.makeText(this, "打开屏幕滤镜", Toast.LENGTH_SHORT).show();
            getQsTile().setState(Tile.STATE_ACTIVE);
        }
        getQsTile().updateTile();
    }

}
