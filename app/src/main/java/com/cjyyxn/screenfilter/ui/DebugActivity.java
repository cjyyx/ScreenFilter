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
    private boolean isInBackground = false;

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

    @Override
    protected void onPause() {
        super.onPause();
        isInBackground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInBackground = false;
    }

    private void setUI() {
        sw_debug_temp_control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppConfig.setTempControlMode(isChecked);
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

                if (!isInBackground) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 在UI线程中更新UI组件

//                        Log.d("ccjy", "更新 DebugActivityUI");
                        tv_debug_run_info.setText("");
                        tv_debug_run_info.append("应用运行信息:");
                        tv_debug_run_info.append(String.format(
                                "\n当前环境光照 %.2f lux, 屏幕亮度 %.2f %%",
                                GlobalStatus.light, GlobalStatus.getBrightness() * 100
                        ));
                        tv_debug_run_info.append(String.format(
                                "\n当前系统亮度条 int 值 %d, 系统亮度 %.2f %%",
                                GlobalStatus.getSystemBrightnessProgress(), GlobalStatus.getSystemBrightness() * 100
                        ));
                        tv_debug_run_info.append(String.format(
                                "\n当前滤镜亮度 %.2f %%, 滤镜不透明度 %.2f %%",
                                GlobalStatus.getHardwareBrightness() * 100, GlobalStatus.getFilterOpacity() * 100
                        ));
                        tv_debug_run_info.append(String.format(
                                "\n当前屏幕实际亮度(估计值) %.2f nit", GlobalStatus.getBrightness() * AppConfig.MAX_SCREEN_LIGHT
                        ));
                    });
                }
            }
        };
        timer.schedule(task, 0, 200);
    }

}