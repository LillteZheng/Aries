package com.zhengsr.socketlib.udp;

import android.os.Build;
import android.util.Log;

import com.zhengsr.socketlib.CloseUtils;
import com.zhengsr.socketlib.udp.callback.AbsUdp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

/**
 * @author by  zhengshaorui on 2019/9/19
 * Describe: udp 提供者，监听某个端口，若有广播，则返回数据
 */
public class UdpProvider extends AbsUdp{
    private static final String TAG = "UdpProvider";
    private HandleSearcher mProvider;

    public static UdpProvider create(){
        return new UdpProvider();
    }

    private UdpProvider(){

    }

    public void start(){
        //监听广播端口
        mProvider = new HandleSearcher(UDPConstants.PORT_BROADCAST);
        mProvider.start();
    }
    @Override
    public void stop() {
        if (mProvider != null){
            mProvider.exit();
        }
    }


    class HandleSearcher extends Thread{
        int port;
        DatagramSocket ds;
        boolean done;
        byte[] bytes = new byte[5];
        ByteBuffer receiver;
        public HandleSearcher(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                ds = new DatagramSocket();
                Log.d(TAG, "zsr 开始监听");
                DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
                while (!done){
                    ds.receive(packet);
                    receiver = ByteBuffer.wrap(bytes);
                    byte cmd = receiver.get();
                    int port = receiver.getInt();

                        Log.d(TAG, "zsr 收到数据: "+cmd+" "+port);
                    if (cmd == UDPConstants.REQUEST && port > 0){
                        ByteBuffer responseBuffer = ByteBuffer.allocate(128);
                        responseBuffer.put(UDPConstants.RESPONSE);
                        responseBuffer.putInt(1234);
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
                e.printStackTrace();
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
