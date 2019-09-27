package com.zhengsr.socketlib.nio.core;

import com.zhengsr.socketlib.nio.core.async.ReceiveDispatcherAsync;
import com.zhengsr.socketlib.nio.core.async.SendDispatcherAsync;
import com.zhengsr.socketlib.nio.core.callback.Receiver;
import com.zhengsr.socketlib.nio.core.callback.Sender;
import com.zhengsr.socketlib.nio.core.packet.SendPacket;
import com.zhengsr.socketlib.nio.core.packet.box.StringSendPacket;
import com.zhengsr.socketlib.nio.core.selector.IoSelector;
import com.zhengsr.socketlib.utils.CloseUtils;
import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.selector.IProvider;
import com.zhengsr.socketlib.utils.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author by  zhengshaorui on 2019/9/24
 * Describe: 消费者
 */
public class Consumer implements Closeable{

    private  IProvider mProvider;
    private SocketChannel mChannel;
    private OnChannelListener mListener;
    private IoArgs mIoArgs = new IoArgs();
    private SendDispatcherAsync mSendDispatcher;
    private ReceiveDispatcherAsync mReceiveDispatcher;
    private Sender mSender;
    private Receiver mReceiver;

    public void setUp(SocketChannel channel, OnChannelListener listener) throws IOException {
        mListener = listener;
        mChannel = channel;
        channel.configureBlocking(false);


        ChannelProviderProcessor processor = new ChannelProviderProcessor(channel,IoSelector.getProvider());
        mSender = processor;
        mReceiver = processor;
        mSendDispatcher = new SendDispatcherAsync(mSender);
        mReceiveDispatcher = new ReceiveDispatcherAsync(mReceiver);
    }


    /**
     * 发送字符串
     * @param msg
     */
    public void sendMsg(String msg){
        SendPacket packet = new StringSendPacket(msg);
        mSendDispatcher.sendPacket(packet);
    }





    @Override
    public void close() throws IOException {
        mProvider.unRegisterInput(mChannel);
        CloseUtils.close(mChannel);
        mListener.onChannelClose(mChannel);
    }

    public interface OnChannelListener{
        void onMeassage(String msg);
        void onChannelClose(SocketChannel channel);
    }
}
