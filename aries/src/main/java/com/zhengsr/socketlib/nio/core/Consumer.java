package com.zhengsr.socketlib.nio.core;

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
    private IoArgs mIoBuffer = new IoArgs();
    public void setUp(SocketChannel channel, OnChannelListener listener) throws IOException {
        mListener = listener;
        mChannel = channel;
        channel.configureBlocking(false);

        mProvider = IoSelector.getProvider();

        readNextMsg();
    }

    /**
     * 读数据
     */
    private void readNextMsg() {
       mProvider.registerInput(mChannel,inputRunnable);
    }

    public void sendMsg(String msg){
        outputRunnable.setAttach(msg);
        mProvider.registerOutput(mChannel,outputRunnable);
    }


    IProvider.HandleInputRunnable inputRunnable = new IProvider.HandleInputRunnable() {
        @Override
        public void canProviderInput() {
            try {

                if (mIoBuffer.read(mChannel) > 0) {
                    //强行去掉换行符,后面再优化
                    String msg = mIoBuffer.string();
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
            }
        }
    };

    IProvider.HandleOutputRunnable outputRunnable = new IProvider.HandleOutputRunnable() {
        @Override
        public void canProviderOutput() {
            String msg = (String) getAttach()+"\n";
            if (msg != null) {

                try {
                    int write = mIoBuffer.write(mChannel,msg);
                    if (write < 0) {
                        close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
