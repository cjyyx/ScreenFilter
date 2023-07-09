package com.cjyyxn.screenfilter;


import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;

public class GlobalStatus {

    /**
     * 当前环境光照，由 AppAccessibilityService 持续赋值
     */
    public static float light = 0;
    /**
     * 当前屏幕亮度，由 BrightnessManager 持续赋值
     * 当处于系统自动亮度状态时，AppAccessibilityService 也会持续赋值
     */
    public static float brightness = 0;
    /**
     * 系统亮度条对应的亮度, 由 AppAccessibilityService 持续监测并赋值；
     * 用户拖动主界面的屏幕亮度条，设置的是系统亮度条
     */
    public static float systemBrightness = 0;


    private static AppAccessibilityService appAccessibilityService = null;
    @SuppressLint("StaticFieldLeak")
    private static FilterViewManager filterViewManager = null;
    @SuppressLint("StaticFieldLeak")
    private static BrightnessManager brightnessManager = null;

    // 下面是配置相关参数
    private static float highLightThreshold = 4000f;
    private static float minHardwareBrightness = 1f;
    private static float maxFilterOpacity = 0.9f;


    private static boolean filterOpenMode = false;
    private static boolean intelligentBrightnessOpenMode = false;
    private static boolean sunlightPatternOpenMode = true; // 阳光模式默认开启
    /**
     * 临时控制模式
     * 用户测试亮度的效果时，或屏幕截图时，为 true ,此时不会自动改变屏幕亮度
     */
    private static boolean tempControlMode = false;


    public static float getHighLightThreshold() {
        return highLightThreshold;
    }

    public static void setHighLightThreshold(float hlt) {
        highLightThreshold = hlt;
    }

    public static float getMinHardwareBrightness() {
        return minHardwareBrightness;
    }

    public static void setMinHardwareBrightness(float mhb) {
        minHardwareBrightness = mhb;
    }

    public static float getMaxFilterOpacity() {
        return maxFilterOpacity;
    }

    public static void setMaxFilterOpacity(float mfo) {
        maxFilterOpacity = mfo;
    }

    public static ArrayList<float[]> getBrightnessPointList() {
        if (brightnessManager != null) {
            return brightnessManager.getBrightnessPointList();
        } else {
            return null;
        }
    }

    public static void addBrightnessPoint(float light, float brightness) {
        if (brightnessManager != null) {
            brightnessManager.addBrightnessPoint(light, brightness);
        }
    }

    public static void delBrightnessPoint(float light, float brightness) {
        if (brightnessManager != null) {
            brightnessManager.delBrightnessPoint(light, brightness);
        }
    }

    public static boolean isFilterOpenMode() {
        return filterOpenMode;
    }

    public static void setFilterOpenMode(boolean filterOpenMode) {
        if (isReady()) {
            GlobalStatus.filterOpenMode = filterOpenMode;
            if (!filterOpenMode) {
                closeFilter();
                setIntelligentBrightnessOpenMode(false);
            }
        } else {
            GlobalStatus.filterOpenMode = false;
        }
    }

    public static boolean isIntelligentBrightnessOpenMode() {
        return intelligentBrightnessOpenMode;
    }

    public static void setIntelligentBrightnessOpenMode(boolean intelligentBrightnessOpenMode) {
        if (isReady() && isFilterOpenMode()) {
            GlobalStatus.intelligentBrightnessOpenMode = intelligentBrightnessOpenMode;
        } else {
            GlobalStatus.intelligentBrightnessOpenMode = false;
        }
    }

    public static boolean isSunlightPatternOpenMode() {
        return sunlightPatternOpenMode;
    }

    public static void setSunlightPatternOpenMode(boolean sunlightPatternOpenMode) {
        GlobalStatus.sunlightPatternOpenMode = sunlightPatternOpenMode;
    }

    public static boolean isTempControlMode() {
        return tempControlMode;
    }

    public static void setTempControlMode(boolean tempControlMode) {
        GlobalStatus.tempControlMode = tempControlMode;
    }

    /**
     * Global 初始化
     */
    public static void init(AppAccessibilityService a) {
        appAccessibilityService = a;
        filterViewManager = new FilterViewManager(a);
        brightnessManager = new BrightnessManager(a);
        setTempControlMode(false);
        loadDefaultConfig();

        if (isFilterOpenMode()) {
            openFilter();
        }
    }

