package com.cjyyxn.screenfilter;

public class AppConfig {

    /**
     * 手机屏幕的最大亮度，单位为 lux
     */
    public static final float MAX_SCREEN_LIGHT = 600f;

    /**
     * Settings.System.SCREEN_BRIGHTNESS 相关的值
     * 安卓开发者文档中说取值是 0-255
     * 但 MIUI14 中是 1-128
     */
    public static final int SETTING_SCREEN_BRIGHTNESS = 128;

    /**
     * 亮度调节容差，与亮度调节算法有关，取值 [0,1]
     */
    public static final float BRIGHTNESS_ADJUSTMENT_TOLERANCE =0.18828f;

    /**
     * 亮度调节系数，与亮度调节算法有关，取值 [0,1]
     */
    public static final float BRIGHTNESS_ADJUSTMENT_FACTOR = 0.71f;

    /**
     * 低光照阈值
     */
    public static final float LOW_LIGHT_THRESHOLD = 5f;
}
