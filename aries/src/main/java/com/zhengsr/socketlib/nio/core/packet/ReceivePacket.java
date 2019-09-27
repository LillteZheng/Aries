package com.zhengsr.socketlib.nio.core.packet;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe:
 *  当一个大数据过来，除了知道有多少byte
 *  还需要知道当前读到哪里了
 */
public abstract class ReceivePacket extends Packet {
    public abstract void save(byte[] bytes,int count);
}
