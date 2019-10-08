package com.zhengsr.socketlib.nio.core.async;

import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.callback.Receiver;
import com.zhengsr.socketlib.nio.core.packet.ReceivePacket;
import com.zhengsr.socketlib.nio.core.packet.box.StringReceivePacket;
import com.zhengsr.socketlib.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe: 处理消息黏包和消息不完整的具体接收类
 */
public class ReceiveDispatcherAsync implements Closeable {
    private Receiver mReceiver;
    private IoArgs mIoArgs = new IoArgs();
    private ReceivePacket<?> mTempPacket;
    private WritableByteChannel mTempChannel;
    private long mTotal,mPosition;

    private onReceivePacketListener mOnReceivePacketListener;
    public ReceiveDispatcherAsync(Receiver receiver,onReceivePacketListener listener) {
        this.mReceiver = receiver;
        mOnReceivePacketListener = listener;
        mReceiver.setReceiveListener(processor);
        readNextPacket();
    }

    private void readNextPacket() {
        try {
            mReceiver.receiveAsync(mIoArgs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析[header][data+data..]
     * @param args
     */
    private void assemblePacket(IoArgs args) {
        //header 部分
        if (mTempPacket == null){
            int length = args.readLength();
            //直接解析 string packet
            mTempPacket = new StringReceivePacket(length);
            mTotal = length;
            mPosition = 0;
            mTempChannel = Channels.newChannel(mTempPacket.open());
        }else{
            //data部分,要把args的数据，拿出来放到 byte[]里面

            try {
                int count = args.writeTo(mTempChannel);
                if (count > 0) {
                    mPosition += count;
                    if (mPosition >= mTotal) {
                        //已经完成了一份 packet 的接收
                        consumeSuccess();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void consumeSuccess() {
        ReceivePacket packet = mTempPacket;
        CloseUtils.close(mTempChannel);
        CloseUtils.close(packet);
        mTempChannel = null;
        mTempPacket = null;
        mTotal = 0;
        mPosition = 0;
        //用临时变量，清空全局变量
        if (mOnReceivePacketListener != null) {
            mOnReceivePacketListener.onReceiver(packet);
        }
    }

    IoArgs.IoArgsEventProcessor processor = new IoArgs.IoArgsEventProcessor() {
        @Override
        public IoArgs providerIoArgs() {
            IoArgs args = mIoArgs;
            int receiveLength;
            if (mTempPacket == null){
                receiveLength = 4;
            }else{
                receiveLength = (int) Math.min(mTotal - mPosition,args.capacity());
            }
            //构建 [header][data][data]
            args.limit(receiveLength);
            return args;
        }

        @Override
        public void onConsumeCompleted(IoArgs args) {
            //解析packet 和 接收下一个包
            assemblePacket(args);
            readNextPacket();
        }


    };

    @Override
    public void close() throws IOException {
        consumeSuccess();
    }

    public interface onReceivePacketListener{
        void onReceiver(ReceivePacket packet);
    }
}
