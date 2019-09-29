package com.zhengsr.socketlib.nio.core.callback;

import com.zhengsr.socketlib.nio.IoArgs;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe: 接受者上层，传递 Ioargs
 */
public interface Receiver  extends Closeable{
    void setProcessorListener(IoArgs.IoArgsEventProcessor processor);
    void receiveAsync(IoArgs args) throws IOException;
}
