package com.zhengsr.socketlib.udp;

import com.zhengsr.socketlib.udp.callback.DeviceListener;

/**
 * @author by  zhengshaorui on 2019/9/19
 * Describe:
 */
public class UdpRequest {
    private int mPort;
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
            mSearcher = UdpSearcher.create();
        }
        mSearcher.startSearch(mTimeout,mListener);
    }

    public void stopSearch(){
        if (mSearcher != null) {
            mSearcher.stop();
        }
    }

    public void startProvider(){
        if (mProvider == null) {
            mProvider = UdpProvider.create();
        }
        mProvider.start();
    }

    public void stopProvider(){
        if (mProvider != null) {
            mProvider.stop();
        }
    }
}
