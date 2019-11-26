package io.agora.rtc.shuangshi;

import android.app.Application;
import android.os.Environment;

import java.io.File;

import io.agora.rtc.lib.rtc.RtcWorkerThread;
import io.agora.rtc.lib.rtm.RtmManager;
import io.agora.rtc.lib.util.LogUtil;
import io.agora.rtc.lib.util.SPUtil;


public class AGApplication extends Application {

    private static AGApplication instance;
    private RtmManager rtmManager;
    private RtcWorkerThread rtcWorker;

    public static AGApplication the() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initSpUtil();
        initLogUtil();
    }

    private void initLogUtil() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File logDir = getExternalFilesDir("log");
            if (logDir != null) {
                String logFilePath = logDir.getAbsolutePath() + File.separator + "log.txt";
                LogUtil.initFileLog(logFilePath);
            }
        }
    }

    public void initRtmManager() {
        rtmManager = RtmManager.createInstance(this, getString(R.string.agora_app_id));
    }

    public synchronized void initWorkerThread() {
        if (rtcWorker == null) {
            rtcWorker = new RtcWorkerThread("Agora", this);
            rtcWorker.start();

            rtcWorker.waitForReady();
        }
    }

    public void initSpUtil() {
        SPUtil.init(this);
    }

    public synchronized void deInitWorkerThread() {
        rtcWorker.exit();
        try {
            rtcWorker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rtcWorker = null;
    }

    public RtcWorkerThread getRtcWorker() {
        return rtcWorker;
    }

    public RtmManager getRtmManager() {
        return rtmManager;
    }
}
