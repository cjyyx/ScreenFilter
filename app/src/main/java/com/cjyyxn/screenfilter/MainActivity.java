package com.cjyyxn.screenfilter;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cjyyxn.screenfilter.ui.MainUI;
import com.cjyyxn.screenfilter.ui.PreparatoryActivity;

public class MainActivity extends AppCompatActivity {

    private MainUI mainUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppConfig.init(this);

        Log.d("ccjy", "MainActivity created");
        mainUI = new MainUI(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        all_judge();

        mainUI.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (AppConfig.isHideInMultitaskingInterface()) {
            try {
                ActivityManager service = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                for (ActivityManager.AppTask task : service.getAppTasks()) {
                    if (task.getTaskInfo().taskId == getTaskId()) {
                        task.setExcludeFromRecents(true);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(this, "多任务界面隐藏成功", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "多任务界面隐藏失败", Toast.LENGTH_SHORT).show();
                });
            }
        }

        mainUI.onPause();

    }

    public void all_judge() {
        SharedPreferences shared = getSharedPreferences("share", Context.MODE_PRIVATE);
        if (!shared.getBoolean("agreePrivacyPolicy", false)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("滤镜护眼防频闪 隐私政策");

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_privacy_policy, null);
            builder.setView(view);

            builder.setPositiveButton("同意", (dialog, which) -> {
                SharedPreferences.Editor editor = shared.edit();
                editor.putBoolean("agreePrivacyPolicy", true);
                editor.apply();
                ready_judge();
            });

            builder.setNegativeButton("取消", (dialog, which) -> {
                finish();
            });


            AlertDialog dialog = builder.create();
            dialog.setOnCancelListener(dialogInterface -> {
                finish();
            });

            dialog.show();
        } else {
            Log.d("ccjy", "已同意隐私政策");
            ready_judge();
        }
    }

    private void ready_judge() {
        if (!GlobalStatus.isReady()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(this, "未设置必须的权限", Toast.LENGTH_SHORT).show();
            });
            startActivity(new Intent(this, PreparatoryActivity.class));
        }
    }
}