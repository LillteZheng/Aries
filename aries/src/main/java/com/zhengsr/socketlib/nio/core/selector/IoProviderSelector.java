package com.zhengsr.socketlib.nio.core.selector;

import com.zhengsr.socketlib.CloseUtils;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author by  zhengshaorui on 2019/9/24
 * Describe:
 */
public class IoProviderSelector implements IProvider {

    /**
     * 一些状态,涉及到一些多线程的状态量
     */
    private AtomicBoolean mIsClosed = new AtomicBoolean(false);
    /**
     * 这里在  selectionkey 的注册添加，需要等待，避免多线程并发问题
     */
    private AtomicBoolean mInputLocker = new AtomicBoolean(false);
    private AtomicBoolean mOutputLocker = new AtomicBoolean(false);

    private final Selector mReadSelector;
    private final Selector mWriterSelector;
    private final ExecutorService mInputPool ;
    private final ExecutorService mOutputPool ;
    private final HashMap<SelectionKey,Runnable> mInputMap;
    private final HashMap<SelectionKey,Runnable> mOutputMap;

    public IoProviderSelector() throws IOException {
        mReadSelector = Selector.open();
        mWriterSelector = Selector.open();
        mInputPool = Executors.newFixedThreadPool(4);
        mOutputPool = Executors.newFixedThreadPool(4);
        mInputMap = new LinkedHashMap<>();
        mOutputMap = new LinkedHashMap<>();
        //开始读
        startRead();
        startWrite();
    }



    /**
     * 开始监听和读取
     */
    private void startRead() {
        //开个线程用来拿到 selectionkey
        Thread readthread = new Thread("read selector thread"){
            @Override
            public void run() {
                super.run();
                while (!mIsClosed.get()){
                    try {
                        if (mReadSelector.select() == 0){
                            waitSelection(mInputLocker);
                            continue;
                        }
                        /**
                         * 为了发挥多线程的优势，避免客户端一个个读取数据，这里采用 mina 的做法
                         */
                        Set<SelectionKey> selectionKeys = mReadSelector.selectedKeys();

                        for (SelectionKey key : selectionKeys) {
                            handleSelection(key,SelectionKey.OP_READ,mInputMap,mInputPool);
                        }
                        selectionKeys.clear();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        readthread.setPriority(Thread.MAX_PRIORITY);
        readthread.start();
    }
    /**
     * 开始监听和写数据
     */
    private void startWrite() {
        //开个线程用来拿到 selectionkey
        Thread readthread = new Thread("write selector thread"){
            @Override
            public void run() {
                super.run();
                while (!mIsClosed.get()){
                    try {
                        if (mWriterSelector.select() == 0){
                            waitSelection(mOutputLocker);
                            continue;
                        }
                        /**
                         * 为了发挥多线程的优势，避免客户端一个个读取数据，这里采用 mina 的做法
                         */
                        Set<SelectionKey> selectionKeys = mWriterSelector.selectedKeys();

                        for (SelectionKey key : selectionKeys) {
                            handleSelection(key,SelectionKey.OP_WRITE,mOutputMap,mOutputPool);
                        }
                        selectionKeys.clear();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        readthread.setPriority(Thread.MAX_PRIORITY);
        readthread.start();
    }


    /**
     * 等待是否注册成功
     * @param locker
     */
    private static void waitSelection(AtomicBoolean locker){
        synchronized (locker){
            if (locker.get()){
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean registerInput(SocketChannel channel, HandleInputRunnable runnable) {
        registerSelection(channel,mInputLocker,mReadSelector,SelectionKey.OP_READ,mInputMap,runnable);
        return false;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandleOutputRunnable runnable) {
        registerSelection(channel,mOutputLocker,mWriterSelector,SelectionKey.OP_WRITE,mOutputMap,runnable);
        return false;
    }

    @Override
    public boolean unRegisterInput(SocketChannel channel) {
        unRegisterSelection(channel,mReadSelector,mInputMap);
        return false;
    }

    @Override
    public boolean unRegisterOutput(SocketChannel channel) {
        unRegisterSelection(channel,mWriterSelector,mOutputMap);
        return false;
    }


    /**
     * 注册selectionkey
     */
    private void registerSelection(SocketChannel channel,AtomicBoolean locker,
                                   Selector selector,int keyOps,Map<SelectionKey,Runnable> map,
                                   Runnable runnable){
        /**
         * 注册的时候，把当前对应的 selectionkey，和 runnable 结合起来
         * 但是这里是多线程并发的，所以要考虑好并发的问题
         * 假设这里正在注册 key 或添加的时候，而 mReadSelector.select() 又有新的数据过来，
         * 这里就需要它等待我们这边注册完成，即某个状态，可以使用 atomicboolean 来表示
         */
        synchronized (locker){
            //表示我正在注册，如果有其他selectinkey，应该等待我注册完成
            locker.set(true);

            try {
                //先唤醒
                selector.wakeup();
                SelectionKey key = null;
                if (channel.isRegistered()){
                    key = channel.keyFor(selector);
                    if (key != null){
                        //Selector以前被channel注册过，重新注册即可
                        key.interestOps(key.interestOps() | keyOps);
                    }
                }
                //还未注册channel
                if (key == null){
                    key = channel.register(selector,keyOps);
                    map.put(key,runnable);
                }
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }finally {
                //表示注册完成
                locker.set(false);
            }
        }
    }


    private void unRegisterSelection(SocketChannel channel,Selector selector,
                                     Map<SelectionKey,Runnable> map){
        if (channel.isRegistered()){
            SelectionKey key = channel.keyFor(selector);
            if (key != null){
                /**
                 * 也可以用 key.interestOps(key.interestOps() & ~keyOps);
                 * 只是这里读写分离，所以用 key.cancel()也可以，如果是同个selector，
                 * 则需要区分一下读写
                 */
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }
    }
    /**
     * 处理selectionkey
     */
    private void handleSelection(SelectionKey key, int keyOps, Map<SelectionKey,Runnable> map,
                                 ExecutorService pool){
        /**
         * key.interestOps() 为感兴趣事件
         * 假如 interestOps 已经有读写事件了，即 OP_READ = 0X00000001 和 OP_WRITE = 0X00000100，
         * 那它们相加即为 0x00000101 ，如果要取消关注的事件，比如取消读事件，运算如下：
         * 0x00000101 & ~ 0x00000001 -> 0x00000101 & 0x11111110 = 0x00000100 就只剩下写事件了
         */
        if (key.isValid()) {
            //先去掉对 OP_READ 的监听，防止下一次进来干扰
            key.interestOps(key.interestOps() & ~keyOps);

            //拿到这个 key 对应的 runnable 了，直接执行
            Runnable runnable = map.get(key);
            if (runnable != null && !pool.isShutdown()){
                pool.execute(runnable);
            }
        }

    }

    @Override
    public void close() throws IOException {
        if (mIsClosed.compareAndSet(false,true)){
            mInputPool.shutdownNow();
            mInputMap.clear();
            mReadSelector.wakeup();

            CloseUtils.close(mReadSelector);
        }
    }
}
