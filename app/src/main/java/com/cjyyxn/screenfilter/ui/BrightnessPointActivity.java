package com.cjyyxn.screenfilter.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.cjyyxn.screenfilter.AppConfig;
import com.cjyyxn.screenfilter.GlobalStatus;
import com.cjyyxn.screenfilter.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@SuppressLint("DefaultLocale")
public class BrightnessPointActivity extends AppCompatActivity {

    private XYPlot plot;

    private View dialogBrightnessView;

    private SeekBar sb_dialog_brightness_point_light;
    private float brightness_point_dialog_light;
    private TextView tv_dialog_brightness_point_light;
    private TextView tv_dialog_sensor_light;
    private SeekBar sb_dialog_brightness_point_brightness;
    private float brightness_point_dialog_brightness; // [0,1]
    private TextView tv_dialog_brightness_point_brightness;
    private Button bt_list_brightness_point_add;

    private LinearLayout ll_list_brightness_point_container;
    private boolean isInBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness_point);

        bt_list_brightness_point_add = findViewById(R.id.bt_list_brightness_point_add);
        bt_list_brightness_point_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddBrightnessPointDialog();
            }
        });

        showPlot();
        initBrightnessDialog();
        addTimer();
        addBrightnessPointListView();
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

    private void showPlot() {

        plot = (XYPlot) findViewById(R.id.plot);
        plot.setDomainBoundaries(-20, AppConfig.getHighLightThreshold(), BoundaryMode.FIXED);
        plot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

        plot.setDomainStep(StepMode.SUBDIVIDE, 11);
        plot.setRangeStep(StepMode.SUBDIVIDE, 11);

        // 能够移动和缩放
//        PanZoom.attach(plot);
        PanZoom.attach(plot, PanZoom.Pan.HORIZONTAL, PanZoom.Zoom.STRETCH_HORIZONTAL);
        plot.getOuterLimits().set(0, AppConfig.getHighLightThreshold(), -1, 100);

        plot.getLegend().setVisible(false);

        ArrayList<float[]> bpl = AppConfig.getBrightnessPointList();

        List<Float> list1 = bpl.stream().map(arr -> arr[0]).collect(Collectors.toList());
        List<Float> list2 = bpl.stream().map(arr -> arr[1] * 100).collect(Collectors.toList());

        XYSeries series = new SimpleXYSeries(list1, list2, "xxx");

        LineAndPointFormatter seriesFormat =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);


        plot.addSeries(series, seriesFormat);

    }

    private void updatePlot() {
        plot.clear();
        ArrayList<float[]> bpl = AppConfig.getBrightnessPointList();

        List<Float> list1 = bpl.stream().map(arr -> arr[0]).collect(Collectors.toList());
        List<Float> list2 = bpl.stream().map(arr -> arr[1] * 100).collect(Collectors.toList());

        XYSeries series = new SimpleXYSeries(list1, list2, "xxx");

        LineAndPointFormatter seriesFormat =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);


        plot.addSeries(series, seriesFormat);
        plot.redraw();
    }


    /**
     * 初始化光照-亮度对应点对话框
     */
    private void initBrightnessDialog() {
        dialogBrightnessView = LayoutInflater.from(this).inflate(R.layout.dialog_brightness_point, null);
        sb_dialog_brightness_point_light = dialogBrightnessView.findViewById(R.id.sb_dialog_brightness_point_light);
        tv_dialog_brightness_point_light = dialogBrightnessView.findViewById(R.id.tv_dialog_brightness_point_light);
        tv_dialog_sensor_light = dialogBrightnessView.findViewById(R.id.tv_dialog_sensor_light);
        sb_dialog_brightness_point_brightness = dialogBrightnessView.findViewById(R.id.sb_dialog_brightness_point_brightness);
        tv_dialog_brightness_point_brightness = dialogBrightnessView.findViewById(R.id.tv_dialog_brightness_point_brightness);

        // 设置光照的拖动条
        sb_dialog_brightness_point_light.setMax(light2progress(AppConfig.getHighLightThreshold()));
        sb_dialog_brightness_point_light.setMin(0);
        sb_dialog_brightness_point_light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 在进度条改变时执行的代码
                brightness_point_dialog_light = progress2light(progress);
                tv_dialog_brightness_point_light.setText(String.format("%.1f lux", brightness_point_dialog_light));
                GlobalStatus.setBrightness(GlobalStatus.calculateBrightnessByLight(brightness_point_dialog_light));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 在开始拖动进度条时执行的代码
                AppConfig.setTempControlMode(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 在停止拖动进度条时执行的代码
                AppConfig.setTempControlMode(false);
            }
        });

        // 设置亮度的拖动条
        sb_dialog_brightness_point_brightness.setMax(1000);
        sb_dialog_brightness_point_brightness.setMin(0);
        sb_dialog_brightness_point_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 在进度条改变时执行的代码
                brightness_point_dialog_brightness = ((float) progress) / 1000f;
                tv_dialog_brightness_point_brightness.setText(String.format("%.1f %%", brightness_point_dialog_brightness * 100));
                GlobalStatus.setBrightness(brightness_point_dialog_brightness);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 在开始拖动进度条时执行的代码
                AppConfig.setTempControlMode(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 在停止拖动进度条时执行的代码
                AppConfig.setTempControlMode(false);
            }
        });

    }

    private void openAddBrightnessPointDialog() {
        // 重复打开对话框时，避免报错
        ViewGroup parent = (ViewGroup) dialogBrightnessView.getParent();
        if (parent != null) {
            parent.removeView(dialogBrightnessView);
        }

        float gl = GlobalStatus.light;
        tv_dialog_brightness_point_light.setText(String.format("%.1f lux", gl));
        sb_dialog_brightness_point_light.setProgress(light2progress(gl));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBrightnessView);

        builder.setTitle("添加光照-亮度对应点");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppConfig.addBrightnessPoint(brightness_point_dialog_light, brightness_point_dialog_brightness);
                updateBrightnessPointListView();
                updatePlot();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openModifyBrightnessPointDialog(float light, float brightness) {
        // 重复打开对话框时，避免报错
        ViewGroup parent = (ViewGroup) dialogBrightnessView.getParent();
        if (parent != null) {
            parent.removeView(dialogBrightnessView);
        }

        tv_dialog_brightness_point_light.setText(String.format("%.1f lux", light));
        sb_dialog_brightness_point_light.setProgress(light2progress(light));
        tv_dialog_brightness_point_brightness.setText(
                String.format("%.1f %%", brightness * 100));
        sb_dialog_brightness_point_brightness.setProgress(
                (int) (brightness * 1000)
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBrightnessView);

        builder.setTitle("修改光照-亮度对应点");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppConfig.delBrightnessPoint(light, brightness);
                AppConfig.addBrightnessPoint(brightness_point_dialog_light, brightness_point_dialog_brightness);
                updateBrightnessPointListView();
                updatePlot();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!isInBackground) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 在UI线程中更新UI组件
