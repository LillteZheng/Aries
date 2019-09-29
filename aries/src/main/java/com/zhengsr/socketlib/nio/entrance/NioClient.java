package com.zhengsr.socketlib.nio.entrance;

import android.net.IpPrefix;

import com.zhengsr.socketlib.Aries;
import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.Consumer;
import com.zhengsr.socketlib.nio.core.selector.IoProviderSelector;
import com.zhengsr.socketlib.nio.core.selector.IoSelector;
import com.zhengsr.socketlib.utils.CloseUtils;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.nio.callback.TcpClientListener;
import com.zhengsr.socketlib.utils.Lgg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by  zhengshaorui on 2019/9/23
 * Describe: Nio 客户端
 */
public class NioClient extends Consumer{
    private ExecutorService mExecutorService ;
    private TcpClientListener mListener;
    private SocketChannel mChannel;

    private DeviceInfo mInfo;
    public NioClient(){
        try {
            IoSelector.setProvider(new IoProviderSelector());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mExecutorService = Executors.newSingleThreadExecutor();
    }
    public NioClient listener(TcpClientListener listener){
        mListener = listener;
        return this;
    }
    public void start(final String ip, final int port){
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mChannel = SocketChannel.open();
                    mChannel.connect(new InetSocketAddress(InetAddress.getByName(ip),port));
                    setUp(mChannel);
                    connectSuccess(mChannel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }



    @Override
    public void onNewMessage(String msg) {
        if (mListener != null) {
            mListener.onResponse(msg);
        }
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        if (mListener != null) {
            mListener.serverDisconnect(mInfo);
        }
    }

    private void connectSuccess(final SocketChannel socket) {
        if (mListener != null){
            Aries.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String ip = socket.socket().getInetAddress().getHostAddress();
                    int port = socket.socket().getPort();
                    mInfo = new DeviceInfo();
                    mInfo.ip = ip;
                    mInfo.port = port;
                    mInfo.info = "server connect success";
                    mListener.serverConnected(mInfo);
                }
            });
        }
    }



    public void stop() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
        }


    }
}
