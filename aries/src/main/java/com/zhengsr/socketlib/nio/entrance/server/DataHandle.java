package com.zhengsr.socketlib.nio.entrance.server;

import android.os.Build;

import com.zhengsr.socketlib.CloseUtils;
import com.zhengsr.socketlib.Lgg;
import com.zhengsr.socketlib.bean.DeviceInfo;

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
    private final ClientReadHandler mReadHandler;
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
        mReadHandler = new ClientReadHandler(readSelector);
        mReadHandler.start();

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


    /**
     * 读监听
     */
    class ClientReadHandler extends Thread {
        Selector selector;
        boolean done;
        ByteBuffer buffer;

        public ClientReadHandler(Selector selector) {
            this.selector = selector;
            buffer = ByteBuffer.allocate(256);
        }

        @Override
        public void run() {
            super.run();
            try {
                while (!done) {
                    if (selector.select() == 0) {
                        if (done) {
                            break;
                        }
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    Lgg.d("what?: " + iterator.hasNext());
                    while (iterator.hasNext()) {
                        if (done) {
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            //先清空数据
                            buffer.clear();

                            //拿到数据
                            int read = channel.read(buffer);
                            if (read > 0) {
                                //buffer.flip();
                                //强行去掉换行符,后面再优化
                                String msg = new String(buffer.array(), 0, buffer.position() - 1);
                                mListener.onNewMsg(DataHandle.this, msg);
                            } else {
                                mListener.onError("客户端已经无法读取信息");
                                DataHandle.this.closeSelf();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                exit();
            }

        }


        public void exit() {
            done = true;
            CloseUtils.close(selector);
        }
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
                this.msg = msg + "\n";
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
        mReadHandler.exit();
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
