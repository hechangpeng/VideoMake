package com.videocreator.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.videocreator.example.R;

/**
 * Date：2018/4/19
 * Author：HeChangPeng
 */
public class CameraRecordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_record);
        Toast.makeText(this, "功能暂未实现", Toast.LENGTH_SHORT).show();
        finish();
    }
}
