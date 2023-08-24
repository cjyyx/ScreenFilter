package com.cjyyxn.screenfilter.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.cjyyxn.screenfilter.R;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressLint("InflateParams")
public class CombinationControl {

    private final LinearLayout linearLayout;
    private final Context context;

    private ArrayList<SeekBarControl> sbcList = new ArrayList<SeekBarControl>();
    private ArrayList<SwitchControl> swcList = new ArrayList<SwitchControl>();

    public CombinationControl(LinearLayout ll, Context c) {
        linearLayout = ll;
        context = c;
    }

    public static void pass() {

    }

    public void addSeekBarControl(
            String name, int minP, int maxP,
            Function<Integer, String> tv_set,
            TriConsumer<SeekBar, Integer, Boolean> onPrChanged,
            Consumer<SeekBar> onStartTouch,
            Consumer<SeekBar> onStopTouch,
            Consumer<SeekBar> updateMethod
    ) {
        sbcList.add(new SeekBarControl(
                linearLayout, context,
                name, minP, maxP,
                tv_set,
                onPrChanged,
                onStartTouch,
                onStopTouch,
                updateMethod
        ));
    }

    public void addSwitchControl(
            String name,
            BiConsumer<CompoundButton, Boolean> onCheckedChanged,
            Consumer<Switch> updateMethod
    ) {
        swcList.add(new SwitchControl(
                linearLayout, context,
                name,
                onCheckedChanged,
                updateMethod
        ));
    }

    public void addLine() {
        View view1 = new View(context);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 16);
        linearLayout.addView(view1, layoutParams1);

        View view2 = new View(context);
        view2.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray)); // 设置背景颜色
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        linearLayout.addView(view2, layoutParams2);

        View view3 = new View(context);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 16);
        linearLayout.addView(view3, layoutParams3);
    }

    public void update() {
        new Handler(Looper.getMainLooper()).post(() -> {
            // 在UI线程中更新UI组件
            for (int i = 0; i < sbcList.size(); i++) {
                sbcList.get(i).update();
            }
            for (int i = 0; i < swcList.size(); i++) {
                swcList.get(i).update();
            }
        });
    }

    private class SeekBarControl {
        private final LinearLayout linearLayout;
        private final Context context;
        private final String name;
        private final int minP;
        private final int maxP;
        private final Function<Integer, String> tv_set;
        private final TriConsumer<SeekBar, Integer, Boolean> onPrChanged;
        private final Consumer<SeekBar> onStartTouch;
        private final Consumer<SeekBar> onStopTouch;

        private final Consumer<SeekBar> updateMethod;

        private final SeekBar sb_control;
        private final TextView tv_control_set;

        public SeekBarControl(
                LinearLayout ll, Context c,
                String name, int minP, int maxP,
                Function<Integer, String> tv_set,
                TriConsumer<SeekBar, Integer, Boolean> onPrChanged,
                Consumer<SeekBar> onStartTouch,
                Consumer<SeekBar> onStopTouch,
                Consumer<SeekBar> updateMethod
        ) {
            linearLayout = ll;
            context = c;
            this.name = name;
            this.minP = minP;
            this.maxP = maxP;
            this.tv_set = tv_set;
            this.onPrChanged = onPrChanged;
            this.onStartTouch = onStartTouch;
            this.onStopTouch = onStopTouch;
            this.updateMethod = updateMethod;

            // TODO layout 高度设置无效

            LinearLayout cloneLayout = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.seekbar_control, null);

            TextView tv_control_name = cloneLayout.findViewById(R.id.tv_control_name);
            sb_control = cloneLayout.findViewById(R.id.sb_control);
            tv_control_set = cloneLayout.findViewById(R.id.tv_control_set);

            tv_control_name.setText(name);

            sb_control.setMin(minP);
            sb_control.setMax(maxP);
            sb_control.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    onPrChanged.accept(seekBar, progress, fromUser);
                    tv_control_set.setText(tv_set.apply(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    onStartTouch.accept(seekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    onStopTouch.accept(seekBar);
                }
            });

            linearLayout.addView(cloneLayout);

        }

        public void update() {
            updateMethod.accept(sb_control);
            tv_control_set.setText(tv_set.apply(sb_control.getProgress()));
        }
    }

    private class SwitchControl {
        private final LinearLayout linearLayout;
        private final Context context;
        private final String name;

        private final BiConsumer<CompoundButton, Boolean> onCheckedChanged;
        private final Consumer<Switch> updateMethod;

        private final Switch sw_control;

        public SwitchControl(
                LinearLayout ll, Context c,
                String name,
                BiConsumer<CompoundButton, Boolean> onCheckedChanged,
                Consumer<Switch> updateMethod
        ) {
            linearLayout = ll;
            context = c;
            this.name = name;
            this.onCheckedChanged = onCheckedChanged;
            this.updateMethod = updateMethod;

            LinearLayout cloneLayout = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.switch_control, null);

            sw_control = cloneLayout.findViewById(R.id.sw_control);

            sw_control.setText(name);
            sw_control.setOnCheckedChangeListener(onCheckedChanged::accept);

            linearLayout.addView(cloneLayout);

        }

        public void update() {
            updateMethod.accept(sw_control);
        }
    }
}
