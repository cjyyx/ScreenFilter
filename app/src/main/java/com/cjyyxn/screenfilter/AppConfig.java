package com.cjyyxn.screenfilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

@SuppressLint("StaticFieldLeak")
public class AppConfig {

    // 一些常数
    /**
     * 手机屏幕的最大亮度，单位为 nit
     */
    public static final float MAX_SCREEN_LIGHT = 500f;
    /**
     * Settings.System.SCREEN_BRIGHTNESS 相关的值
     * 安卓开发者文档中说取值是 0-255
     * 但 MIUI14 中是 1-128
     */
    public static final int SETTING_SCREEN_BRIGHTNESS = 128;
    /**
     * 亮度调节容差，与亮度调节算法有关，取值 [0,1]
     */
    public static final float BRIGHTNESS_ADJUSTMENT_TOLERANCE = 0.18828f;
    /**
     * 亮度调节系数，与亮度调节算法有关，取值 [0,1]
     */
    public static final float BRIGHTNESS_ADJUSTMENT_FACTOR = 0.71f;
    /**
     * 低光照阈值，单位 lux
     */
    public static final float LOW_LIGHT_THRESHOLD = 5f;


    // 默认配置
    private static final float default_highLightThreshold = 4000f;
    private static final float default_minHardwareBrightness = 0.5f;
    private static final float default_maxFilterOpacity = 0.9f;
    private static final boolean default_filterOpenMode = true;
    private static final boolean default_intelligentBrightnessOpenMode = false;
    private static final boolean default_hideInMultitaskingInterface = false;

    private static Context context = null;
    private static SharedPreferences shared = null;
    private static SharedPreferences.Editor editor = null;


