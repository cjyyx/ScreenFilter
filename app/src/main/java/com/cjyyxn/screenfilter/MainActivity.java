package com.cjyyxn.screenfilter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        if (!GlobalStatus.isReady()) {
            Toast.makeText(this, "未设置必须的权限", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PreparatoryActivity.class));
        }
    }
}