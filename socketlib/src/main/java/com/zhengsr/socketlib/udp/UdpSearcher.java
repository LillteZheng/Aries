package com.zhengsr.socketlib.udp;

import com.zhengsr.socketlib.Aries;
import com.zhengsr.socketlib.CloseUtils;
import com.zhengsr.socketlib.bean.DeviceInfo;
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
    protected ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private ResponseListener mResponseListener;
    public static UdpSearcher create(){
        return new UdpSearcher();
    }
    
    private UdpSearcher(){}


    /**
     * 开始搜索设备
     * @param timeout
     * @param listener
     */
    public void startSearch(final int timeout, final DeviceListener listener){
        //android需要在线程下，才能执行 socket 的操作
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    CountDownLatch downLatch = new CountDownLatch(1);
                    mResponseListener = listener(downLatch);
                    sendBroadcast();
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
    private ResponseListener listener(CountDownLatch receiveDownLatch) throws InterruptedException {
        /**
         * 受系统cpu和线程异步启动的影响，这压力需要线程启动完毕了，才能继续其他的操作
         */
        CountDownLatch startDownLatch = new CountDownLatch(1);
        ResponseListener listener = new ResponseListener(UDPConstants.PORT_CLIENT_RESPONSE, startDownLatch, receiveDownLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }

    /**
     * 监听服务端返回来的数据
     */
    class ResponseListener extends Thread{
        private int port;
        private DatagramSocket ds;
        private boolean done = false;
        private byte[] bytes = new byte[128];
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
                    int port = packet.getPort();
                    int length = packet.getLength();
                    buffer = ByteBuffer.wrap(bytes,0,length);
                    int cmd = buffer.getInt();
                    int tcpPort = buffer.getInt();
                    int position = buffer.position();
                    String name =  new String(bytes,position,length - position);
                    //Log.d(TAG, "zsr run: "+name);
                    if (UDPConstants.RESPONSE == cmd && tcpPort > 0){
                        DeviceInfo info = new DeviceInfo(ip,tcpPort,name);
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
    private void sendBroadcast() throws IOException {
        DatagramSocket ds = new DatagramSocket();
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(UDPConstants.REQUEST);
        buffer.putInt(UDPConstants.PORT_CLIENT_RESPONSE);
        DatagramPacket packet = new DatagramPacket(
                buffer.array(),
                buffer.position(),
                InetAddress.getByName(UDPConstants.BROADCAST_IP),
                UDPConstants.PORT_BROADCAST
        );
        ds.send(packet);
        ds.close();
    }

    @Override
    public void stop() {
        if (mExecutorService != null){
            mExecutorService.shutdownNow();
        }
    }
}
