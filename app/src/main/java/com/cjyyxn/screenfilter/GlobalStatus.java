package com.cjyyxn.screenfilter;


import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

@SuppressLint("StaticFieldLeak")
public class GlobalStatus {

    /**
     * 当前环境光照，由 AppAccessibilityService 持续赋值
     */
    public static float light = 0;

    private static AppAccessibilityService appAccessibilityService = null;
    private static FilterViewManager filterViewManager = null;
    private static BrightnessManager brightnessManager = null;


    /**
     * Global 初始化
     */
    public static void init(AppAccessibilityService a) {
        appAccessibilityService = a;
        filterViewManager = new FilterViewManager(a);
        brightnessManager = new BrightnessManager(a);

        if (AppConfig.isFilterOpenMode()) {
            openFilter();
        }
    }

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
        if (isReady() && filterViewManager.getHardwareBrightness() < 0f) {
            filterViewManager.open();
        }
    }

    public static void closeFilter() {
        if (isReady() && filterViewManager.getHardwareBrightness() >= 0) {
            filterViewManager.close();
        }
    }

    public static float calculateBrightnessByLight(float light) {
        return brightnessManager.calculateBrightnessByLight(light);
    }

    /**
     * 获取屏幕滤镜的不透明度
     * 滤镜未打开，返回 -1
     * 未初始化，返回 -2
     */
    public static float getFilterOpacity() {
        if (filterViewManager != null) {
            return filterViewManager.getAlpha();
        } else {
            return -2f;
        }
    }

    /**
     * 设置不透明度;
     *
     * @param alpha 0 表示完全透明，1 表示完全不透明
     */
    public static void setFilterOpacity(float alpha) {
        if (isReady()) {
            filterViewManager.setAlpha(alpha);
        }
    }

    /**
     * 获取屏幕滤镜的亮度
     * 滤镜未打开，返回 -1
     * 未初始化，返回 -2
     */
    public static float getHardwareBrightness() {
        if (filterViewManager != null) {
            return filterViewManager.getHardwareBrightness();
        } else {
            return -2f;
        }
    }

    /**
     * 利用 layoutParams.screenBrightness 设置硬件亮度，覆盖状态栏系统亮度条的效果;
     *
     * @param brightness [0,1]
     */
    public static void setHardwareBrightness(float brightness) {
        if (isReady()) {
            filterViewManager.setHardwareBrightness(brightness);
        }
    }


    /**
     * 获取系统亮度条对应的亮度
     */
    public static float getSystemBrightness() {
        if (isReady()) {
            return appAccessibilityService.getSystemBrightness();
        } else {
            return 0;
        }
    }

    /**
     * 获取系统亮度条值
     * 与 AppConfig.SETTING_SCREEN_BRIGHTNESS 有关
     */
    public static int getSystemBrightnessProgress() {
        if (isReady()) {
            return appAccessibilityService.getSystemBrightnessProgress();
        } else {
            return 0;
        }
    }

    /**
     * 设置系统亮度条
     * 与 AppConfig.SETTING_SCREEN_BRIGHTNESS 有关
     */
    public static void setSystemBrightnessProgress(int progress) {
        if (isReady()) {
            appAccessibilityService.setSystemBrightnessProgress(progress);
        }
    }

    /**
     * 通过亮度值控制状态栏系统亮度条
     * 当屏幕滤镜打开时，状态栏系统亮度条会被滤镜的亮度值覆盖，不会改变硬件亮度
     *
     * @param brightness [0,1]
     */
    public static void setSystemBrightnessProgressByBrightness(float brightness) {
        if (isReady()) {
            appAccessibilityService.setSystemBrightnessProgressByBrightness(brightness);
        }
    }

    /**
     * 获取当前屏幕亮度
     */
    public static float getBrightness() {
        if (getFilterOpacity() >= 0) {
            // 实际亮度 = 硬件亮度 * ( 1 - 不透明度 )^2
            return getHardwareBrightness() * (float) Math.pow((1 - getFilterOpacity()), 2);
        } else {
            return getSystemBrightness();
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
     * 触发屏幕截图
     * 先模拟上划操作，从而上拉任务栏
     * 延时后打开系统截图服务
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
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d("ccjy", "开启系统截图服务");
                appAccessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
            }, 500);
        }
    }


}


