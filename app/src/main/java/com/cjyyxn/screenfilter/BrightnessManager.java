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

    private final ArrayList<float[]> brightnessPointList = new ArrayList<>(); // [light,brightness]
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

    public ArrayList<float[]> getBrightnessPointList() {
        sortBrightnessPointList();
        return brightnessPointList;
    }

    public void addBrightnessPoint(float light, float brightness) {
        float[] floatArray = new float[]{light, brightness};
        brightnessPointList.add(floatArray);
        sortBrightnessPointList();
    }

    public void delBrightnessPoint(float light, float brightness) {
        float[] floatArray = new float[]{light, brightness};

        for (int i = 0; i < brightnessPointList.size(); i++) {
            float[] arr = brightnessPointList.get(i);
            if (arr.length == 2 && arr[0] == floatArray[0] && arr[1] == floatArray[1]) {
                brightnessPointList.remove(i);
                break;
            }
        }
    }

    public void clearBrightnessPointList() {
        brightnessPointList.clear();
    }

    private void sortBrightnessPointList() {
        brightnessPointList.sort((a, b) -> Float.compare(a[0], b[0]));
    }

    public float calculateBrightnessByLight(float light) {
        if(light>GlobalStatus.getHighLightThreshold()){
            return 1f;
        }

        float brightness = 0;
        sortBrightnessPointList();
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
        if (!GlobalStatus.isFilterOpenMode()) {
            return;
        }

        // 实际亮度 = 硬件亮度 * ( 1 - 不透明度 )^2
        // 不透明度 = 1 - sqrt( 实际亮度 / 硬件亮度 )

        float sb;
        float fo;

        if (brightness > GlobalStatus.getMinHardwareBrightness()) {
            sb = brightness;
            fo = 0;
        } else {
            sb = GlobalStatus.getMinHardwareBrightness();
            fo = 1f - (float) Math.sqrt((double) brightness / sb);

            if (fo > GlobalStatus.getMaxFilterOpacity()) {
                fo = GlobalStatus.getMaxFilterOpacity();
            }
        }

//        Log.d("ccjy", String.format(
//                "已知最小硬件亮度 %.1f %%, 最大滤镜不透明度 %.1f %%, 要求设置实际亮度 %.1f %%",
//                GlobalStatus.getMinHardwareBrightness() * 100, GlobalStatus.getMaxFilterOpacity() * 100, brightness * 100
//        ));
//
//        Log.d("ccjy", String.format(
//                "计算得需设置硬件亮度 %.1f %%, 滤镜不透明度 %.1f %%",
//                sb * 100, fo * 100
//        ));

        GlobalStatus.setHardwareBrightness(sb);
        GlobalStatus.setFilterOpacity(fo);

        GlobalStatus.brightness = brightness;
        GlobalStatus.setSystemBrightnessProgressByBrightness(GlobalStatus.brightness);
    }

    private void addTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                if (GlobalStatus.isTempControlMode()) {
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

//                if (intelligentBrightnessState != IntelligentBrightnessState.USER_ADJUSTMENT) {
//
//                } else {
//                    KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//                    if (keyguardManager.isKeyguardLocked()) {
//                        // 设备处于锁屏状态
//                        Log.d("cjy", "设备锁屏");
//                        intelligentBrightnessState = IntelligentBrightnessState.SMOOTH_LIGHT;
//                    } else {
//                        // 设备未处于锁屏状态
//                    }
//                }

                // 有一个问题，系统自动亮度下，不会修改GlobalStatus.brightness TODO
                if (GlobalStatus.isFilterOpenMode()) {
                    // brightnessManageLoop会调用setBrightness，从而修改GlobalStatus.brightness
                    brightnessManageLoop();
                } else {
                    GlobalStatus.brightness = GlobalStatus.getSystemBrightness();
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
        if (GlobalStatus.isIntelligentBrightnessOpenMode()) {
            // 智能亮度开
            switch (intelligentBrightnessState) {
                case SMOOTH_LIGHT:
                    // SMOOTH_LIGHT 状态下，根据光照计算亮度
                    float bset = calculateBrightnessByLight(GlobalStatus.light);

                    if (isSystemBrightnessChanged) {
                        // 反馈用户调节
                        float userb = GlobalStatus.getSystemBrightness();
                        if ((userb - bset) > AppConfig.BRIGHTNESS_ADJUSTMENT_TOLERANCE) {
                            // 用户调节亮度过高
                            keepenBrightness = bset + AppConfig.BRIGHTNESS_ADJUSTMENT_TOLERANCE * AppConfig.BRIGHTNESS_ADJUSTMENT_FACTOR;
                        } else if ((bset - userb) > AppConfig.BRIGHTNESS_ADJUSTMENT_TOLERANCE) {
                            // 用户调节亮度过低
                            keepenBrightness = bset - AppConfig.BRIGHTNESS_ADJUSTMENT_TOLERANCE * AppConfig.BRIGHTNESS_ADJUSTMENT_FACTOR;
                        } else {
                            // 用户调节亮度处于容差之内
                            keepenBrightness = userb;
                        }
                    } else {
                        // 用来稳定亮度
                        if ((bset - keepenBrightness) > AppConfig.BRIGHTNESS_ADJUSTMENT_TOLERANCE) {
                            // 环境光照高于容差
                            keepenBrightness = bset - AppConfig.BRIGHTNESS_ADJUSTMENT_TOLERANCE * AppConfig.BRIGHTNESS_ADJUSTMENT_FACTOR / 2f;
                        } else if ((keepenBrightness - bset) > AppConfig.BRIGHTNESS_ADJUSTMENT_TOLERANCE) {
                            // 环境光照低于于容差
                            keepenBrightness = bset + AppConfig.BRIGHTNESS_ADJUSTMENT_TOLERANCE * AppConfig.BRIGHTNESS_ADJUSTMENT_FACTOR / 2f;
                        }
                    }

                    GlobalStatus.setBrightness(keepenBrightness);

                    // 光照过高，转到系统自动亮度
                    if (GlobalStatus.light > GlobalStatus.getHighLightThreshold()) {
                        // 开启系统自动亮度
                        openSystemAutoBrightnessMode();
                        intelligentBrightnessState = IntelligentBrightnessState.HIGH_LIGHT;
                    }
                    break;
                case HIGH_LIGHT:
                    // HIGH_LIGHT 下，系统自动亮度，当光照过低，转到 SMOOTH_LIGHT
                    if (GlobalStatus.light <= GlobalStatus.getHighLightThreshold()) {
                        // 关闭系统自动亮度
                        closeSystemAutoBrightnessMode();
                        intelligentBrightnessState = IntelligentBrightnessState.SMOOTH_LIGHT;
                    }
                    break;
                case DECREASE_LIGHT:
                    break;
                case INCREASE_LIGHT:
                    break;
                case USER_ADJUSTMENT:
                    break;
            }

        } else {
            // 智能亮度关
            // 阳光模式默认开

            if (GlobalStatus.light > GlobalStatus.getHighLightThreshold()) {
                // 开启自动亮度
                openSystemAutoBrightnessMode();
            } else {
                // 关闭自动亮度
                closeSystemAutoBrightnessMode();
                setBrightness(GlobalStatus.getSystemBrightness());
            }
        }
    }

    private void openSystemAutoBrightnessMode() {
        GlobalStatus.closeFilter();
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
        GlobalStatus.openFilter();
        GlobalStatus.setBrightness(calculateBrightnessByLight(GlobalStatus.light));
    }

    /**
     * 当开启智能亮度后，有以下状态
     * SMOOTH_LIGHT,
     * INCREASE_LIGHT,
     * DECREASE_LIGHT,
     * HIGH_LIGHT,
     * USER_ADJUSTMENT
     */
    private enum IntelligentBrightnessState {
        SMOOTH_LIGHT,
        INCREASE_LIGHT,
        DECREASE_LIGHT,
        HIGH_LIGHT,
        USER_ADJUSTMENT
    }


}
