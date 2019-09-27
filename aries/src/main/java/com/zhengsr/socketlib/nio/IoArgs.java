package com.zhengsr.socketlib.nio;

import com.zhengsr.socketlib.Aries;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author by  zhengshaorui on 2019/9/26
 * Describe:基于封装的 bytebuffer
 */
public class IoArgs {
    private int limit = 256;
    ByteBuffer buffer = ByteBuffer.allocate(256);

    /**
     * 从数组读数据到 bytebuffer
     * @param offset 数组的位移，因为可能数据很大，而bytebuffer容量有限，需要分几次读取完成
     */
    public int readFrom(byte[] bytes,int offset){
        //首先确定 buffer 能读取的大小
        /**
         *  buffer.remaining() 表示 limit - position 之间的大小，读写模式不同
         *  byte.length = 10, buffer.allocate = 5,所以 buffer.remaining() = 5 ;
         *  假如从数组中读取了 4个字节到buffer之后，remaining = 1
         *  下次能读取的，应该只有 1 个字节，所以，用 math.min(10-4,1)
         *  后面如果要继续写入buffer，则得先 buffer.clear 然后才能继续写入
         */
        int size = Math.min(bytes.length - offset,buffer.remaining());
        buffer.put(bytes,offset,size);
        return size;
    }

    /**
     * 把 buffer 的数据，写到 byte去
     * @param offset
     * @return
     */
    public int writeTo(byte[] bytes,int offset){
        /**
         *  buffer.remaining() 表示 limit - position 之间的大小，读写模式不同
         *  byte.length = 5, 假如 buffer 已经有 10个数据;
         *  当数据从 buffer 读取 4 个字节的数据后，remaining  = 6，
         *  下次数组还能写入到byte的数据应该为 math.min(5-4,6)`
         */
        int size = Math.min(bytes.length - offset,buffer.remaining());
        buffer.get(bytes,offset,size);
        return size;
    }

    /**
     * 从channel中，读数据到buffer
     */
    public int readFrom(SocketChannel channel) throws IOException {
        //在读数据到 buffer 之前，应该先清空buffer，和设置容量区间
        startWriting();
        int length = 0;
        // hasRemaining  = limit > position ，这里其实也可以不用 while，
        // 因为buffer为上次写的数据，所以 channel.read(buffer)之后，position = limit
        while (buffer.hasRemaining()){
            int len = channel.read(buffer);
            if (len < 0){
                throw new EOFException();
            }
            length += len;
        }
        finishWriting();
        return length;
    }

    /**
     * 写数据到 channel
     */
    public int writeTo(SocketChannel channel) throws IOException {

        int length = 0;
        while (buffer.hasRemaining()){
            int len = channel.read(buffer);
            if (len < 0){
                throw new EOFException();
            }
            length += len;
        }
        return length;
    }

    public void startWriting(){
        buffer.clear();
        buffer.limit(limit);
    }

    public void finishWriting(){
        //切换到读模式
        buffer.flip();
    }

    public void limit(int limit){
        this.limit = limit;
    }

    public void writeLength(int header) {
        //写入头部信息
        buffer.putInt(header);
    }
    public int readLength(){
        //读头部的数据信息
        return buffer.getInt();
    }


    /**
     * IoArgs 提供者，消费者，数据产生和消费者
     */
    public interface IoArgsEventProcessor{
        //ioargs提供者
        /*IoArgs providerIoArgs();

        void onConsumeFailed(IoArgs args);
        *//**
         * 消费成功
         *//*
        void onConsumeCompleted(IoArgs args);*/
        void onStart(IoArgs args);
        void onCompleted(IoArgs args);
    }
}
