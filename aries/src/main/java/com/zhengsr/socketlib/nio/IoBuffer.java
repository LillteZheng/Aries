package com.zhengsr.socketlib.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author by  zhengshaorui on 2019/9/26
 * Describe:基于封装的 bytebuffer
 */
public class IoBuffer {

    ByteBuffer buffer = ByteBuffer.allocate(5);

    /**
     * 从channel中，读数据到buffer
     */
    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    public int write(SocketChannel channel,String msg) throws IOException {

        buffer.clear();
        buffer.put(msg.getBytes());
        //切换到读模式，这样才能拿到数据
        buffer.flip();
        //直到读取完毕为止
        while (buffer.hasRemaining()) {
            //接着把数据写到 channel 去
            return channel.write(buffer);
        }
        return -1;
    }
    public String string(){
        return new String(buffer.array(),0,buffer.position());
    }
}
