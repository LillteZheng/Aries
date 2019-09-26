package com.zhengsr.socketlib.nio.core.selector;

import android.content.Context;
import android.util.AttributeSet;

import com.zhengsr.socketlib.nio.core.selector.IProvider;
import com.zhengsr.socketlib.nio.core.selector.IoProviderSelector;

import java.io.IOException;

/**
 * @author by  zhengshaorui on 2019/9/24
 * Describe: selector 的单例
 */
public class IoSelector {
    private static  IProvider mIProvider;
    public static IProvider getProvider() throws IOException {
        if (mIProvider == null){
            mIProvider = new IoProviderSelector();
        }
        return mIProvider;
    }
}
