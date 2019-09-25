package com.zhengsr.socketlib.nio.core;

import com.zhengsr.socketlib.CloseUtils;
import com.zhengsr.socketlib.Lgg;
import com.zhengsr.socketlib.nio.core.selector.IProvider;
import com.zhengsr.socketlib.nio.entrance.server.DataHandle;
import com.zhengsr.socketlib.utils.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author by  zhengshaorui on 2019/9/24
 * Describe: 消费者
 */
public class Consumer implements Closeable{

    private final IProvider mProvider;
    private SocketChannel mChannel;
    private OnChannelListener mListener;
    public Consumer(SocketChannel channel, OnChannelListener listener) throws IOException {
        mListener = listener;
        mChannel = channel;
        channel.configureBlocking(false);

        mProvider = IoSelector.getProvider();

        readNextMsg();
    }

    private void readNextMsg() {
       mProvider.registerInput(mChannel,inputRunnable);
    }


    IProvider.HandleInputRunnable inputRunnable = new IProvider.HandleInputRunnable() {
        @Override
        public void canProviderInput() {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(256);
                //先清空数据
                buffer.clear();

                //拿到数据
                int read = mChannel.read(buffer);
                if (read > 0) {
                    //buffer.flip();
                    //强行去掉换行符,后面再优化
                    String msg = new String(buffer.array(), 0, buffer.position());
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
