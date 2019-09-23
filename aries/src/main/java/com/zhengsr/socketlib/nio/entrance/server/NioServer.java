package com.zhengsr.socketlib.nio.entrance.server;

import android.os.Build;

import com.zhengsr.socketlib.CloseUtils;
import com.zhengsr.socketlib.Lgg;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.nio.callback.TcpServerListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by  zhengshaorui on 2019/9/23
 * Describe: nio 服务端，充当一个中转站，把收到客户端的信息转发给其他客户端
 */
public class NioServer {

    private Selector mSelector;
    private HandleAccept mHandleAccept;
    private TcpServerListener mListener;
    private List<DataHandle> mDataHandletList = new ArrayList<>();
    //转发线程池
    private ExecutorService mForwordThreadPool = Executors.newSingleThreadExecutor();

    private ServerSocketChannel mServerChannel;



    public NioServer(){}


    public NioServer listener(TcpServerListener listener){
        mListener = listener;
        return this;
    }

    public void start(int port){
        try {
            mSelector = Selector.open();
            mServerChannel = ServerSocketChannel.open();
            mServerChannel.configureBlocking(false);
            mServerChannel.socket().bind(new InetSocketAddress(port));
            mServerChannel.register(mSelector, SelectionKey.OP_ACCEPT);
            mHandleAccept = new HandleAccept();
            mHandleAccept.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void stop() {
        for (DataHandle dataHandle : mDataHandletList) {
            dataHandle.exit();
        }
        mDataHandletList.clear();
        mHandleAccept.exit();
        CloseUtils.close(mSelector);
        CloseUtils.close(mServerChannel);
        mForwordThreadPool.shutdownNow();
    }

    /**
     * 发送多个数据
     * @param msg
     */
    public void sendBroMsg(String msg) {
        synchronized (this){
            for (DataHandle handle : mDataHandletList) {
                handle.sendMsg(msg);
            }
        }
    }

    class HandleAccept extends Thread{
        boolean done;
        @Override
        public void run() {
            while (!done){
                try {
                    //防止空轮询的问题；这里应该参考netty的做法，当出现几次空轮询的selector时，应
                    //丢弃掉这个selector，后面再优化
                    if (mSelector.select() == 0){
                        if (done){
                            break;
                        }
                        continue;
                    }
                    //注册感兴趣的事件
                    Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        if (done){
                            break;
                        }
                        //单线程可以使用这样的做法
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        //如果是接入事件
                        if (key.isAcceptable()){
                            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                            //拿到客户端
                            SocketChannel accept = channel.accept();

                            //过滤已经重复添加的
                            if (checkClientExisit(accept)){
                                continue;
                            }

                            /**
                             * 这里应该把 accept 的输入输出流，单独开一个类，用来做中转的作用
                             * 里面应该的读写，应该也是用线程来实现
                             */
                            new DataHandle(accept,new DataListener());

                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        void exit(){
            done =true;
            CloseUtils.close(mSelector);
        }
    }


    /**
     * 状态监听
     */
    class DataListener implements DataHandle.DataClientListener{

        @Override
        public synchronized void onSelfClose(DataHandle client, DeviceInfo info) {
            mDataHandletList.remove(client);
            mListener.onClientDisconnect(info);
            mListener.onClientCount(mDataHandletList.size());
        }

        @Override
        public void onNewMsg(final DataHandle client,final String msg) {
            //先传输给自己
            mListener.onResponse(msg);
            //转发给其他客户端
            mForwordThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (NioServer.this){
                        for (DataHandle handle : mDataHandletList) {
                            //过滤自身
                            if (handle == client){
                                continue;
                            }
                            //转发信息
                            handle.sendMsg(msg);
                        }
                    }
                }
            });

        }

        @Override
        public void onError(String errorMsg) {
            Lgg.d("onError"+errorMsg);
        }

        @Override
        public synchronized void onConnected(DataHandle client, DeviceInfo info) {
            mDataHandletList.add(client);
            mListener.onClientCount(mDataHandletList.size());
            mListener.onClientConnected(info);
        }
    }

    /**
     * 检测是否已经存在了，防止重复添加
     * @param socket
     * @return
     */
    private synchronized boolean checkClientExisit(SocketChannel socket){
        String ip = socket.socket().getInetAddress().getHostAddress();
        for (DataHandle handle : mDataHandletList) {
            DeviceInfo info = handle.getInfo();
            if (info.ip.equals(ip)){
                return true;
            }
        }
        return false;
    }
}
