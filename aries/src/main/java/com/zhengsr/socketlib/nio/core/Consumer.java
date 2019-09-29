package com.zhengsr.socketlib.nio.core;

import com.zhengsr.socketlib.nio.core.async.ReceiveDispatcherAsync;
import com.zhengsr.socketlib.nio.core.async.SendDispatcherAsync;
import com.zhengsr.socketlib.nio.core.callback.Receiver;
import com.zhengsr.socketlib.nio.core.callback.Sender;
import com.zhengsr.socketlib.nio.core.packet.ReceivePacket;
import com.zhengsr.socketlib.nio.core.packet.SendPacket;
import com.zhengsr.socketlib.nio.core.packet.box.StringReceivePacket;
import com.zhengsr.socketlib.nio.core.packet.box.StringSendPacket;
import com.zhengsr.socketlib.nio.core.selector.IoSelector;
import com.zhengsr.socketlib.utils.CloseUtils;
import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.selector.IProvider;
import com.zhengsr.socketlib.utils.Lgg;
import com.zhengsr.socketlib.utils.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author by  zhengshaorui on 2019/9/24
 * Describe: 消费者
 */
public class Consumer implements Closeable,ChannelProviderProcessor.OnChannelStatusChangedListener {

    private SocketChannel mChannel;
    private SendDispatcherAsync mSendDispatcher;
    private ReceiveDispatcherAsync mReceiveDispatcher;
    private Sender mSender;
    private Receiver mReceiver;

    public void setUp(SocketChannel channel) throws IOException {
        mChannel = channel;
        channel.configureBlocking(false);


        ChannelProviderProcessor processor = new ChannelProviderProcessor(channel,
                IoSelector.getProvider(),this);
        mSender = processor;
        mReceiver = processor;
        mSendDispatcher = new SendDispatcherAsync(mSender);
        mReceiveDispatcher = new ReceiveDispatcherAsync(mReceiver,onReceivePacketListener);
    }


    /**
     * 发送字符串
     * @param msg
     */
    public void sendMsg(String msg){
        SendPacket packet = new StringSendPacket(msg);
        mSendDispatcher.sendPacket(packet);
    }



    ReceiveDispatcherAsync.onReceivePacketListener onReceivePacketListener = new ReceiveDispatcherAsync.onReceivePacketListener() {
        @Override
        public void onReceiver(ReceivePacket packet) {
            if (packet instanceof StringReceivePacket){
                onNewMessage(((StringReceivePacket) packet).string());
            }
        }
    };


    public void onNewMessage(String msg){

    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        Lgg.d("我自己关闭了");
    }

    @Override
    public void close() throws IOException {
        //关闭发送接收
        mSendDispatcher.close();
        mReceiveDispatcher.close();
        //关闭注册
        mSender.close();
        mReceiver.close();
        //关闭通道
        CloseUtils.close(mChannel);
    }
}
