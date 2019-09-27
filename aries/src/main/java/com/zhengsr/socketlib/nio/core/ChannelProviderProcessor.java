package com.zhengsr.socketlib.nio.core;

import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.callback.Sender;
import com.zhengsr.socketlib.nio.core.selector.IProvider;
import com.zhengsr.socketlib.utils.CloseUtils;
import com.zhengsr.socketlib.utils.Lgg;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe:
 */
public class ChannelProviderProcessor implements Sender{

    private IProvider mProvider;
    private AtomicBoolean mIsClosed = new AtomicBoolean(false);
    private SocketChannel mChannel;
    private IoArgs.IoArgsEventProcessor mOutputEventProcessor;
    public ChannelProviderProcessor(SocketChannel channel,IProvider provider) {
        mProvider = provider;
        mChannel = channel;
    }

    @Override
    public void sendAsync(IoArgs ioArgs, IoArgs.IoArgsEventProcessor processor) throws IOException {
        if (mIsClosed.get()){
            throw  new IOException("current channel is closed");
        }
        outputRunnable.setAttach(ioArgs);
        mOutputEventProcessor = processor;
        mProvider.registerOutput(mChannel,outputRunnable);
    }


    /**
     * 具体写数据到channel的实现
     */
    IProvider.HandleOutputRunnable outputRunnable = new IProvider.HandleOutputRunnable() {
        @Override
        public void canProviderOutput() {
            if (mIsClosed.get()){
                return;
            }
            //拿到一份可消费的 ioargs
            IoArgs args = (IoArgs) getAttach();
            IoArgs.IoArgsEventProcessor processor = mOutputEventProcessor;
            processor.onStart(args);
            try {
                if (args.writeTo(mChannel) > 0){
                    processor.onCompleted(args);
                }else{
                    throw new IOException("Cannot write any data!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

}
