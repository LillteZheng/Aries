package com.zhengsr.socketlib.udp;

import com.zhengsr.socketlib.Constants;
import com.zhengsr.socketlib.udp.callback.DeviceListener;
import com.zhengsr.socketlib.udp.consume.UdpProvider;
import com.zhengsr.socketlib.udp.consume.UdpSearcher;

/**
 * @author by  zhengshaorui on 2019/9/19
 * Describe:
 */
public class UdpRequest {
    private int mPort = -1;
    private int mTimeout;
    private DeviceListener mListener;
    private UdpSearcher mSearcher;
    private UdpProvider mProvider;



    public UdpRequest port(int port){
        mPort = port;
        return this;
    }
    public UdpRequest timeout(int timeout){
        mTimeout = timeout;
        return this;
    }
    public UdpRequest listener(DeviceListener listener){
        mListener = listener;
        return this;
    }

    public void searchDevice(){
        if (mSearcher == null) {
            mSearcher = new UdpSearcher();
        }
        if (mPort == -1){
            mPort = Constants.PORT_BROADCAST;
        }
        mSearcher.startSearch(mPort,mTimeout,mListener);
    }

    public void stopSearch(){
        if (mSearcher != null) {
            mSearcher.stop();
        }
        mSearcher = null;
    }

    public void startProvider(){
        if (mProvider == null) {
            mProvider = new UdpProvider();
        }
        if (mPort == -1){
            mPort = Constants.PORT_BROADCAST;
        }
        mProvider.start(mPort);
    }

    public void stopProvider(){
        if (mProvider != null) {
            mProvider.stop();
        }
        mProvider = null;
    }
}