    public static void loadDefaultConfig() {
        setFilterOpenMode(true);
        setIntelligentBrightnessOpenMode(true);
        setSunlightPatternOpenMode(true);
        setHighLightThreshold(4000f);
        setMaxFilterOpacity(0.95f);
        setMinHardwareBrightness(0.5f);

        if (brightnessManager != null) {
            brightnessManager.clearBrightnessPointList();
            addBrightnessPoint(0, 0.05f);
            addBrightnessPoint(10, 0.1f);
            addBrightnessPoint(30, 0.2f);
            addBrightnessPoint(42, 0.34f);
            addBrightnessPoint(60, 0.4f);
            addBrightnessPoint(250, 0.6f);
            addBrightnessPoint(1000, 1f);
            addBrightnessPoint(GlobalStatus.getHighLightThreshold(), 1f);
        }
    }


//    /**
//     * 当传感器获得的光照强度变化后调用
//     */
//    public static void onLightChanged(float l) {
//        light = l;
//        if (isReady()) {
//            brightnessManager.onLightChanged(light);
//        }
//    }

    /**
     * 当无障碍服务已打开，应用所需的各权限都满足后，返回 true
     */
    public static boolean isReady() {
        return isAccessibility() && appAccessibilityService.isReady();
    }

    /**
     * 当无障碍服务已打开, 返回 true
     */
    public static boolean isAccessibility() {
        return (appAccessibilityService != null);
    }

    public static void openFilter() {
        if (isReady()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // 在UI线程中更新UI组件
                filterViewManager.open();
            });
        }
    }

    public static void closeFilter() {
        if (isReady()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // 在UI线程中更新UI组件
                filterViewManager.close();
            });
        }
    }

    public static float calculateBrightnessByLight(float light) {
        return brightnessManager.calculateBrightnessByLight(light);
    }

    public static float getFilterOpacity() {
        return filterViewManager.getAlpha();
    }

    /**
     * 设置不透明度;
     *
     * @param alpha 0 表示完全透明，1 表示完全不透明
     */
    public static void setFilterOpacity(float alpha) {
        if (isReady()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // 在UI线程中更新UI组件
                filterViewManager.setAlpha(alpha);
            });
        }
    }

    public static float getHardwareBrightness() {
        return filterViewManager.getHardwareBrightness();
    }

    /**
     * 利用 layoutParams.screenBrightness 设置硬件亮度，覆盖状态栏系统亮度条的效果;
     *
     * @param brightness [0,1]
     */
    public static void setHardwareBrightness(float brightness) {
        if (isReady()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // 在UI线程中更新UI组件
                filterViewManager.setHardwareBrightness(brightness);
            });
        }
    }

    /**
     * 设置实际亮度，会自动计算滤镜不透明度和硬件亮度，并设置
     *
     * @param brightness [0,1]
     */
    public static void setBrightness(float brightness) {
        if (isReady() && filterViewManager.isOpen) {
            brightnessManager.setBrightness(brightness);
        }
    }


    /**
     * 通过亮度值控制状态栏系统亮度条
     * 当屏幕滤镜打开时，状态栏系统亮度条会被滤镜的亮度值覆盖，不会关闭硬件亮度
     *
     * @param brightness [0,1]
     */
    public static void setSystemBrightnessProgressByBrightness(float brightness) {
        if (isReady()) {
            appAccessibilityService.setSystemBrightnessProgressByBrightness(brightness);
        }
    }

    /**
     * 触发屏幕截图
     * 先模拟上划操作，从而上拉任务栏
     * 延时后打开系统截图服务
     * 可能需要 500 ms
     */
    public static void triggerScreenCap() {
        if (appAccessibilityService != null) {

            // 模拟上划操作，从而上拉任务栏
            Path path = new Path();
            path.moveTo(540, 2300);
            path.lineTo(540, 2000);

            GestureDescription gestureDescription = new GestureDescription.Builder().addStroke(
                    new GestureDescription.StrokeDescription(path, 0, 50
                    )).build();

            Log.d("ccjy", "模拟上滑操作");
            long time1 = System.currentTimeMillis();
            appAccessibilityService.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    // 手势事件执行完成后的回调方法
                    long time2 = System.currentTimeMillis();
                    long diffMillis = time2 - time1;
                    Log.d("ccjy", String.format(
                            "手势事件执行用了 %d ms", diffMillis
                    ));
                }
            }, null);

            // 延时后打开系统截图服务
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("ccjy", "开启系统截图服务");
                    appAccessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
                }
            }, 200);
        }
    }


}


