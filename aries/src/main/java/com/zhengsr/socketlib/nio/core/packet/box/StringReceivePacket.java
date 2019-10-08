package com.zhengsr.socketlib.nio.core.packet.box;

import com.zhengsr.socketlib.nio.core.packet.ReceivePacket;

import java.io.ByteArrayOutputStream;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe: 字符串的packet，只需要知道长度即可
 */
public class StringReceivePacket extends ReceivePacket<ByteArrayOutputStream> {
    private String string;
    public StringReceivePacket(int len){
        length = len;
    }


    @Override
    public void closeStream(ByteArrayOutputStream stream) {
        super.closeStream(stream);
        string = new String(stream.toByteArray());
    }

    public String string() {
        return string;
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
