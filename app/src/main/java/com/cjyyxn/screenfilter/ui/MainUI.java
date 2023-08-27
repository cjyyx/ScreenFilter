package com.cjyyxn.screenfilter.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
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

    private final LinearLayout ll0_list_main;
    private final CombinationControl combinationControl;

    public MainUI(MainActivity act) {
        mainActivity = act;

        tv_main_light = mainActivity.findViewById(R.id.tv_main_light);
        tv_main_brightness = mainActivity.findViewById(R.id.tv_main_brightness);

        ll0_list_main = mainActivity.findViewById(R.id.ll0_list_main);
        combinationControl = new CombinationControl(ll0_list_main, mainActivity);

        setUI();
        addTimer();
    }

    private void setUI() {

        combinationControl.addSwitchControl(
                "屏幕滤镜开关",
                (buttonView, isChecked) -> AppConfig.setFilterOpenMode(isChecked),
                (sw) -> sw.setChecked(AppConfig.isFilterOpenMode())
        );
        combinationControl.addSwitchControl(
                "智能亮度开关",
                (buttonView, isChecked) -> AppConfig.setIntelligentBrightnessOpenMode(isChecked),
                (sw) -> sw.setChecked(AppConfig.isIntelligentBrightnessOpenMode())
        );
        combinationControl.addSwitchControl(
                "在多任务界面隐藏",
                (buttonView, isChecked) -> AppConfig.setHideInMultitaskingInterface(isChecked),
                (sw) -> sw.setChecked(AppConfig.isHideInMultitaskingInterface())
        );

        combinationControl.addLine();

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
                (sb, P, fromUser) -> {
                    if (fromUser) {
                        AppConfig.setMinHardwareBrightness(((float) P) / 100f);
                    }
                },
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
                (sb, P, fromUser) -> {
                    if (fromUser) {
                        AppConfig.setMaxFilterOpacity(((float) P) / 100f);
                    }
                },
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
                (sb, P, fromUser) -> {
                    if (fromUser) {
                        AppConfig.setHighLightThreshold(1000f + ((float) P) * (5000f / 50f));
                    }
                },
                (sb) -> CombinationControl.pass(),
                (sb) -> CombinationControl.pass(),
                (sb) -> sb.setProgress((int) ((AppConfig.getHighLightThreshold() - 1000f) * (50f / 5000f) + 0.5))
        );
        combinationControl.addSeekBarControl(
                "亮度调高容差", 0, 50,
                (P) -> String.format("%.2f", AppConfig.getBrightnessAdjustmentIncreaseTolerance()),
                (sb, P, fromUser) -> {
                    if (fromUser) {
                        AppConfig.setBrightnessAdjustmentIncreaseTolerance(((float) P) / 100f);
                    }
                },
                (sb) -> CombinationControl.pass(),
                (sb) -> CombinationControl.pass(),
                (sb) -> sb.setProgress((int) (AppConfig.getBrightnessAdjustmentIncreaseTolerance() * 100f + 0.5f))
        );
        combinationControl.addSeekBarControl(
                "亮度调低容差", 0, 50,
                (P) -> String.format("%.2f", AppConfig.getBrightnessAdjustmentDecreaseTolerance()),
                (sb, P, fromUser) -> {
                    if (fromUser) {
                        AppConfig.setBrightnessAdjustmentDecreaseTolerance(((float) P) / 100f);
                    }
                },
                (sb) -> CombinationControl.pass(),
                (sb) -> CombinationControl.pass(),
                (sb) -> sb.setProgress((int) (AppConfig.getBrightnessAdjustmentDecreaseTolerance() * 100f + 0.5f))
        );

        combinationControl.addLine();

        combinationControl.addJumpLabel(
                "打开准备界面",
                () -> mainActivity.startActivity(new Intent(mainActivity, PreparatoryActivity.class))
        );
        combinationControl.addJumpLabel(
                "打开亮度-光照曲线设置界面",
                () -> mainActivity.startActivity(new Intent(mainActivity, BrightnessPointActivity.class))
        );
        combinationControl.addJumpLabel(
                "加载默认配置",
                () -> {
                    AppConfig.loadDefaultConfig();
                    Toast.makeText(mainActivity, "默认配置加载成功", Toast.LENGTH_SHORT).show();
                }
        );
        combinationControl.addJumpLabel(
                "打开调试界面",
                () -> mainActivity.startActivity(new Intent(mainActivity, DebugActivity.class))
        );

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

                    });

                }
            }
        };
        timer.schedule(task, 0, 200);
    }

}
