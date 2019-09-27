package com.zhengsr.socketlib.nio.entrance.server;

import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.nio.core.Consumer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author by  zhengshaorui on 2019/9/23
 * Describe: 处理 readFrom 和 writeTo 两种 selector 数据处理
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
