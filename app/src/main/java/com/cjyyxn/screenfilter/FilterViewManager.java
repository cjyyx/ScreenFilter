package com.cjyyxn.screenfilter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class FilterViewManager {

    private final Context context;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;
    private final FilterView filterView;

    private float alpha = 0;
    private float hardwareBrightness = 0;

    /**
     * 滤镜处于开启状态，为 true
     */
    public boolean isOpen;

    public FilterViewManager(Context c) {
        // 这里假设传入的 Context 有无障碍权限，后面的代码不对无障碍权限进行检验

        isOpen = false;
        context = c;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        filterView = new FilterView(context);

        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        // width 和 height 覆盖屏幕
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
            windowManager.addView(filterView, layoutParams);
            isOpen = true;
        }
    }

    public void close() {
        if (isOpen) {
            windowManager.removeView(filterView);
            isOpen = false;
        }
    }

    public void setAlpha(float alpha) {
        if (isOpen) {
            try {
                filterView.setAlpha(alpha);
                this.alpha =alpha;
            }catch (Exception e){
                Log.d("ccjy", "滤镜不透明度设置失败");
                e.printStackTrace();
            }
        }
    }

    public void setHardwareBrightness(float brightness) {
        if (isOpen) {
            try {
                layoutParams.screenBrightness = brightness;
                windowManager.updateViewLayout(filterView, layoutParams);
                hardwareBrightness =brightness;
            }catch (Exception e){
                Log.d("ccjy", "滤镜硬件亮度设置失败");
                e.printStackTrace();
            }
        }
    }

    public float getAlpha(){
        return alpha;
    }
    public float getHardwareBrightness(){
        return hardwareBrightness;
    }

    private class FilterView extends View {

        public FilterView(Context context) {
            super(context);
            setBackgroundColor(Color.BLACK);
            setAlpha(0f);
        }

        @Override
        public void setAlpha(float alpha) {

//            Log.d("ccjy", String.format(
//                    "设置滤镜不透明度为 %.1f %%",
//                    alpha * 100
//            ));

            super.setAlpha(alpha);
            invalidate();
        }
    }
}
