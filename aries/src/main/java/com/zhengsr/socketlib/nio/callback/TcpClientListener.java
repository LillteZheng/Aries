package com.zhengsr.socketlib.nio.callback;


import com.zhengsr.socketlib.bean.DeviceInfo;

public interface TcpClientListener extends BaseListener {
    void serverConnected(DeviceInfo info);
    void serverDisconnect(DeviceInfo info);
    void serverConnectFail(String msg);
}
