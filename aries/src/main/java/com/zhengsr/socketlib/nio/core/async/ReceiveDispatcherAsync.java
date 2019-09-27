package com.zhengsr.socketlib.nio.core.async;

import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.callback.Receiver;

import java.io.IOException;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe:
 */
public class ReceiveDispatcherAsync {
    private Receiver mReceiver;
    private IoArgs mIoArgs = new IoArgs();
    public ReceiveDispatcherAsync(Receiver receiver) {
        this.mReceiver = receiver;
        mReceiver.setProcessorListener(processor);
        readNextPacket();
    }

    private void readNextPacket() {
        try {
            mReceiver.receiveAsync(mIoArgs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    IoArgs.IoArgsEventProcessor processor = new IoArgs.IoArgsEventProcessor() {
        @Override
        public void onStart(IoArgs args) {
            //在开始接收前，解析[header][data+data..]，所以这里的 listener 只注册一次

            /**
             * 开始接收一个包之前，先解析header部分，即 4 个字节的长度信息
             */
        }

        @Override
        public void onCompleted(IoArgs args) {

        }
    };
}
