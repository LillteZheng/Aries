package com.zhengsr.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zhengsr.socketlib.Aries;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.nio.callback.TcpClientListener;
import com.zhengsr.socketlib.udp.callback.DeviceListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DeviceListener {
    private static final String TAG = "MainActivity";
    private TextView mTextView;
    private EditText mEditText;
    StringBuilder sb = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "zsr search: ");
    }

    public void search(View view) {
        Aries.udp()
                .timeout(2000)
                .listener(this)
                .searchDevice();

        mTextView = findViewById(R.id.textview);
        mEditText = findViewById(R.id.edittext);


    }

    @Override
    public void findDevice(List<DeviceInfo> deviceInfos) {
        if (deviceInfos.size() > 0){

            DeviceInfo info = deviceInfos.get(0);
            Aries.get()
                    .port(30401)
                    .ip(info.ip)
                    .listener(new TransListener())
                    .startClient();
        }else{
            sb.append("没有设备");
            sb.append("\n");
            mTextView.setText(sb.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Aries.udp().stopSearch();
        Aries.get().stopClient();
    }

    public void send(View view) {
        String msg = mEditText.getText().toString();
        Aries.get().sendClientMsg(msg);
        sb.append("你发送了: "+msg).append("\n");
        mTextView.setText(sb.toString());
        mEditText.setText("");
    }

    /**
     * 接收信息
     */
    class TransListener implements TcpClientListener{

        @Override
        public void serverConnected(DeviceInfo info) {
            sb = new StringBuilder();
            sb.append("连接上服务器: "+info.ip).append("\n");
            mTextView.setText(sb.toString());
        }

        @Override
        public void serverDisconnect(DeviceInfo info) {
            sb.append("服务端: "+info.ip+" "+"退出群聊").append("\n");
            mTextView.setText(sb.toString());
        }

        @Override
        public void serverConnectFail(String msg) {

        }


        @Override
        public void onResponse(final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sb.append("服务端信息: " + msg).append("\n");
                    mTextView.setText(sb.toString());
                }
            });
        }
    }
}
