package com.zhengsr.socketlib.nio.entrance;

import com.zhengsr.socketlib.Constants;
import com.zhengsr.socketlib.nio.callback.BaseListener;
import com.zhengsr.socketlib.nio.callback.TcpClientListener;
import com.zhengsr.socketlib.nio.callback.TcpServerListener;
import com.zhengsr.socketlib.nio.entrance.server.NioServer;

/**
 * @author by  zhengshaorui on 2019/9/23
 * Describe:
 */
public class NioRequest {
    private int mPort = -1;
    private String mIp;
    private NioServer mNioServer;
    private NioClient mNioClient;
    private BaseListener mListener;
    public NioRequest port(int port){
        mPort = port;
        return this;
    }


    public NioRequest ip(String ip){
        mIp = ip;
        return this;
    }

    public NioRequest listener(BaseListener listener){
        mListener = listener;
        return this;
    }


    public NioRequest startServer(){
        if (mNioServer == null){
            mNioServer = new NioServer();
        }
        if (mPort == -1){
            mPort = Constants.TCP_PORT;
        }
        mNioServer.listener((TcpServerListener) mListener)
                .start(mPort);
        return this;
    }
    public NioRequest startClient(){
        if (mNioClient == null){
            mNioClient = new NioClient();
        }
        if (mPort == -1){
            mPort = Constants.TCP_PORT;
        }
        mNioClient.listener((TcpClientListener) mListener)
                .start(mIp,mPort);
        return this;
    }


    public void stopServer(){
        if (mNioServer != null) {
            mNioServer.stop();
        }
        mNioServer = null;
    }
    public void stopClient(){
        if (mNioClient != null) {
            mNioClient.stop();
        }
        mNioClient = null;
    }

    public void sendClientMsg(String msg) {
        if (mNioClient != null) {
            mNioClient.sendMsg(msg);
        }
    }

    public void sendServerBroMsg(String msg) {
        if (mNioServer != null) {
            mNioServer.sendBroMsg(msg);
        }
    }
}
