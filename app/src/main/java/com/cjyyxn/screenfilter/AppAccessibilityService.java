package com.cjyyxn.screenfilter;

import android.accessibilityservice.AccessibilityService;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class AppAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 当用户打开无障碍服务时，会执行该方法
     */
    @Override
    public void onServiceConnected() {
        Log.d("ccjy", "无障碍服务启动！！！");

        AppConfig.init(this);

        addLightSensor();
        GlobalStatus.init(this);
//        addTimer();
    }

    private void addLightSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        SensorEventListener lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // 在这里处理光线传感器事件
                GlobalStatus.light = event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // 在这里处理传感器精度变化事件
            }
        };
        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

//    private void addTimer() {
//        AppAccessibilityService appAccessibilityService = this;
//
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                if (GlobalStatus.isFilterOpenMode() && GlobalStatus.light < GlobalStatus.getHighLightThreshold()) {
//                    // 滤镜打开模式，以及光照没有超过阈值的情况下，确保滤镜打开
//                    GlobalStatus.openFilter();
//                }
//            }
//        };
//        timer.schedule(task, 0, 10000);
//    }

    /**
     * 获取系统亮度条的 progress
     */
    public int getSystemBrightnessProgress() {
        int getVal = 0;
        try {
            getVal = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

//        Log.d("ccjy", String.format(
//                "读取到系统亮度条为 %d",
//                getVal
//        ));

        return getVal;
    }

    /**
     * 设置系统亮度条
     */
    public void setSystemBrightnessProgress(int progress) {
        int p = Math.min(AppConfig.SETTING_SCREEN_BRIGHTNESS, Math.max(1, progress));
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, p);
    }

    /**
     * 获取系统亮度条对应的亮度;
     * 记系统亮度条值为 p, 取值 1-128 的整数
     * // ??? b = log2(p)/7
     *
     * @return brightness
     */
    public float getSystemBrightness() {
        float p = getSystemBrightnessProgress();
//        float b = (float) (Math.log(p) / Math.log(2)) / 7;
//        b = Math.max(b, 0);
//        return b;
        return p / ((float) AppConfig.SETTING_SCREEN_BRIGHTNESS);
    }

    /**
     * 通过系统亮度条对应的亮度，设置系统亮度条
     */
    public void setSystemBrightnessProgressByBrightness(float brightness) {

        float b = Math.min(1f, Math.max(0f, brightness));
        int bv = (int) (b * AppConfig.SETTING_SCREEN_BRIGHTNESS + 0.5f);
        setSystemBrightnessProgress(bv);

//        Log.d("ccjy", String.format(
//                "setSystemBrightnessProgress: 设置系统亮度条对应的亮度为 %.1f %%, 注入系统亮度条 %d",
//                brightness * 100, bv
//        ));
    }

    public boolean isReady() {
        return Settings.System.canWrite(this);
    }

}