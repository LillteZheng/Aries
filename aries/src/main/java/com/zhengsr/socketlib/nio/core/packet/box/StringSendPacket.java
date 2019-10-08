package com.zhengsr.socketlib.nio.core.packet.box;

import com.zhengsr.socketlib.nio.core.packet.SendPacket;

import java.io.ByteArrayInputStream;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe: 字符串包则是以 msg 为主
 */
public class StringSendPacket extends SendPacket<ByteArrayInputStream> {
    private final byte[] bytes;
    public StringSendPacket(String msg){
        bytes = msg.getBytes();
        length = bytes.length;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }
}
