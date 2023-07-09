package com.cjyyxn.screenfilter;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentResolver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Timer;
import java.util.TimerTask;


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

        addTimer();
        addLightSensor();

        GlobalStatus.init(this);
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

    private void addTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                GlobalStatus.systemBrightness = getSystemBrightness();

                // 获取系统亮度模式设置
                ContentResolver contentResolver = getContentResolver();
                int mode = 0;
                try {
                    mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
                    if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                        // 系统自动亮度开启
                        GlobalStatus.brightness=GlobalStatus.systemBrightness;
                    }
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        // 每隔 0.1秒钟执行一次任务
        timer.schedule(task, 0, 100);
    }

    /**
     * 获取系统亮度条的 progress
     *
     * @return progress 取值为 1-128 的整数
     */
    private int getSystemBrightnessProgress() {
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
     * 获取系统亮度条对应的亮度;
     * 记系统亮度条值为 p, 取值 1-128 的整数
     * // ??? b = log2(p)/7
     *
     * @return brightness
     */
    private float getSystemBrightness() {
        float p = getSystemBrightnessProgress();
//        float b = (float) (Math.log(p) / Math.log(2)) / 7;
//        b = Math.max(b, 0);
//        return b;
        return ((float) p)/128f;
    }

    /**
     * 通过系统亮度条对应的亮度，设置系统亮度条
     */
    public void setSystemBrightnessProgressByBrightness(float brightness) {

        int bv = (int) (brightness * 128 + 0.5f);
        ContentResolver contentResolver = getContentResolver();
        Settings.System.putInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, bv);

//        Log.d("ccjy", String.format(
//                "setSystemBrightnessProgress: 设置系统亮度条对应的亮度为 %.1f %%, 注入系统亮度条 %d",
//                brightness * 100, bv
//        ));
    }
    public boolean isReady() {
        return Settings.System.canWrite(this);
    }

}