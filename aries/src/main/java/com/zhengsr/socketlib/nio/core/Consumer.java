package com.zhengsr.socketlib.nio.core;

import com.zhengsr.socketlib.nio.core.async.SendDispatcherAsync;
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
    private Sender mSender;

    public void setUp(SocketChannel channel, OnChannelListener listener) throws IOException {
        mListener = listener;
        mChannel = channel;
        channel.configureBlocking(false);


        mSender = new ChannelProviderProcessor(channel,IoSelector.getProvider());
        mSendDispatcher = new SendDispatcherAsync(mSender);
        readNextMsg();
    }

    /**
     * 读数据
     */
    private void readNextMsg() {
       mProvider.registerInput(mChannel,inputRunnable);
    }

    /**
     * 发送字符串
     * @param msg
     */
    public void sendMsg(String msg){
        SendPacket packet = new StringSendPacket(msg);
        mSendDispatcher.sendPacket(packet);
    }


    IProvider.HandleInputRunnable inputRunnable = new IProvider.HandleInputRunnable() {
        @Override
        public void canProviderInput() {
            /*try {

                if (mIoArgs.readFrom(mChannel) > 0) {
                    //强行去掉换行符,后面再优化
                   // String msg = mIoArgs.string();
                    msg = StringUtils.removeBlank(msg);
                    if (msg.length() > 0){
                        mListener.onMeassage(msg);
                    }
                    //读取成功，再读取下调
                    readNextMsg();
                } else {
                    close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    };


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
