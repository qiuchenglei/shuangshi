package io.agora.rtc.lib.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.agora.rtc.shuangshi.BuildConfig;

public class LogUtil {
    private static final String tagPre = "Shuang_shi_";

    private String tag;

    public LogUtil(String tag) {
        this.tag = tagPre + tag;
    }

    public void d(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public void i(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public void w(String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg);
        }
    }

    public void e(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

    private static String mFileName;
    private static LogUtil logUtilFile;
    public static void initFileLog(String fileName) {
        mFileName = fileName;

        File file = new File(mFileName);
        if (!file.exists()) {
            if (file.getParent() == null) {
                file.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void fileLog(String s) {
        if (TextUtils.isEmpty(mFileName) || TextUtils.isEmpty(s)){
            return;
        }
        if (logUtilFile == null) {
            logUtilFile = new LogUtil("file");
        }
        logUtilFile.e("s");
        FileWriter fw = null;
        try {
            fw = new FileWriter(mFileName, true);
            fw.append(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
