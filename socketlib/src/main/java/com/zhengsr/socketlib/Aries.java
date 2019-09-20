package com.zhengsr.socketlib;

import android.os.Handler;
import android.os.Looper;

import com.zhengsr.socketlib.udp.UdpRequest;

/**
 * @author by  zhengshaorui on 2019/9/19
 * Describe: 白羊座是一个对 socket 和 NIO 封装的工具类，
 *          你只需要简单的配置即可使用
 */
public class Aries {
    public static Handler HANDLER = new Handler(Looper.getMainLooper());
    private static UdpRequest mUdpRequest;
    public static UdpRequest port(int port){
        mUdpRequest = new UdpRequest().port(port);
        return mUdpRequest;
    }


    public static void stopProvider(){
        if (mUdpRequest != null) {
            mUdpRequest.stopProvider();
        }
    }
    public static void stopSearcher(){
        if (mUdpRequest != null) {
            mUdpRequest.stopSearch();
        }
    }
}
