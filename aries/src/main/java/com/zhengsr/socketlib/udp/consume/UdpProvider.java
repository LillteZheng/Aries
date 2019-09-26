package com.zhengsr.socketlib.udp.consume;

import android.os.Build;

import com.zhengsr.socketlib.utils.CloseUtils;
import com.zhengsr.socketlib.Constants;
import com.zhengsr.socketlib.udp.callback.AbsUdp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by  zhengshaorui on 2019/9/19
 * Describe: udp 提供者，监听某个端口，若有广播，则返回数据
 */
public class UdpProvider extends AbsUdp{
    private static final String TAG = "UdpProvider";
    private HandleSearcher mProvider;
    private ExecutorService mExecutorService;

    public void start(int port){
        mExecutorService = Executors.newSingleThreadExecutor() ;
        //监听广播端口
        if (port == -1){
            port = Constants.PORT_BROADCAST;
        }
        mProvider = new HandleSearcher(port);
        mExecutorService.execute(mProvider);
    }
    @Override
    public void stop() {
        if (mProvider != null){
            mProvider.exit();
        }
        if (mExecutorService != null){
            mExecutorService.shutdownNow();
        }
    }


    class HandleSearcher extends Thread{
        int port;
        DatagramSocket ds;
        boolean done;
        byte[] bytes = new byte[6];
        ByteBuffer receiver;
        public HandleSearcher(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                ds = new DatagramSocket(port);
                DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
                while (!done){
                    ds.receive(packet);
                    receiver = ByteBuffer.wrap(bytes);
                    byte cmd = receiver.get();
                    int port = receiver.getInt();

                    if (cmd == Constants.REQUEST && port > 0){
                        ByteBuffer responseBuffer = ByteBuffer.allocate(20);
                        responseBuffer.put(Constants.RESPONSE);
                        responseBuffer.put(Build.MODEL.getBytes());
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseBuffer.array(),
                                responseBuffer.position(),
                                packet.getAddress(),
                                port
                        );
                        ds.send(responsePacket);
                    }
                }
            } catch (Exception e) {
              //  e.printStackTrace();
            }finally {
                CloseUtils.close(ds);
            }
        }

        public void exit(){
            done = true;
            CloseUtils.close(ds);
        }
    }
}
