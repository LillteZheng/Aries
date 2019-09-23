package com.zhengsr.socketlib;

/**
 * created by zhengshaorui on 2019/8/9
 * Describe: udp 参数常亮
 */
public class Constants {

    /**
     * UDP 广播端口
     */
    public static final int PORT_BROADCAST = 30023;
    /**
     * 提供者，回送消息的端口
     */
    public static final int PORT_CLIENT_RESPONSE = 30034;

    public static int  TCP_PORT = 30045;
    /**
     * udp 广播
     */
    public static final String BROADCAST_IP = "255.255.255.255";

    /**
     * 用来识别的命令
     */
    public static final byte HEADER = 7;
    public static final byte REQUEST = 0x01;
    public static final byte RESPONSE = 0x02;

}