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
    public static void setProvider(IProvider provider){
        mIProvider = provider;
    }

    public static IProvider getProvider() throws IOException {
        return mIProvider;
    }
}
