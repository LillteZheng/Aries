package com.zhengsr.socketlib.udp.consume;

import android.util.Log;

import com.zhengsr.socketlib.Aries;
import com.zhengsr.socketlib.CloseUtils;
import com.zhengsr.socketlib.bean.DeviceInfo;
import com.zhengsr.socketlib.Constants;
import com.zhengsr.socketlib.udp.callback.AbsUdp;
import com.zhengsr.socketlib.udp.callback.DeviceListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author by  zhengshaorui on 2019/9/19
 * Describe: UDP 搜索，发送udp广播，并监听广播端口，如果有数据返回，则表示拿到服务端的信息
 */
public class UdpSearcher extends AbsUdp{
    private static final String TAG = "UdpSearcher";
    protected ExecutorService  mExecutorService ;
    private ResponseListener mResponseListener;
    private ResponseListener mResponseThread;


    public UdpSearcher(){
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 开始搜索设备
     * @param timeout
     * @param listener
     */
    public void startSearch(final int port,final int timeout, final DeviceListener listener){
        //android需要在线程下，才能执行 socket 的操作
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    CountDownLatch downLatch = new CountDownLatch(1);
                    mResponseListener = listener(downLatch,port);
                    sendBroadcast(port);
                    downLatch.await(timeout, TimeUnit.MILLISECONDS);
                    //切换到主线程
                    Aries.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.findDevice(mResponseListener.getDeviceInfos());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    /**
     * 启动线程配置
     * @param receiveDownLatch
     * @return
     * @throws InterruptedException
     */
    private ResponseListener listener(CountDownLatch receiveDownLatch,int responsePort) throws InterruptedException {
        /**
         * 受系统cpu和线程异步启动的影响，这压力需要线程启动完毕了，才能继续其他的操作
         */
        if (responsePort == -1){
            responsePort = Constants.PORT_CLIENT_RESPONSE;
        }else{
            responsePort += 1;
        }
        CountDownLatch startDownLatch = new CountDownLatch(1);
        mResponseThread = new ResponseListener(responsePort, startDownLatch, receiveDownLatch);
        mResponseThread.start();
        startDownLatch.await();
        return mResponseThread;
    }

    /**
     * 监听服务端返回来的数据
     */
    class ResponseListener extends Thread{
        private int port;
        private DatagramSocket ds;
        private boolean done = false;
        private byte[] bytes = new byte[20];
        private ByteBuffer buffer;
        private List<DeviceInfo> deviceInfos = new ArrayList<>();
        private CountDownLatch startDownLatch,receiveDownLatch;
        public ResponseListener(int port,CountDownLatch startDownLatch,CountDownLatch receiveDownLatch) {
            this.port = port;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            super.run();
            //通知线程已启动
            startDownLatch.countDown();
            try {
                ds = new DatagramSocket(port);
                DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
                deviceInfos.clear();
                while (!done){
                    ds.receive(packet);
                    String ip = packet.getAddress().getHostAddress();
                    //int port = packet.getPort();
                    int length = packet.getLength();
                    buffer = ByteBuffer.wrap(bytes,0,length);
                    int cmd = buffer.get();
                    int position = buffer.position();
                    String name =  new String(bytes,position,length - position);
                    if (Constants.RESPONSE == cmd){
                        DeviceInfo info = new DeviceInfo(ip,-1,name);
                        deviceInfos.add(info);
                    }
                    //成功接受到一份
                    receiveDownLatch.countDown();
                }
            }catch (Exception e){

            }finally {
                CloseUtils.close(ds);
            }
        }
        public void exit(){
            done = true;
            CloseUtils.close(ds);
        }

        public List<DeviceInfo> getDeviceInfos() {
            exit();
            return deviceInfos;
        }
    }

    /**
     * 发送广播
     * @throws IOException
     */
    private void sendBroadcast(int port) throws IOException {
        //创建 DatagramSocket 端口由系统指定
        DatagramSocket ds = new DatagramSocket();
        /**
         * 创建数据包
         */
        int responsePort = -1;
        if (port == -1){
            port =  Constants.PORT_BROADCAST;
            responsePort = Constants.PORT_CLIENT_RESPONSE;
        }else {
            responsePort = port+1;
        }
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put(Constants.REQUEST);
        buffer.putInt(responsePort);
        DatagramPacket packet = new DatagramPacket(
                buffer.array(),
                buffer.position(),
                InetAddress.getByName(Constants.BROADCAST_IP),
                port
        );
        ds.send(packet);
        ds.close();
    }

    @Override
    public void stop() {
        if (mExecutorService != null){
            mExecutorService.shutdownNow();
        }
        if (mResponseThread != null) {
            mResponseThread.exit();
        }
    }
}
