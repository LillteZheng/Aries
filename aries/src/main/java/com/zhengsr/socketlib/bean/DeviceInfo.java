package com.zhengsr.socketlib.bean;

/**
 * @author by  zhengshaorui on 2019/9/19
 * Describe: 设备名称
 */
public class DeviceInfo {
    public String ip;
    public int port;
    public String info;

    public DeviceInfo() {
    }

    public DeviceInfo(String ip, int port, String data) {
        this.ip = ip;
        this.port = port;
        this.info = data;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", info='" + info + '\'' +
                '}';
    }
}
