package com.zhengsr.socketlib.nio.core.selector;

import com.zhengsr.socketlib.nio.IoArgs;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * @author by  zhengshaorui on 2019/9/24
 * Describe: 是一个观察者，用来注册Socketchannel的输入和输出，并把要实现的
 * 放在 Runnbale 的 run 方法中，供外部实现
 */
public interface IProvider extends Closeable{
    /**
     * 注册输入输出
     * @param channel
     * @return
     */
    boolean registerInput(SocketChannel channel,HandleInputRunnable runnable);
    boolean registerOutput(SocketChannel channel,HandleOutputRunnable runnable);

    boolean unRegisterInput(SocketChannel channel);
    boolean unRegisterOutput(SocketChannel channel);


    abstract class HandleInputRunnable implements Runnable{
        //具体操作放在线程
        @Override
        public final void run() {
            canProviderInput();
        }
        public abstract void canProviderInput();
    }

    /**
     * 写逻辑
     */
    abstract class HandleOutputRunnable implements Runnable{
        //用来关联数据，比如 string
        Object attach;
        @Override
        public final void run() {
            //具体操作放在线程
            canProviderOutput();
        }
        public void setAttach(Object attach){
            this.attach = attach;
        }

        public Object getAttach() {
            return attach;
        }

        public abstract void canProviderOutput();
    }
}