    // 配置
    private static float highLightThreshold;
    private static float minHardwareBrightness;
    private static float maxFilterOpacity;
    /**
     * 列表内元素为 [light,brightness]
     */
    private static ArrayList<float[]> brightnessPointList;
    private static boolean filterOpenMode;
    private static boolean intelligentBrightnessOpenMode;
    private static boolean hideInMultitaskingInterface;
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
        editor.putFloat("highLightThreshold", highLightThreshold);
        editor.apply();
    }

    public static float getMinHardwareBrightness() {
        return minHardwareBrightness;
    }

    public static void setMinHardwareBrightness(float mhb) {
        minHardwareBrightness = mhb;
        editor.putFloat("minHardwareBrightness", minHardwareBrightness);
        editor.apply();
    }

    public static float getMaxFilterOpacity() {
        return maxFilterOpacity;
    }

    public static void setMaxFilterOpacity(float mfo) {
        maxFilterOpacity = mfo;
        editor.putFloat("maxFilterOpacity", maxFilterOpacity);
        editor.apply();
    }

    private static void sortBrightnessPointList() {
        brightnessPointList.sort((a, b) -> Float.compare(a[0], b[0]));
    }

    private static void loadDefaultBrightnessPointList() {
        clearBrightnessPointList();
        float t = 1.514159f;
        addBrightnessPoint(0, 0f / t);
        addBrightnessPoint(10, 0.1f / t);
        addBrightnessPoint(20, 0.2f / t);
        addBrightnessPoint(40, 0.3f / t);
        addBrightnessPoint(80, 0.4f / t);
        addBrightnessPoint(160, 0.5f / t);
        addBrightnessPoint(300, 0.6f / t);
        addBrightnessPoint(600, 0.71f / t);
        addBrightnessPoint(1000, 0.82f / t);
        addBrightnessPoint(1500, 0.95f / t);
        addBrightnessPoint(getHighLightThreshold(), 1f);
        syncBrightnessPointList();
    }

    private static void syncBrightnessPointList() {
        editor.putString("brightnessPointList", new Gson().toJson(brightnessPointList));
        editor.apply();
    }

    public static ArrayList<float[]> getBrightnessPointList() {
        sortBrightnessPointList();
        return brightnessPointList;
    }

    public static void clearBrightnessPointList() {
        brightnessPointList.clear();
    }

    public static void addBrightnessPoint(float light, float brightness) {
        float[] floatArray = new float[]{light, brightness};
        brightnessPointList.add(floatArray);
        sortBrightnessPointList();
        syncBrightnessPointList();
    }

    public static void delBrightnessPoint(float light, float brightness) {
        float[] floatArray = new float[]{light, brightness};

        for (int i = 0; i < brightnessPointList.size(); i++) {
            float[] arr = brightnessPointList.get(i);
            if (arr.length == 2 && arr[0] == floatArray[0] && arr[1] == floatArray[1]) {
                brightnessPointList.remove(i);
                break;
            }
        }
        syncBrightnessPointList();
    }

    public static boolean isFilterOpenMode() {
        return filterOpenMode;
    }

    public static void setFilterOpenMode(boolean filterOpenMode) {
        if (GlobalStatus.isReady()) {
            AppConfig.filterOpenMode = filterOpenMode;
            if (!filterOpenMode) {
                GlobalStatus.closeFilter();
                setIntelligentBrightnessOpenMode(false);
            }
        } else {
            AppConfig.filterOpenMode = false;
        }
        editor.putBoolean("filterOpenMode", filterOpenMode);
        editor.apply();
    }

    public static boolean isIntelligentBrightnessOpenMode() {
        return intelligentBrightnessOpenMode;
    }

    public static void setIntelligentBrightnessOpenMode(boolean intelligentBrightnessOpenMode) {
        if (GlobalStatus.isReady() && isFilterOpenMode()) {
            AppConfig.intelligentBrightnessOpenMode = intelligentBrightnessOpenMode;
        } else {
            AppConfig.intelligentBrightnessOpenMode = false;
        }
        editor.putBoolean("intelligentBrightnessOpenMode", intelligentBrightnessOpenMode);
        editor.apply();
    }

    public static boolean isHideInMultitaskingInterface() {
        return hideInMultitaskingInterface;
    }

    public static void setHideInMultitaskingInterface(boolean hideInMultitaskingInterface) {
        AppConfig.hideInMultitaskingInterface = hideInMultitaskingInterface;
        editor.putBoolean("hideInMultitaskingInterface", hideInMultitaskingInterface);
        editor.apply();
    }

    public static boolean isTempControlMode() {
        return tempControlMode;
    }

    public static void setTempControlMode(boolean tempControlMode) {
        AppConfig.tempControlMode = tempControlMode;
    }

    public static void init(Context c) {
        context = c;
        shared = c.getSharedPreferences("share", Context.MODE_PRIVATE);
        editor = shared.edit();

        setTempControlMode(false);

        highLightThreshold = shared.getFloat("highLightThreshold", default_highLightThreshold);
        maxFilterOpacity = shared.getFloat("maxFilterOpacity", default_maxFilterOpacity);
        minHardwareBrightness = shared.getFloat("minHardwareBrightness", default_minHardwareBrightness);

        if (shared.contains("brightnessPointList")) {
            brightnessPointList = new Gson().fromJson(
                    shared.getString("brightnessPointList", ""), new TypeToken<ArrayList<float[]>>() {
                    }.getType()
            );
        } else {
            brightnessPointList = new ArrayList<float[]>();
            loadDefaultBrightnessPointList();
        }

        filterOpenMode = shared.getBoolean("filterOpenMode", default_filterOpenMode);
        intelligentBrightnessOpenMode = shared.getBoolean("intelligentBrightnessOpenMode", default_intelligentBrightnessOpenMode);
        hideInMultitaskingInterface = shared.getBoolean("hideInMultitaskingInterface", default_hideInMultitaskingInterface);

    }

    public static void loadDefaultConfig() {
        setFilterOpenMode(default_filterOpenMode);
        setIntelligentBrightnessOpenMode(default_intelligentBrightnessOpenMode);
        setHideInMultitaskingInterface(default_hideInMultitaskingInterface);

        setHighLightThreshold(default_highLightThreshold);
        setMaxFilterOpacity(default_maxFilterOpacity);
        setMinHardwareBrightness(default_minHardwareBrightness);

        loadDefaultBrightnessPointList();
    }
}
