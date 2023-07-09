package com.cjyyxn.screenfilter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cjyyxn.screenfilter.ui.MainUI;
import com.cjyyxn.screenfilter.ui.PreparatoryActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("ccjy", "MainActivity created");
        new MainUI(this);
    }

    @Override
    public void onResume() {
        super.onResume();

//        Log.d("ccjy", "开始 isAccessibility 检查");

//        // 可能GlobalStatus加载需要时间，只能这样判断 isAccessibility
//        for (int i = 0; i < 10000; i++) {
//            if (GlobalStatus.isAccessibility()) {
//                Log.d("ccjy", "isAccessibility = true, i = " + i);
//                break;
//            }
//        }

        if (!GlobalStatus.isReady()){
            Toast.makeText(this, "未设置必须的权限", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PreparatoryActivity.class));
        }
    }
}