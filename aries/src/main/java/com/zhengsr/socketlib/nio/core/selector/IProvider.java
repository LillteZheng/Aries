package com.zhengsr.socketlib.nio.core.selector;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * @author by  zhengshaorui on 2019/9/24
 * Describe: selector 的读写接口
 */
public interface IProvider extends Closeable{
    /**
     * 注册输入输出
     * @param channel
     * @return
     */
    boolean registerInput(SocketChannel channel,HandleInputRunnable runnable);

    boolean unRegisterInput(SocketChannel channel);


    abstract class HandleInputRunnable implements Runnable{
        //具体操作放在线程
        @Override
        public final void run() {
            canProviderInput();
        }
        public abstract void canProviderInput();
    }
}
