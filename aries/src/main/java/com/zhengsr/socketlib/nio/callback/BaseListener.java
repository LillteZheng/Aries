package com.zhengsr.socketlib.nio.callback;

import java.io.File;

public interface BaseListener {
    /**
     * 字符串的传输,这里不要转换成UI线程，因为有可能需要在后台传输
     */
    void onResponse(String msg);

}