package com.zhengsr.socketlib.nio.core.packet;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe:
 *  任何数据都可以是byte数据,
 *  所以这里只需要知道数据有多少byte即可
 */
public abstract class SendPacket extends Packet {
    public abstract byte[] bytes();
}
