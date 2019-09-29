package com.zhengsr.socketlib.nio.core.callback;

import com.zhengsr.socketlib.nio.IoArgs;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe:发送者上层，传递 Ioargs
 */
public interface Sender extends Closeable{
    void sendAsync(IoArgs ioArgs, IoArgs.IoArgsEventProcessor processor) throws IOException;
}
