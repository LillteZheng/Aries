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
    private int limit = 5;
    ByteBuffer buffer = ByteBuffer.allocate(5);

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
     * @return
     */
    public int writeTo(byte[] bytes){
        /**
         *  buffer.remaining() 表示 limit - position 之间的大小，读写模式不同
         *  而这里的写，比较特殊，因为buffer每次的 limit 都是确定的。
         *  比如有 byte 有 8 个数据，buffer 的大小只有 5，那么第一次需要从byte读取
         *  5个字节，第二次则需要读 3 个字节，而每次 limit 都被我们限定的，所以这里
         *  并不需要偏移量来识别
         */
        int size = Math.min(bytes.length ,buffer.remaining());
        buffer.get(bytes,0,size);
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
            int len = channel.write(buffer);
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

    public int capacity() {
        return buffer.capacity();
    }


    /**
     * IoArgs 提供者，消费者，数据产生和消费者
     */
    public interface IoArgsEventProcessor{
        //ioargs提供者
        IoArgs providerIoArgs();

        void onConsumeCompleted(IoArgs args);
    }
}
