package com.zhengsr.socketlib;

import android.util.Log;

/**
 * @author by  zhengshaorui on 2019/9/23
 * Describe: 信息统一管理工具类
 */
public class Lgg {
    private static final boolean DEBUG = true;

    public static void d(String msg){
        if (DEBUG){
            Log.d("Aries", "zsr -> "+msg);
        }
    }
}
