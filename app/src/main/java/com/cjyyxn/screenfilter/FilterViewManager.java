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
    private float alpha = 0f;
    private float hardwareBrightness = 0f;

    public FilterViewManager(Context c) {
        // 这里假设传入的 Context 有无障碍权限，后面的代码不对无障碍权限进行检验

        isOpen = false;
        context = c;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        filterView = new FilterView(context);

        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        // width 和 height 尽可能大，从而覆盖屏幕
        layoutParams.width = 4000;
        layoutParams.height = 4000;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
    }

    public void open() {
        if (isOpen) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            // 在UI线程中更新UI组件
            if (!isOpen) {
                windowManager.addView(filterView, layoutParams);
                setAlpha(alpha);
                setHardwareBrightness(hardwareBrightness);
                isOpen = true;
            }
        });
    }

    public void close() {
        if (!isOpen) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            // 在UI线程中更新UI组件
            if (isOpen) {
                windowManager.removeView(filterView);
                isOpen = false;
            }
        });
    }

    public float getAlpha() {
        if (isOpen) {
            return alpha;
        } else {
            return -1f;
        }
    }

    public void setAlpha(float alpha) {
        if (!isOpen) {
            return;
        }

        if (Float.compare(this.alpha, alpha) == 0) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            // 在UI线程中更新UI组件
            if (isOpen) {
                float a = Math.min(1f, Math.max(0f, alpha));
                filterView.setAlpha(a);
                this.alpha = a;
            }
        });
    }

    public float getHardwareBrightness() {
        if (isOpen) {
            return hardwareBrightness;
        } else {
            return -1f;
        }
    }

    public void setHardwareBrightness(float brightness) {
        if (!isOpen) {
            return;
        }

        if (Float.compare(this.hardwareBrightness, brightness) == 0) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            // 在UI线程中更新UI组件
            if (isOpen) {
                float b = Math.min(1f, Math.max(0f, brightness));
                // layoutParams.screenBrightness 会覆盖系统亮度设置
                layoutParams.screenBrightness = b;
                windowManager.updateViewLayout(filterView, layoutParams);
                hardwareBrightness = b;
            }
        });
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
