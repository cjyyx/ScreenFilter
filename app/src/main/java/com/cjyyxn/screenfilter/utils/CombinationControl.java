package com.cjyyxn.screenfilter.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cjyyxn.screenfilter.R;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;


public class CombinationControl {

    private final LinearLayout linearLayout;
    private final Context context;

    private ArrayList<SeekBarControl> sbcList = new ArrayList<SeekBarControl>();

    public CombinationControl(LinearLayout ll, Context c) {
        linearLayout = ll;
        context = c;
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

    public void update() {
        new Handler(Looper.getMainLooper()).post(() -> {
            // 在UI线程中更新UI组件
            for (int i = 0; i < sbcList.size(); i++) {
                sbcList.get(i).update();
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
}
