package com.zhengsr.socketlib.nio.core.packet.box;

import com.zhengsr.socketlib.nio.core.packet.ReceivePacket;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe: 字符串的packet，只需要知道长度即可
 */
public class StringReceivePacket extends ReceivePacket {
    //记录当前读到哪里了
    int position;
    final byte[] buff;
    public StringReceivePacket(int len){
        buff = new byte[len];
        length = buff.length;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes,0,buff,position,count);
        position += count;
    }
}
