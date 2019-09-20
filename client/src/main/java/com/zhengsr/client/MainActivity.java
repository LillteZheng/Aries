package com.zhengsr.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zhengsr.socketlib.Aries;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.udp.callback.DeviceListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DeviceListener {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void search(View view) {
        Aries.port(5566)
                .timeout(2000)
                .listener(this)
                .searchDevice();
    }

    @Override
    public void findDevice(List<DeviceInfo> deviceInfos) {
        Log.d(TAG, "zsr findDevice: "+deviceInfos.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Aries.stopSearcher();
    }
}
