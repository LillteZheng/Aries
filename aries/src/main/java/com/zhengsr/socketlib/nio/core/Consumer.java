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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author by  zhengshaorui on 2019/9/24
 * Describe: 消费者
 */
public class Consumer implements Closeable{

    private  IProvider mProvider;
    private SocketChannel mChannel;
    private OnChannelListener mListener;
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

    IProvider.HandleOutputRunnable outputRunnable = new IProvider.HandleOutputRunnable() {
        @Override
        public void canProviderOutput() {
            String msg = (String) getAttach()+"\n";
            Lgg.d("发送: "+msg);
            if (msg != null) {
                ByteBuffer buffer = ByteBuffer.allocate(256);
                buffer.clear();
                //把byte的数据填充到 msg 中
                buffer.put(msg.getBytes());
                //切换到读模式，这样才能拿到数据
                buffer.flip();
                //直到读取完毕为止
                while (buffer.hasRemaining()) {
                    //接着把数据写到 channel 去
                    try {
                        int write = mChannel.write(buffer);
                        Lgg.d("write: "+write);
                        if (write < 0) {
                          //  mListener.onError("客户端已无法发送数据");
                         //   DataHandle.this.closeSelf();
                            close();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
