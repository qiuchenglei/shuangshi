package io.agora.rtc.lib.util;

import android.util.Log;

public class LogUtil {
    private static final String tagPre = "Shuang_shi_";

    private String tag;

    public LogUtil(String tag) {
        this.tag = tagPre + tag;
    }

    public void d(String msg) {
        Log.d(tag, msg);
    }

    public void i(String msg) {
        Log.i(tag, msg);
    }

    public void w(String msg) {
        Log.w(tag, msg);
    }

    public void e(String msg) {
        Log.e(tag, msg);
    }
}
