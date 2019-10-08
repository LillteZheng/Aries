package com.zhengsr.socketlib.nio.core.packet;


import com.zhengsr.socketlib.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author by  zhengshaorui on 2019/9/26
 * Describe: 规定包的长度
 */
public abstract class Packet<T extends Closeable> implements Closeable{
    public long length;
    private T stream;


    /**
     * 创建流
     */
    public final T open(){
        stream = createStream();
        return stream;
    }

    @Override
    public final void close() throws IOException {
        if (stream != null) {
            closeStream(stream);
            CloseUtils.close(stream);
            stream = null;
        }
    }

    /**
     * 创建不同的流，需要子类去实现
     */
    protected abstract T createStream();

    /**
     * 关闭之前，可以对流进行一些操作，比如拿出数据
     */
    public void closeStream(T stream){}
}
