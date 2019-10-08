package com.zhengsr.socketlib.nio.core.packet;

import java.io.InputStream;

/**
 * @author by  zhengshaorui on 2019/9/27
 * Describe: 需要把数据读出来，所以必须是 inputstream 类型
 */
public abstract class SendPacket<T extends InputStream> extends Packet<T> {

}
