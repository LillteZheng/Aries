package com.zhengsr.socketlib.nio.entrance;

import com.zhengsr.socketlib.Aries;
import com.zhengsr.socketlib.utils.CloseUtils;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.nio.callback.TcpClientListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by  zhengshaorui on 2019/9/23
 * Describe: Nio 客户端
 */
public class NioClient {
    private ExecutorService mExecutorService ;
    private TcpClientListener mListener;
    private ReadHandler mReadHandler;
    private DeviceInfo mInfo;
    private Socket mSocket;
    private PrintStream mPs;


    public NioClient(){
        mExecutorService = Executors.newSingleThreadExecutor();
    }
    public NioClient listener(TcpClientListener listener){
        mListener = listener;
        return this;
    }
    public void start(final String ip, final int port){
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket();
                    int timeout = 3000;
                    mSocket.connect(new InetSocketAddress(InetAddress.getByName(ip),port),timeout);
                    connectSuccess(mSocket);
                    mReadHandler = new ReadHandler(mSocket.getInputStream());
                    mReadHandler.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    class  ReadHandler extends Thread{
        private BufferedReader br;
        private boolean done = false;
        InputStream stream;
        public ReadHandler(InputStream inputStream) {
            this.stream = inputStream;
            br = new BufferedReader(new InputStreamReader(stream));
        }

        @Override
        public void run() {
            super.run();
            try {
                while (!done){
                    String msg = br.readLine();
                    if (msg == null){
                       // System.out.println("连接断开");
                        Aries.HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.serverDisconnect(mInfo);
                            }
                        });
                        break;
                    }

                    mListener.onResponse(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                exit();
            }

        }
        public void exit(){
            done = true;
            //必须关闭 stream，不然socket会被占用
            CloseUtils.close(stream);
            CloseUtils.close(br);
        }
    }


    /**
     * 接受终端数据，并发送给服务端
     */
    public  void send(final String msg){
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mPs = new PrintStream(mSocket.getOutputStream());
                    mPs.println(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void connectSuccess(final Socket socket) {
        if (mListener != null){
            Aries.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String ip = socket.getInetAddress().getHostAddress();
                    int port = socket.getPort();
                    mInfo = new DeviceInfo();
                    mInfo.ip = ip;
                    mInfo.port = port;
                    mInfo.info = "server connected";
                    mListener.serverConnected(mInfo);
                }
            });
        }


    }

    public void stop() {
        if (mReadHandler != null) {
            mReadHandler.exit();
        }
        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
        }
        CloseUtils.close(mPs);
        CloseUtils.close(mSocket);

    }
}
