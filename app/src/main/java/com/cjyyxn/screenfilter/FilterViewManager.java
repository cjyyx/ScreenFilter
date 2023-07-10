package com.cjyyxn.screenfilter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

public class FilterViewManager {

    private final Context context;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;
    private final FilterView filterView;
    /**
     * 滤镜处于开启状态，为 true
     */
    public boolean isOpen;
    private float alpha = 0;
    private float hardwareBrightness = 0;

    public FilterViewManager(Context c) {
        // 这里假设传入的 Context 有无障碍权限，后面的代码不对无障碍权限进行检验

        isOpen = false;
        context = c;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        filterView = new FilterView(context);

        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        // width 和 height 尽可能大，从而覆盖屏幕
        layoutParams.width = 1800;
        layoutParams.height = 3200;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
    }

    public void open() {
        if (!isOpen) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // 在UI线程中更新UI组件
                windowManager.addView(filterView, layoutParams);
                isOpen = true;
            });
        }
    }

    public void close() {
        if (isOpen) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // 在UI线程中更新UI组件
                windowManager.removeView(filterView);
                isOpen = false;
            });
        }
    }

    public float getAlpha() {
        if (isOpen) {
            return alpha;
        } else {
            return 0f;
        }
    }

    public void setAlpha(float alpha) {
        if (isOpen) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // 在UI线程中更新UI组件
                filterView.setAlpha(alpha);
                this.alpha = alpha;
            });
        }
    }

    public float getHardwareBrightness() {
        if (isOpen) {
            return hardwareBrightness;
        } else {
            return 0f;
        }
    }

    public void setHardwareBrightness(float brightness) {
        if (isOpen) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // 在UI线程中更新UI组件
                // layoutParams.screenBrightness 会覆盖系统亮度设置
                layoutParams.screenBrightness = brightness;
                windowManager.updateViewLayout(filterView, layoutParams);
                hardwareBrightness = brightness;
            });
        }
    }

    private static class FilterView extends View {

        public FilterView(Context context) {
            super(context);
            setBackgroundColor(Color.BLACK);
            setAlpha(0f);
        }

        @Override
        public void setAlpha(float alpha) {
            super.setAlpha(alpha);
            invalidate();
        }
    }
}
