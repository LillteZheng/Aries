package com.zhengsr.socketlib.nio.core.async;

import android.os.Build;

import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.callback.Sender;
import com.zhengsr.socketlib.nio.core.packet.SendPacket;
import com.zhengsr.socketlib.nio.core.selector.IProvider;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.zhengsr.socketlib.utils.CloseUtils.close;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe:
 */
public class SendDispatcherAsync {

    private Queue<SendPacket> mQueue;
    private AtomicBoolean mIsSending = new AtomicBoolean(false);

    /**
     * 长度和进度
     */
    private int mTotal;
    private int mPosition;
    private Sender mSender;
    private IoArgs mIoArgs = new IoArgs();
    private SendPacket mPacketTemp;
    public SendDispatcherAsync(Sender sender) {
        mSender = sender;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mQueue = new ConcurrentLinkedDeque<>();
        }else{
            mQueue = new ConcurrentLinkedQueue<>();
        }
    }

    public void sendPacket(SendPacket packet){
        mQueue.offer(packet);
        if (mIsSending.compareAndSet(false,true)){
            sendNextPacket();
        }
    }

    /**
     * 发送下一个包
     */
    private void sendNextPacket() {

        SendPacket packet = mPacketTemp = mQueue.poll();
        if (packet == null){
            //取消发送状态
            mIsSending.set(false);
            return;
        }
        //准备发送
        mTotal = packet.length;
        mPosition = 0;
        
        sendCurrentPacket();
    }
    /**
     * 发送真正的数据,在发送之前，先检测是否发送完成
     * 当未发送完成，重新注册即可
     */
    private void sendCurrentPacket() {
        //把数据写到 ioargs ,再通过它传递给 provider 去消费
        IoArgs args = mIoArgs;
        //先清掉以前的数据
        args.startWriting();

        if (mPosition >= mTotal){
            sendNextPacket();
            return;
        }else if (mPosition == 0){
            //如果是首包,把长度信息写入
            args.writeLength(mTotal);
        }

        byte[] bytes = mPacketTemp.bytes();
        //把数据写入到buffer中，并通过 args 传递给channel
        int count = args.readFrom(bytes,mPosition);
        mPosition += count;
        args.finishWriting();
        //这样就构成了 [header][data+data..] 的形式
        try {
            mSender.sendAsync(args,ioArgsEventProcessor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    IoArgs.IoArgsEventProcessor ioArgsEventProcessor = new IoArgs.IoArgsEventProcessor() {

        @Override
        public void onStart(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            sendCurrentPacket();
        }
    };


}
