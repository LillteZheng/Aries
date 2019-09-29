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
        setUp(channel);

        String ip = channel.socket().getInetAddress().getHostAddress();
        int port = channel.socket().getPort();
        mInfo = new DeviceInfo(ip, port, "client connected");
        listener.onConnected(this, mInfo);
    }




    public DeviceInfo getInfo() {
        return mInfo;
    }


    @Override
    public void onNewMessage(String msg) {
        super.onNewMessage(msg);
        mListener.onNewMsg(this,msg);
    }

    public void exit() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exit();
        mListener.onSelfClose(this, mInfo);
    }

    public interface DataClientListener {
        void onSelfClose(DataHandle client, DeviceInfo info);
        void onNewMsg(DataHandle client, String msg);
        void onConnected(DataHandle client, DeviceInfo info);
    }
}
