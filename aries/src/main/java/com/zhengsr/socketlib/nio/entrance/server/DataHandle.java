package com.zhengsr.socketlib.nio.entrance.server;

import android.os.Build;

import com.zhengsr.socketlib.CloseUtils;
import com.zhengsr.socketlib.Lgg;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.nio.core.Consumer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.BreakIterator;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by  zhengshaorui on 2019/9/23
 * Describe: 处理 read 和 write 两种 selector 数据处理
 */
public class DataHandle extends Consumer {
    private DataClientListener mListener;
    private final DeviceInfo mInfo;

    public DataHandle(SocketChannel channel, DataClientListener listener) throws IOException {
        mListener = listener;
        setUp(channel, new OnChannelListener() {
            @Override
            public void onMeassage(String msg) {
                mListener.onNewMsg(DataHandle.this,msg);
            }

            @Override
            public void onChannelClose(SocketChannel channel) {
                closeSelf();
            }
        });

        String ip = channel.socket().getInetAddress().getHostAddress();
        int port = channel.socket().getPort();
        mInfo = new DeviceInfo(ip, port, "client connected");
        listener.onConnected(this, mInfo);
    }




    public DeviceInfo getInfo() {
        return mInfo;
    }



    /**
     * 关闭自身
     */
    private void closeSelf() {
        exit();
        mListener.onSelfClose(this, mInfo);
    }

    public void exit() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface DataClientListener {
        void onSelfClose(DataHandle client, DeviceInfo info);
        void onNewMsg(DataHandle client, String msg);
        void onConnected(DataHandle client, DeviceInfo info);
    }
}
