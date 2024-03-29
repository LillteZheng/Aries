package com.zhengsr.socketlib.nio.core.async;

import android.os.Build;

import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.callback.Sender;
import com.zhengsr.socketlib.nio.core.packet.SendPacket;
import com.zhengsr.socketlib.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe:处理消息黏包和消息不完整的具体发送类
 */
public class SendDispatcherAsync implements Closeable{
    private static final String TAG = "SendDispatcherAsync";
    private Queue<SendPacket> mQueue;
    private AtomicBoolean mIsSending = new AtomicBoolean(false);

    /**
     * 长度和进度
     */
    private long mTotal;
    private long mPosition;
    private Sender mSender;
    private IoArgs mIoArgs = new IoArgs();
    private SendPacket mTempPacket;
    private ReadableByteChannel mTempChannel;
    public SendDispatcherAsync(Sender sender) {
        mSender = sender;
        mSender.setSendListener(ioArgsEventProcessor);
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

        SendPacket packet = mTempPacket = mQueue.poll();
        if (packet == null){
            //取消发送状态
            mIsSending.set(false);
            return;
        }
        //准备发送
        mTotal = packet.length;
        mPosition = 0;
        
        sendCurrentPacket(mIoArgs);
    }
    /**
     * 发送真正的数据,在发送之前，先检测是否发送完成
     * 当未发送完成，重新注册即可
     */
    private void sendCurrentPacket(IoArgs args) {

        if (mPosition >= mTotal){
            consumeSuccess();
            sendNextPacket();
            return;
        }
        try {
            mSender.sendAsync(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void consumeSuccess() {
        CloseUtils.close(mTempChannel);
        CloseUtils.close(mTempPacket);
        mTempChannel = null;
        mTempPacket = null;
        mTotal = 0;
        mPosition = 0;
    }


    IoArgs.IoArgsEventProcessor ioArgsEventProcessor = new IoArgs.IoArgsEventProcessor() {

        @Override
        public IoArgs providerIoArgs() {
            //把数据写到 ioargs ,再通过它传递给 provider 去消费
            IoArgs args = mIoArgs;

            //如果byte通道为空，则为首包
            if (mTempChannel == null) {
                mTempChannel = Channels.newChannel((InputStream) mTempPacket.open());
                //因为是channel写数据，需要对bytebuffer进行数据限制
                //而之前byte数组的，已经在ioargs处理过，所以不需要
                args.limit(4);
                args.writeLength((int) mTotal);
            }else{
                args.limit((int) Math.min(args.capacity(),mTotal - mPosition));
                try {
                    int count = args.readFrom(mTempChannel);
                    mPosition += count;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            //这样就构成了 [header][data+data..] 的形式
            return args;
        }

        @Override
        public void onConsumeCompleted(IoArgs args) {
            sendCurrentPacket(args);
        }

    };


    @Override
    public void close() throws IOException {
        if (mIsSending.compareAndSet(false,true)) {
            mIsSending.set(false);
            mQueue.clear();
            consumeSuccess();
        }
    }
}
