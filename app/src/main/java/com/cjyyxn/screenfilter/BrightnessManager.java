package com.cjyyxn.screenfilter;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 2 * 2 * 5 一共 20 种状态；
 * 光照改变、系统亮度条被用户拉动、锁屏后开屏 一共三个事件；
 */
public class BrightnessManager {

    Context context;

    private float currentLight;
    private boolean isLightChanged;
    private float currentSystemBrightness;
    private boolean isSystemBrightnessChanged;
    private float keepenBrightness = 0;

    private IntelligentBrightnessState intelligentBrightnessState;

    public BrightnessManager(Context c) {
        context = c;
        intelligentBrightnessState = IntelligentBrightnessState.SMOOTH_LIGHT;
        currentLight = GlobalStatus.light;
        isLightChanged = true;
        currentSystemBrightness = GlobalStatus.getSystemBrightness();
        isSystemBrightnessChanged = true;
//        brightnessManageLoop();
        addTimer();
    }

    public float calculateBrightnessByLight(float light) {

        ArrayList<float[]> brightnessPointList = AppConfig.getBrightnessPointList();

        if (light > AppConfig.getHighLightThreshold()) {
            return 1f;
        }

        float brightness = 0;
        for (int i = 0; i < brightnessPointList.size() - 1; i++) {
            float[] p0 = brightnessPointList.get(i);
            float[] p1 = brightnessPointList.get(i + 1);

            if (p0[0] <= light && light <= p1[0]) {
                if (Float.compare(p0[0], p1[0]) == 0) {
                    brightness = p0[1];
                } else {
                    brightness = p0[1] + ((p1[1] - p0[1]) / (p1[0] - p0[0])) * (light - p0[0]);
                }
                break;
            }
        }
        return brightness;
    }

    /**
     * 设置屏幕实际亮度
     * 当屏幕滤镜模式开时，调用屏幕滤镜设置亮度
     */
    public void setBrightness(float brightness) {
        if (GlobalStatus.getFilterOpacity() < 0f) {
            return;
        }

        // 实际亮度 = 硬件亮度 * ( 1 - 不透明度 )^2
        // 不透明度 = 1 - sqrt( 实际亮度 / 硬件亮度 )

        float sb;
        float fo;

        if (brightness > AppConfig.getMinHardwareBrightness()) {
            sb = brightness;
            fo = 0;
        } else {
            sb = AppConfig.getMinHardwareBrightness();
            fo = 1f - (float) Math.sqrt(Math.max(0f, brightness / sb));

            if (fo > AppConfig.getMaxFilterOpacity()) {
                fo = AppConfig.getMaxFilterOpacity();
            }
        }

//        Log.d("ccjy", String.format(
//                "已知最小硬件亮度 %.1f %%, 最大滤镜不透明度 %.1f %%, 要求设置实际亮度 %.1f %%",
//                GlobalStatus.getMinHardwareBrightness() * 100, GlobalStatus.getMaxFilterOpacity() * 100, brightness * 100
//        ));

//        Log.d("ccjy", String.format(
//                "计算得需设置硬件亮度 %.1f %%, 滤镜不透明度 %.1f %%",
//                sb * 100, fo * 100
//        ));

        GlobalStatus.setHardwareBrightness(sb);
        GlobalStatus.setFilterOpacity(fo);
    }

    private void addTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                if (AppConfig.isTempControlMode()) {
                    return;
                }

                if (Float.compare(currentSystemBrightness, GlobalStatus.getSystemBrightness()) != 0) {
                    // 说明用户调整了状态栏的亮度条
                    Log.d("ccjy", String.format("使用者修改系统亮度条为 %.1f %%", GlobalStatus.getSystemBrightness() * 100));
                    isSystemBrightnessChanged = true;
                    currentSystemBrightness = GlobalStatus.getSystemBrightness();
                }

                if (Float.compare(currentLight, GlobalStatus.light) != 0) {
                    // 传感器检测的光照改变
                    isLightChanged = true;
                    currentLight = GlobalStatus.light;
                }

                if (AppConfig.isFilterOpenMode()) {
                    brightnessManageLoop();
                }

