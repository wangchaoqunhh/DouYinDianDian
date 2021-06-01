package com.wcq.douyindiandian.util;

import android.util.Log;

import static com.wcq.douyindiandian.BuildConfig.LOG;

public class Logging {
    public static void e(String tag, String msg) {
        if (LOG) {
            if (tag == null || tag.length() == 0 || msg == null || msg.length() == 0){
                return;
            }
            int segmentSize = 3 * 1024;
            int length = msg.length();
            if (length <= segmentSize) { // 长度小于等于限制直接打印
                Log.e(tag, msg);
            } else {
                while (msg.length() > segmentSize) { // 循环分段打印日志
                    String logContent = msg.substring(0, segmentSize);
                    msg = msg.replace(logContent, "");
                    Log.e(tag, logContent);
                }
                Log.e(tag, msg); // 打印剩余日志
            }
        }
    }
}
