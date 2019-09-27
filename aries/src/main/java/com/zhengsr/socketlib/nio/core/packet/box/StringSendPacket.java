package com.zhengsr.socketlib.nio.core.packet.box;

import com.zhengsr.socketlib.nio.core.packet.SendPacket;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe: 字符串包则是以 msg 为主
 */
public class StringSendPacket extends SendPacket {
    private final byte[] bytes;
    public StringSendPacket(String msg){
        bytes = msg.getBytes();
        length = bytes.length;
    }

    @Override
    public byte[] bytes() {
        return bytes;
    }
}
