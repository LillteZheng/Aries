package com.zhengsr.socketlib.nio.callback;


import com.zhengsr.socketlib.bean.DeviceInfo;

public interface TcpServerListener extends BaseListener {
    void onClientCount(int count);
    void onClientConnected(DeviceInfo info);
    void onClientDisconnect(DeviceInfo info);
}
