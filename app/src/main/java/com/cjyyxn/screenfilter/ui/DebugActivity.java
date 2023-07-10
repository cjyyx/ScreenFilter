package com.cjyyxn.screenfilter.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cjyyxn.screenfilter.AppConfig;
import com.cjyyxn.screenfilter.GlobalStatus;
import com.cjyyxn.screenfilter.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressLint("DefaultLocale")
public class DebugActivity extends AppCompatActivity {

    private TextView tv_debug_run_info;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch sw_debug_temp_control;
    private LinearLayout ll_list_debug_seekbar_control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        tv_debug_run_info = findViewById(R.id.tv_debug_run_info);
        sw_debug_temp_control = findViewById(R.id.sw_debug_temp_control);
        ll_list_debug_seekbar_control = findViewById(R.id.ll_list_debug_seekbar_control);

        setUI();
        addDebugViewTimer();
    }


    private void setUI() {
        sw_debug_temp_control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalStatus.setTempControlMode(isChecked);
            }
        });

        addSeekBarControl(
                "屏幕亮度", 0, 100,
                (P) -> String.format("%d %%", P),
                (P) -> GlobalStatus.setBrightness(((float) P) * (1f / 100f))
        );
        addSeekBarControl(
                "滤镜不透明度", 0, 100,
                (P) -> String.format("%d %%", P),
                (P) -> GlobalStatus.setFilterOpacity(((float) P) * (1f / 100f))
        );
        addSeekBarControl(
                "硬件亮度", 0, 100,
                (P) -> String.format("%d %%", P),
                (P) -> GlobalStatus.setHardwareBrightness(((float) P) * (1f / 100f))
        );
        addSeekBarControl(
                "用亮度设置状态栏亮度条", 0, 100,
                (P) -> String.format("%d %%", P),
                (P) -> GlobalStatus.setSystemBrightnessProgressByBrightness(((float) P) * (1f / 100f))
        );
    }


    private void addSeekBarControl(
            String name,
            int minP,
            int maxP,
            Function<Integer, String> tv_set,
            Consumer<Integer> onPChanged
    ) {
        LinearLayout cloneLayout = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.seekbar_control, null);

        TextView tv_control_name = cloneLayout.findViewById(R.id.tv_control_name);
        SeekBar sb_control = cloneLayout.findViewById(R.id.sb_control);
        TextView tv_control_set = cloneLayout.findViewById(R.id.tv_control_set);

        tv_control_name.setText(name);

        sb_control.setMin(minP);
        sb_control.setMax(maxP);
        sb_control.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_control_set.setText(tv_set.apply(progress));
                onPChanged.accept(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ll_list_debug_seekbar_control.addView(cloneLayout);
    }

    private void addDebugViewTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                new Handler(Looper.getMainLooper()).post(() -> {
                    // 在UI线程中更新UI组件

                    tv_debug_run_info.setText("");
                    tv_debug_run_info.append("应用运行信息:");
                    tv_debug_run_info.append(String.format(
                            "\n当前环境光照 %.1f lux", GlobalStatus.light
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前屏幕亮度(应用设置的亮度) %.1f %%", GlobalStatus.brightness * 100
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前系统亮度(状态栏亮度条) %.1f %%", GlobalStatus.getSystemBrightness() * 100
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前硬件亮度(由滤镜设置) %.1f %%", GlobalStatus.getHardwareBrightness() * 100
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前滤镜不透明度 %.1f %%", GlobalStatus.getFilterOpacity() * 100
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前屏幕实际亮度(估计值) %.1f lux", GlobalStatus.brightness * AppConfig.MAX_SCREEN_LIGHT
                    ));

                });

            }
        };
        // 每隔 0.1秒钟执行一次任务
        timer.schedule(task, 0, 100);
    }

}