                isSystemBrightnessChanged = false;
                isLightChanged = false;
            }
        };

        timer.schedule(task, 0, 200);
    }


    /**
     * 在屏幕滤镜开启的情况下，应该被调用
     */
    private void brightnessManageLoop() {
        if (AppConfig.isIntelligentBrightnessOpenMode()) {
            // 智能亮度开
            switch (intelligentBrightnessState) {
                case SMOOTH_LIGHT:
                    // SMOOTH_LIGHT 状态下，根据光照计算亮度

                    float bset = calculateBrightnessByLight(GlobalStatus.light);

                    if (isSystemBrightnessChanged) {
                        // 反馈用户调节

                        // 与自动调节相反
                        float btoler_high = bset + AppConfig.getBrightnessAdjustmentDecreaseTolerance() * (0.5f + bset * bset);
                        float btoler_low = bset - AppConfig.getBrightnessAdjustmentIncreaseTolerance() * (0.5f + bset * bset);
                        float userb = currentSystemBrightness;
                        if (userb < btoler_low) {
                            // 用户调节亮度过低
                            keepenBrightness = bset - (bset - btoler_low) * AppConfig.BRIGHTNESS_ADJUSTMENT_FACTOR;
                        } else if (userb > btoler_high) {
                            // 用户调节亮度过高
                            keepenBrightness = bset + (btoler_high - bset) * AppConfig.BRIGHTNESS_ADJUSTMENT_FACTOR;
                        } else {
                            // 用户调节亮度处于容差之内
                            keepenBrightness = userb;
                        }
                    } else {
                        float btoler_high = keepenBrightness + AppConfig.getBrightnessAdjustmentIncreaseTolerance() * (0.5f + keepenBrightness * keepenBrightness);
                        float btoler_low = keepenBrightness - AppConfig.getBrightnessAdjustmentDecreaseTolerance() * (0.5f + keepenBrightness * keepenBrightness);

                        // 用来稳定亮度
                        if ((bset < btoler_low) || (bset > btoler_high)) {
                            keepenBrightness = (bset + keepenBrightness) / 2;
                        }
                    }

                    if ((GlobalStatus.light < AppConfig.LOW_LIGHT_THRESHOLD) && (GlobalStatus.getSystemBrightnessProgress() <= 1)) {
                        // 暗光模式
                        keepenBrightness = 0f;
                    }

                    GlobalStatus.openFilter();
                    GlobalStatus.setBrightness(keepenBrightness);
                    GlobalStatus.setSystemBrightnessProgressByBrightness(keepenBrightness);

                    if (GlobalStatus.light > AppConfig.getHighLightThreshold()) {
                        // 光照过高，转到系统自动亮度
                        GlobalStatus.closeFilter();
                        openSystemAutoBrightnessMode();
                        intelligentBrightnessState = IntelligentBrightnessState.HIGH_LIGHT;
                    } else {
                        // 确保关闭系统自动亮度
                        closeSystemAutoBrightnessMode();
                    }
                    break;
                case HIGH_LIGHT:
                    // HIGH_LIGHT 下，系统自动亮度，当光照过低，转到 SMOOTH_LIGHT
                    if (GlobalStatus.light <= AppConfig.getHighLightThreshold()) {
                        // 关闭系统自动亮度
                        closeSystemAutoBrightnessMode();
                        GlobalStatus.openFilter();
                        intelligentBrightnessState = IntelligentBrightnessState.SMOOTH_LIGHT;
                    }

                    if (GlobalStatus.getSystemBrightness() < 1f) {
                        GlobalStatus.setSystemBrightnessProgressByBrightness(1f);
                    }
                    break;
            }
        } else {
            // 智能亮度关
            // 阳光模式默认开
            GlobalStatus.openFilter();
            GlobalStatus.setBrightness(GlobalStatus.getSystemBrightness());

            if (GlobalStatus.light > AppConfig.getHighLightThreshold()) {
                // 开启自动亮度
                GlobalStatus.closeFilter();
                openSystemAutoBrightnessMode();
            } else {
                // 关闭自动亮度
                closeSystemAutoBrightnessMode();
                GlobalStatus.openFilter();
            }
        }
    }

    private void openSystemAutoBrightnessMode() {
        try {
            // 获取系统亮度模式设置
            ContentResolver contentResolver = context.getContentResolver();
            int mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode != Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                // 自动亮度未开启，开启自动亮度
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                Log.d("ccjy", "开启自动亮度");
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.d("ccjy", "开启自动亮度失败");
        }
    }

    private void closeSystemAutoBrightnessMode() {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            int mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                // 自动亮度已开启，关闭自动亮度
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Log.d("ccjy", "关闭自动亮度");
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.d("ccjy", "关闭自动亮度失败");
        }
    }


    private enum IntelligentBrightnessState {
        SMOOTH_LIGHT,
        HIGH_LIGHT,
    }


}
