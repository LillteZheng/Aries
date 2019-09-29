package com.zhengsr.socketlib.nio.core;

import com.zhengsr.socketlib.nio.IoArgs;
import com.zhengsr.socketlib.nio.core.callback.Receiver;
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
public class ChannelProviderProcessor implements Sender,Receiver {

    private IProvider mProvider;
    private AtomicBoolean mIsClosed = new AtomicBoolean(false);
    private SocketChannel mChannel;
    private IoArgs.IoArgsEventProcessor mOutputEventProcessor;
    private IoArgs.IoArgsEventProcessor mInputEventProcessor;
    private IoArgs mReceiverArgs;
    private OnChannelStatusChangedListener mListener;
    public ChannelProviderProcessor(SocketChannel channel,IProvider provider,OnChannelStatusChangedListener listener) {
        mProvider = provider;
        mChannel = channel;
        mListener = listener;
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
    public void setProcessorListener(IoArgs.IoArgsEventProcessor processor) {
        mInputEventProcessor = processor;
    }

    @Override
    public void receiveAsync(IoArgs args) throws IOException {
        if (mIsClosed.get()){
            throw  new IOException("current channel is closed");
        }
        mReceiverArgs = args;

        mProvider.registerInput(mChannel,inputRunnable);
    }




    IProvider.HandleInputRunnable inputRunnable = new IProvider.HandleInputRunnable() {
        @Override
        public void canProviderInput() {


            IoArgs args = mReceiverArgs;

            IoArgs.IoArgsEventProcessor processor = mInputEventProcessor;
            processor.onStart(args);

            try {
                int read = args.readFrom(mChannel);
                if (read > 0){
                    processor.onCompleted(args);
                }else{
                    throw new IOException("cannot read anymore ");
                }
            } catch (IOException e) {
                Lgg.d("e.: "+e.toString());
                close();
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
            IoArgs args =  getAttach();
            IoArgs.IoArgsEventProcessor processor = mOutputEventProcessor;
            processor.onStart(args);
            try {
                int write = args.writeTo(mChannel);
                if (write > 0){
                    processor.onCompleted(args);
                }else{
                   throw new IOException("cannot write anymore ");
                }
            } catch (IOException e) {
                Lgg.d("已经没法写了: "+e.toString());
                close();
            }
        }
    };


    @Override
    public void close() {
        if (mIsClosed.compareAndSet(false,true)){
            //解除注册
            mProvider.unRegisterInput(mChannel);
            mProvider.unRegisterOutput(mChannel);

            //关闭通道
            CloseUtils.close(mChannel);

            //通知外面
            if (mListener != null) {
                mListener.onChannelClosed(mChannel);
            }
        }
    }

    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
