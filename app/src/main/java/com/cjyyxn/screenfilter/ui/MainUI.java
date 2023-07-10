package com.cjyyxn.screenfilter.ui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cjyyxn.screenfilter.GlobalStatus;
import com.cjyyxn.screenfilter.MainActivity;
import com.cjyyxn.screenfilter.R;

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
    /**
     * 最低硬件亮度 0,100 -> 0,100
     */
    private final SeekBar sb_main_min_hardware_brightness;
    private final TextView tv_main_min_hardware_brightness;
    /**
     * 最高滤镜不透明度 0,100 -> 80,100
     */
    private final SeekBar sb_main_max_filter_opacity;
    private final TextView tv_main_max_filter_opacity;
    /**
     * 高光照阈值 0,50 -> 1000,6000
     */
    private final SeekBar sb_main_high_light_threshold;
    private final TextView tv_main_high_light_threshold;
    private final Button bt_main_load_default_config;
    private final Button bt_main_open_preparatory_view;

    /**
     * 主界面用户设置屏幕亮度 0,100 -> 0,1
     * 应与系统状态栏亮度同步
     */
    private final SeekBar sb_main_brightness_set_by_user;
    private final TextView tv_main_brightness_set_by_user;

    private final Button bt_main_open_debug_view;
    private final Button bt_main_hide_in_multitasking_interface;

    public MainUI(MainActivity act) {
        mainActivity = act;

        tv_main_light = mainActivity.findViewById(R.id.tv_main_light);
        tv_main_brightness = mainActivity.findViewById(R.id.tv_main_brightness);
        bt_main_open_brightness_point_view = mainActivity.findViewById(R.id.bt_main_open_brightness_point_view);
        sw_main_filter = mainActivity.findViewById(R.id.sw_main_filter);
        sb_main_min_hardware_brightness = mainActivity.findViewById(R.id.sb_main_min_hardware_brightness);
        tv_main_min_hardware_brightness = mainActivity.findViewById(R.id.tv_main_min_hardware_brightness);
        sb_main_max_filter_opacity = mainActivity.findViewById(R.id.sb_main_max_filter_opacity);
        tv_main_max_filter_opacity = mainActivity.findViewById(R.id.tv_main_max_filter_opacity);
        sb_main_high_light_threshold = mainActivity.findViewById(R.id.sb_main_high_light_threshold);
        tv_main_high_light_threshold = mainActivity.findViewById(R.id.tv_main_high_light_threshold);
        bt_main_load_default_config = mainActivity.findViewById(R.id.bt_main_load_default_config);
        bt_main_open_preparatory_view = mainActivity.findViewById(R.id.bt_main_open_preparatory_view);
        sw_main_intelligent_brightness = mainActivity.findViewById(R.id.sw_main_intelligent_brightness);
        sb_main_brightness_set_by_user = mainActivity.findViewById(R.id.sb_main_brightness_set_by_user);
        tv_main_brightness_set_by_user = mainActivity.findViewById(R.id.tv_main_brightness_set_by_user);
        bt_main_open_debug_view = mainActivity.findViewById(R.id.bt_main_open_debug_view);
        bt_main_hide_in_multitasking_interface = mainActivity.findViewById(R.id.bt_main_hide_in_multitasking_interface);

        setUI();

        addTimer();
    }

    private void setUI() {
        bt_main_open_brightness_point_view.setOnClickListener(view -> mainActivity.startActivity(new Intent(mainActivity, BrightnessPointActivity.class)));

        bt_main_load_default_config.setOnClickListener(view -> {
            GlobalStatus.loadDefaultConfig();
            Toast.makeText(mainActivity, "默认配置加载成功", Toast.LENGTH_SHORT).show();
        });

        bt_main_open_preparatory_view.setOnClickListener(view -> mainActivity.startActivity(new Intent(mainActivity, PreparatoryActivity.class)));

        bt_main_open_debug_view.setOnClickListener(view -> mainActivity.startActivity(new Intent(mainActivity, DebugActivity.class)));

        bt_main_hide_in_multitasking_interface.setOnClickListener(view -> {
            try {
                ActivityManager service = (ActivityManager) mainActivity.getSystemService(Context.ACTIVITY_SERVICE);
                for (ActivityManager.AppTask task : service.getAppTasks()) {
                    if (task.getTaskInfo().taskId == mainActivity.getTaskId()) {
                        task.setExcludeFromRecents(true);
                        Toast.makeText(mainActivity, "多任务界面隐藏成功", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(mainActivity, "多任务界面隐藏失败", Toast.LENGTH_SHORT).show();
            }
        });

        sw_main_filter.setOnCheckedChangeListener((buttonView, isChecked) -> GlobalStatus.setFilterOpenMode(isChecked));
        sw_main_intelligent_brightness.setOnCheckedChangeListener((buttonView, isChecked) -> GlobalStatus.setIntelligentBrightnessOpenMode(isChecked));

        sb_main_min_hardware_brightness.setMin(0);
        sb_main_min_hardware_brightness.setMax(100);
        sb_main_min_hardware_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float brightness = ((float) progress) / 100f;
                GlobalStatus.setMinHardwareBrightness(brightness);

                tv_main_min_hardware_brightness.setText(String.format(
                        "%.0f %%", GlobalStatus.getMinHardwareBrightness() * 100
                ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sb_main_max_filter_opacity.setMin(0);
        sb_main_max_filter_opacity.setMax(100);
        sb_main_max_filter_opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float mfo = 0.8f + ((float) progress) * (0.2f / 100f);
                GlobalStatus.setMaxFilterOpacity(mfo);

                tv_main_max_filter_opacity.setText(String.format(
                        "%.1f %%", GlobalStatus.getMaxFilterOpacity() * 100
                ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sb_main_high_light_threshold.setMin(0);
        sb_main_high_light_threshold.setMax(50);
        sb_main_high_light_threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float hlt = 1000f + ((float) progress) * (5000f / 50f);
                GlobalStatus.setHighLightThreshold(hlt);

                tv_main_high_light_threshold.setText(String.format(
                        "%.0f lux", GlobalStatus.getHighLightThreshold()
                ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sb_main_brightness_set_by_user.setMin(0);
        sb_main_brightness_set_by_user.setMax(100);
        sb_main_brightness_set_by_user.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float b = ((float) progress) / 100f;
                GlobalStatus.setSystemBrightnessProgressByBrightness(b);

                tv_main_brightness_set_by_user.setText(String.format(
                        "%d %%", progress
                ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void addTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                new Handler(Looper.getMainLooper()).post(() -> {
                    // 在UI线程中更新UI组件

                    tv_main_light.setText(String.format("当前环境光照: %.1f lux", GlobalStatus.light));
                    tv_main_brightness.setText(String.format("当前屏幕亮度: %.1f %%", GlobalStatus.brightness * 100));

                    tv_main_min_hardware_brightness.setText(String.format(
                            "%.0f %%", GlobalStatus.getMinHardwareBrightness() * 100
                    ));
                    sb_main_min_hardware_brightness.setProgress((int) (GlobalStatus.getMinHardwareBrightness() * 100 + 0.5));

                    tv_main_max_filter_opacity.setText(String.format(
                            "%.1f %%", GlobalStatus.getMaxFilterOpacity() * 100
                    ));
                    sb_main_max_filter_opacity.setProgress((int) (
                            (GlobalStatus.getMaxFilterOpacity() - 0.8f) * (100f / 0.2f) + 0.5
                    ));

                    tv_main_high_light_threshold.setText(String.format(
                            "%.0f lux", GlobalStatus.getHighLightThreshold()
                    ));
                    sb_main_high_light_threshold.setProgress((int) (
                            (GlobalStatus.getHighLightThreshold() - 1000f) * (50f / 5000f) + 0.5
                    ));

                    tv_main_brightness_set_by_user.setText(String.format(
                            "%.0f %%", GlobalStatus.systemBrightness * 100
                    ));
                    sb_main_brightness_set_by_user.setProgress((int) (
                            GlobalStatus.systemBrightness * 100
                    ));

                    sw_main_filter.setChecked(GlobalStatus.isFilterOpenMode());
                    sw_main_intelligent_brightness.setChecked(GlobalStatus.isIntelligentBrightnessOpenMode());

                });

            }
        };
        // 每隔 0.1秒钟执行一次任务
        timer.schedule(task, 0, 100);
    }

}
