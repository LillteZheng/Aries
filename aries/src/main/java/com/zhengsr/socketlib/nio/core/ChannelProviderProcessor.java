package com.zhengsr.socketlib.nio.core;

import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.callback.Receiver;
import com.zhengsr.socketlib.nio.core.callback.Sender;
import com.zhengsr.socketlib.nio.core.selector.IProvider;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe:
 */
public class ChannelProviderProcessor implements Sender,Receiver {

    private IProvider mProvider;
    private AtomicBoolean mIsClosed = new AtomicBoolean(false);
    private SocketChannel mChannel;
    private IoArgs.IoArgsEventProcessor mOutputEventProcessor;
    private IoArgs.IoArgsEventProcessor mInputEventProcessor;
    private IoArgs mReceiverArgs;
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

    @Override
    public void receiveAsync(IoArgs args, IoArgs.IoArgsEventProcessor processor) throws IOException {
        if (mIsClosed.get()){
            throw  new IOException("current channel is closed");
        }
        mReceiverArgs = args;
        mInputEventProcessor = processor;
        mProvider.registerInput(mChannel,inputRunnable);
    }


    IProvider.HandleInputRunnable inputRunnable = new IProvider.HandleInputRunnable() {
        @Override
        public void canProviderInput() {


            IoArgs args = mReceiverArgs;

            IoArgs.IoArgsEventProcessor processor = mInputEventProcessor;
            processor.onStart(args);

            try {
                if (args.readFrom(mChannel) > 0){
                    processor.onCompleted(args);
                }else{
                    throw new IOException("Cannot readFrom any data!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

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
