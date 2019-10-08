package com.zhengsr.socketlib.nio;

import com.zhengsr.socketlib.Aries;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author by  zhengshaorui on 2019/9/26
 * Describe:基于封装的 bytebuffer
 */
public class IoArgs {
    private int limit = 5;
    ByteBuffer buffer = ByteBuffer.allocate(5);


    /**
     * 从channel中，读数据到buffer
     */
    public int readFrom(ReadableByteChannel channel) throws IOException {
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
    public int writeTo(WritableByteChannel channel) throws IOException {

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
        startWriting();
        //写入头部信息
        buffer.putInt(header);
        finishWriting();
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
