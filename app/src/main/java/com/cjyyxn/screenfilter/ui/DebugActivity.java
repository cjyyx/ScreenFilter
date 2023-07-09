package com.cjyyxn.screenfilter.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.cjyyxn.screenfilter.GlobalStatus;
import com.cjyyxn.screenfilter.R;

import java.util.Timer;
import java.util.TimerTask;

public class DebugActivity extends AppCompatActivity {

    TextView tv_debug_run_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        tv_debug_run_info = findViewById(R.id.tv_debug_run_info);

        addTimer();
    }

    private void addTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                new Handler(Looper.getMainLooper()).post(() -> {
                    // 在UI线程中更新UI组件

                    tv_debug_run_info.setText("");
                    tv_debug_run_info.append("应用运行信息:");
                    tv_debug_run_info.append(String.format(
                            "\n当前环境光照 %.1f lux",GlobalStatus.light
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前屏幕亮度(应用设置的亮度) %.1f %%",GlobalStatus.brightness*100
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前系统亮度(状态栏亮度条) %.1f %%",GlobalStatus.systemBrightness*100
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前硬件亮度(由滤镜设置) %.1f %%",GlobalStatus.getHardwareBrightness()*100
                    ));
                    tv_debug_run_info.append(String.format(
                            "\n当前滤镜不透明度 %.1f %%",GlobalStatus.getFilterOpacity()*100
                    ));

                });

            }
        };
        // 每隔 0.1秒钟执行一次任务
        timer.schedule(task, 0, 100);
    }

}