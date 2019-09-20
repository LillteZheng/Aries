package com.zhengsr.sockettest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhengsr.socketlib.Aries;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Aries.port(5566).startProvider();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Aries.stopProvider();
    }
}
