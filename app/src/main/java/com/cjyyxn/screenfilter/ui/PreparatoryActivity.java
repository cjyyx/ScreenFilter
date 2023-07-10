package com.cjyyxn.screenfilter.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cjyyxn.screenfilter.GlobalStatus;
import com.cjyyxn.screenfilter.R;

public class PreparatoryActivity extends AppCompatActivity {

    private Button pbt0;
    private Button pbt1;
    private Button pbt2;
    private Button pbt3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparatory);

        pbt0 = findViewById(R.id.pbt0);
        pbt1 = findViewById(R.id.pbt1);
        pbt2 = findViewById(R.id.pbt2);
        pbt3 = findViewById(R.id.pbt3);

        pbt0.setOnClickListener(view -> onButton0());
        pbt1.setOnClickListener(view -> onButton1());

        pbt2.setOnClickListener(view -> onButton2());

        pbt3.setOnClickListener(view -> onButton3());

//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//
//                if (GlobalStatus.isAccessibility()) {
//                    finish();
//                }
//
//            }
//        };
//
//        // 每隔 0.1秒钟执行一次任务
//        timer.schedule(task, 0, 100);
    }

    private void onButton0() {
        if (GlobalStatus.isAccessibility()) {
            Toast.makeText(this, "无障碍已打开", Toast.LENGTH_SHORT).show();
        } else {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "无障碍设置界面不可用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onButton1() {

        if (GlobalStatus.isReady()) {
            Toast.makeText(this, "系统设置权限已开启", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", "com.cjyyxn.screenfilter", null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private void onButton2() {
        if (GlobalStatus.isReady()) {
            finish();
        } else {
            Toast.makeText(this, "未设置必须的权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void onButton3() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", "com.cjyyxn.screenfilter", null);
        intent.setData(uri);
        startActivity(intent);
    }
}