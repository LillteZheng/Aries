package com.zhengsr.socketlib.nio.entrance.server;

import android.os.Build;

import com.zhengsr.socketlib.CloseUtils;
import com.zhengsr.socketlib.Lgg;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.nio.core.Consumer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.BreakIterator;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by  zhengshaorui on 2019/9/23
 * Describe: 处理 read 和 write 两种 selector 数据处理
 */
public class DataHandle {
    private DataClientListener mListener;
    private SocketChannel mChannel;
    private final WriteHandler mWriteHandler;
    private final DeviceInfo mInfo;

    public DataHandle(SocketChannel channel, DataClientListener listener) throws IOException {
        mChannel = channel;
        mListener = listener;
        //配置成非阻塞模式
        channel.configureBlocking(false);
        // 读的 selector key
        Selector readSelector = Selector.open();
        channel.register(readSelector, SelectionKey.OP_READ);
        new Consumer(channel, new Consumer.OnChannelListener() {
            @Override
            public void onMeassage(String msg) {
                mListener.onNewMsg(DataHandle.this,msg);
            }

            @Override
            public void onChannelClose(SocketChannel channel) {
                closeSelf();
            }
        });

        //写得 selector
        Selector writeSelector = Selector.open();
        channel.register(writeSelector, SelectionKey.OP_WRITE);
        mWriteHandler = new WriteHandler(writeSelector);

        String ip = channel.socket().getInetAddress().getHostAddress();
        int port = channel.socket().getPort();
        mInfo = new DeviceInfo(ip, port, "client connected");
        listener.onConnected(this, mInfo);
    }


    public void sendMsg(String msg) {
        mWriteHandler.sendMsg(msg);
    }

    public DeviceInfo getInfo() {
        return mInfo;
    }




    class WriteHandler {
        Selector selector;
        final ExecutorService executorService;
        final ByteBuffer buffer;
        boolean done;

        public WriteHandler(Selector selector) {
            this.selector = selector;
            executorService = Executors.newSingleThreadExecutor();
            buffer = ByteBuffer.allocate(256);
        }

        void exit() {
            done = true;
            executorService.shutdownNow();
            CloseUtils.close(selector);
        }

        void sendMsg(String msg) {
            if (done) {
                return;
            }
            executorService.execute(new sendSync(msg));
        }

        class sendSync implements Runnable {

            String msg;

            public sendSync(String msg) {
                this.msg = msg+"\n";
            }

            @Override
            public void run() {
                if (done) {
                    return;
                }
                if (msg != null) {
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
                            if (write < 0) {
                                mListener.onError("客户端已无法发送数据");
                                DataHandle.this.closeSelf();
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }


    /**
     * 关闭自身
     */
    private void closeSelf() {
        exit();
        mListener.onSelfClose(this, mInfo);
    }

    public void exit() {
        mWriteHandler.exit();
        CloseUtils.close(mChannel);
    }


    public interface DataClientListener {
        void onSelfClose(DataHandle client, DeviceInfo info);

        void onNewMsg(DataHandle client, String msg);

        void onError(String errorMsg);

        void onConnected(DataHandle client, DeviceInfo info);
    }
}
