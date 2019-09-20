package com.zhengsr.socketlib.udp.callback;

import com.zhengsr.socketlib.bean.DeviceInfo;

import java.util.List;

/**
 * @author by  zhengshaorui on 2019/9/19
 * Describe: 用来找到搜索到的设备
 */
public interface DeviceListener {
    void findDevice(List<DeviceInfo> deviceInfos);
}
