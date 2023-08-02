package com.cjyyxn.screenfilter;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cjyyxn.screenfilter.ui.MainUI;
import com.cjyyxn.screenfilter.ui.PreparatoryActivity;

public class MainActivity extends AppCompatActivity {

    public boolean isInBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("ccjy", "MainActivity created");
        new MainUI(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        isInBackground = true;

        if (GlobalStatus.isHideInMultitaskingInterface()) {
            try {
                ActivityManager service = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                for (ActivityManager.AppTask task : service.getAppTasks()) {
                    if (task.getTaskInfo().taskId == getTaskId()) {
                        task.setExcludeFromRecents(true);
//                        Toast.makeText(this, "多任务界面隐藏成功", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
//                Toast.makeText(this, "多任务界面隐藏失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isInBackground = false;

        all_judge();
    }

    public void all_judge(){
        if (!GlobalStatus.isAgreePrivacyPolicy()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("滤镜护眼防频闪 隐私政策");

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_privacy_policy, null);
            builder.setView(view);

            builder.setPositiveButton("同意", (dialog, which) -> {
                GlobalStatus.setAgreePrivacyPolicy(true);
                ready_judge();
            });

            builder.setNegativeButton("取消", (dialog, which) -> {
                GlobalStatus.setAgreePrivacyPolicy(false);
//                    dialog.dismiss();
                finish();
            });


            AlertDialog dialog = builder.create();

            dialog.setOnCancelListener(dialogInterface -> {
                GlobalStatus.setAgreePrivacyPolicy(false);
                finish();
            });

            dialog.show();
        } else {
//            Log.d("ccjy", "已同意隐私政策");
            ready_judge();
        }
    }

    private void ready_judge(){
        if (!GlobalStatus.isReady()) {
            Toast.makeText(this, "未设置必须的权限", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PreparatoryActivity.class));
        }
    }
}