//                        Log.d("ccjy", "更新 BrightnessPointActivityUI");
                        if (tv_dialog_sensor_light != null) {
                            tv_dialog_sensor_light.setText(String.format("%.1f lux", GlobalStatus.light));
                        }
                    });
                }
            }
        };

        timer.schedule(task, 0, 200);
    }

    /**
     * 拖动条区间[0,10000]，映射至 [0,highLightThreshold]
     * L=(2^(p/10000)-1)^3*H
     */
    private float progress2light(int progress) {
        float p = (float) progress;
        float H = AppConfig.getHighLightThreshold();
        float L = (float) Math.pow((Math.pow(2, p / 10000) - 1), 3) * H;

        return L;
    }

    /**
     * 光照区间 [0,highLightThreshold]，映射至拖动条区间[0,10000]
     * p=log2((L/H)^(1/3)+1)*10000
     */
    private int light2progress(float light) {
        float L = light;
        float H = AppConfig.getHighLightThreshold();
        double p = (Math.log(Math.pow(L / H, 1.f / 3.f) + 1) / Math.log(2)) * 10000;
        return (int) p;
    }

    private void addBrightnessPointListView() {
        ll_list_brightness_point_container = findViewById(R.id.ll_list_brightness_point_container);

        ArrayList<float[]> bpl = AppConfig.getBrightnessPointList();

        for (int i = 0; i < bpl.size(); i++) {
            float[] arr = bpl.get(i);
            addBrightnessPointView(arr[0], arr[1]);
        }

    }

    private void updateBrightnessPointListView() {

        ll_list_brightness_point_container.removeAllViews();

        ArrayList<float[]> bpl = AppConfig.getBrightnessPointList();

        for (int i = 0; i < bpl.size(); i++) {
            float[] arr = bpl.get(i);
            addBrightnessPointView(arr[0], arr[1]);
        }
    }

    private void addBrightnessPointView(float light, float brightness) {
        if (Float.compare(light, 0) == 0 && Float.compare(brightness, 0) == 0) {
            return;
        } else if (Float.compare(light, AppConfig.getHighLightThreshold()) == 0 && Float.compare(brightness, 1f) == 0) {
            return;
        }

        LinearLayout cloneLayout = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.list_brightness_point, null);

        TextView tvl = cloneLayout.findViewById(R.id.tv_list_brightness_point_light);
        tvl.setText(String.format("光照: %.1f lux", light));

        TextView tvb = cloneLayout.findViewById(R.id.tv_list_brightness_point_brightness);
        tvb.setText(String.format("亮度: %.1f %%", brightness * 100));

        // 删除按钮
        Button btd = cloneLayout.findViewById(R.id.bt_list_brightness_point_delete);
        btd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppConfig.delBrightnessPoint(light, brightness);
                updateBrightnessPointListView();
                updatePlot();
            }
        });

        // 修改按钮
        Button btm = cloneLayout.findViewById(R.id.bt_list_brightness_point_modify);
        btm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openModifyBrightnessPointDialog(light, brightness);
            }
        });
        ll_list_brightness_point_container.addView(cloneLayout);
    }
}