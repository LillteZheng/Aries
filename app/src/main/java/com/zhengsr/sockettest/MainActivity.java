package com.zhengsr.sockettest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zhengsr.socketlib.Aries;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.nio.callback.TcpClientListener;
import com.zhengsr.socketlib.nio.callback.TcpServerListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private EditText mEditText;
    private TextView mTextView;
    private StringBuilder sb = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Aries.udp().startProvider();
        Aries.get()
                .port(30401)
                .listener(new TransListener())
                .startServer();
        mEditText = findViewById(R.id.edittext);
        mTextView = findViewById(R.id.textview);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Aries.udp().stopProvider();
        Aries.get().stopServer();
    }

    public void send(View view) {
        String msg = mEditText.getText().toString();
        Aries.get().sendServerBroMsg(msg);
        sb.append("你发送了: "+msg);
        mTextView.setText(sb.toString());
        mEditText.setText("");
    }

    /**
     * 接收信息
     */
    class TransListener implements TcpServerListener {


        @Override
        public void onResponse(final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sb.append("客户端: " + msg).append("\n");
                    mTextView.setText(sb.toString());
                }
            });
        }

        @Override
        public void onClientCount(int count) {
            if (count > 0) {
                sb.append("共有: " + count + "个连接到服务器").append("\n");
                mTextView.setText(sb.toString());
            }
        }

        @Override
        public void onClientConnected(DeviceInfo info) {
            sb.append("有客户端接入: "+info.ip).append("\n");
            mTextView.setText(sb.toString());
        }

        @Override
        public void onClientDisconnect(DeviceInfo info) {
            sb.append("客户端: "+info.ip+" "+"退出群聊").append("\n");
            mTextView.setText(sb.toString());
        }
    }
}
