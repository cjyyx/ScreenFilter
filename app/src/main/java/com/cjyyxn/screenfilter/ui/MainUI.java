package com.cjyyxn.screenfilter.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cjyyxn.screenfilter.AppConfig;
import com.cjyyxn.screenfilter.GlobalStatus;
import com.cjyyxn.screenfilter.MainActivity;
import com.cjyyxn.screenfilter.R;
import com.cjyyxn.screenfilter.utils.CombinationControl;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint({"DefaultLocale", "UseSwitchCompatOrMaterialCode"})
public class MainUI {

    private final MainActivity mainActivity;

    private final TextView tv_main_light;
    private final TextView tv_main_brightness;
    private final Button bt_main_open_brightness_point_view;
    private final Switch sw_main_filter;
    private final Switch sw_main_intelligent_brightness;


    private final Button bt_main_load_default_config;
    private final Button bt_main_open_preparatory_view;


    private final Button bt_main_open_debug_view;
    private final Switch sw_main_hide_in_multitasking_interface;

    private final LinearLayout ll0_list_main;
    private final CombinationControl combinationControl;

    public MainUI(MainActivity act) {
        mainActivity = act;

        tv_main_light = mainActivity.findViewById(R.id.tv_main_light);
        tv_main_brightness = mainActivity.findViewById(R.id.tv_main_brightness);
        bt_main_open_brightness_point_view = mainActivity.findViewById(R.id.bt_main_open_brightness_point_view);
        sw_main_filter = mainActivity.findViewById(R.id.sw_main_filter);
        bt_main_load_default_config = mainActivity.findViewById(R.id.bt_main_load_default_config);
        bt_main_open_preparatory_view = mainActivity.findViewById(R.id.bt_main_open_preparatory_view);
        sw_main_intelligent_brightness = mainActivity.findViewById(R.id.sw_main_intelligent_brightness);
        bt_main_open_debug_view = mainActivity.findViewById(R.id.bt_main_open_debug_view);
        sw_main_hide_in_multitasking_interface = mainActivity.findViewById(R.id.sw_main_hide_in_multitasking_interface);
        ll0_list_main = mainActivity.findViewById(R.id.ll0_list_main);
        combinationControl = new CombinationControl(ll0_list_main, mainActivity);

        /**
         * 主界面用户设置屏幕亮度 1,128 -> 0,1
         * 应与系统状态栏亮度同步
         */
        combinationControl.addSeekBarControl(
                "屏幕亮度设置", 1, AppConfig.SETTING_SCREEN_BRIGHTNESS,
                (P) -> String.format("%.0f %%", GlobalStatus.getSystemBrightness() * 100),
                (sb, P, fromUser) -> {
                    if (fromUser) {
                        GlobalStatus.setSystemBrightnessProgress(P);
                        GlobalStatus.setBrightness(GlobalStatus.getSystemBrightness());
                    }
                },
                (sb) -> AppConfig.setTempControlMode(true),
                (sb) -> AppConfig.setTempControlMode(false),
                (sb) -> sb.setProgress(GlobalStatus.getSystemBrightnessProgress())
        );
        /**
         * 最低硬件亮度 0,100 -> 0,100
         */
        combinationControl.addSeekBarControl(
                "最低硬件亮度", 0, 100,
                (P) -> String.format("%.0f %%", AppConfig.getMinHardwareBrightness() * 100),
                (sb, P, fromUser) -> AppConfig.setMinHardwareBrightness(((float) P) / 100f),
                (sb) -> CombinationControl.pass(),
                (sb) -> CombinationControl.pass(),
                (sb) -> sb.setProgress((int) (AppConfig.getMinHardwareBrightness() * 100 + 0.5f))
        );
        /**
         * 最高滤镜不透明度 60,100 -> 60,100
         */
        combinationControl.addSeekBarControl(
                "最高滤镜\n不透明度", 60, 100,
                (P) -> String.format("%.0f %%", AppConfig.getMaxFilterOpacity() * 100),
                (sb, P, fromUser) -> AppConfig.setMaxFilterOpacity(((float) P) / 100f),
                (sb) -> CombinationControl.pass(),
                (sb) -> CombinationControl.pass(),
                (sb) -> sb.setProgress((int) (AppConfig.getMaxFilterOpacity() * 100f + 0.5f))
        );
        /**
         * 高光照阈值 0,50 -> 1000,6000
         */
        combinationControl.addSeekBarControl(
                "阳光模式阈值", 0, 50,
                (P) -> String.format("%.0f lux", AppConfig.getHighLightThreshold()),
                (sb, P, fromUser) -> AppConfig.setHighLightThreshold(1000f + ((float) P) * (5000f / 50f)),
                (sb) -> CombinationControl.pass(),
                (sb) -> CombinationControl.pass(),
                (sb) -> sb.setProgress((int) ((AppConfig.getHighLightThreshold() - 1000f) * (50f / 5000f) + 0.5))
        );
        combinationControl.addSeekBarControl(
                "亮度调节容差", 0, 80,
                (P) -> String.format("%.2f", AppConfig.getBrightnessAdjustmentTolerance()),
                (sb, P, fromUser) -> AppConfig.setBrightnessAdjustmentTolerance(((float) P) / 100f),
                (sb) -> CombinationControl.pass(),
                (sb) -> CombinationControl.pass(),
                (sb) -> sb.setProgress((int) (AppConfig.getBrightnessAdjustmentTolerance() * 100 + 0.5f))
        );

        setUI();
        addTimer();
    }

    private void setUI() {
        bt_main_open_brightness_point_view.setOnClickListener(view -> mainActivity.startActivity(new Intent(mainActivity, BrightnessPointActivity.class)));

        bt_main_load_default_config.setOnClickListener(view -> {
            AppConfig.loadDefaultConfig();
            Toast.makeText(mainActivity, "默认配置加载成功", Toast.LENGTH_SHORT).show();
        });

        bt_main_open_preparatory_view.setOnClickListener(view -> mainActivity.startActivity(new Intent(mainActivity, PreparatoryActivity.class)));

        bt_main_open_debug_view.setOnClickListener(view -> mainActivity.startActivity(new Intent(mainActivity, DebugActivity.class)));


        sw_main_filter.setOnCheckedChangeListener((buttonView, isChecked) -> AppConfig.setFilterOpenMode(isChecked));
        sw_main_intelligent_brightness.setOnCheckedChangeListener((buttonView, isChecked) -> AppConfig.setIntelligentBrightnessOpenMode(isChecked));
        sw_main_hide_in_multitasking_interface.setOnCheckedChangeListener((buttonView, isChecked) -> AppConfig.setHideInMultitaskingInterface(isChecked));


    }

    private void addTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                if (!mainActivity.isInBackground) {
                    combinationControl.update();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 在UI线程中更新UI组件

//                        Log.d("ccjy", "更新 mainUI");
                        tv_main_light.setText(String.format("当前环境光照: %.1f lux", GlobalStatus.light));
                        tv_main_brightness.setText(String.format("当前屏幕亮度: %.1f %%", GlobalStatus.getBrightness() * 100));

                        sw_main_filter.setChecked(AppConfig.isFilterOpenMode());
                        sw_main_intelligent_brightness.setChecked(AppConfig.isIntelligentBrightnessOpenMode());
                        sw_main_hide_in_multitasking_interface.setChecked(AppConfig.isHideInMultitaskingInterface());
                    });

                }
            }
        };
        timer.schedule(task, 0, 200);
    }

}
