package io.agora.rtc.shuangshi.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window

import io.agora.rtc.shuangshi.AGApplication
import io.agora.rtc.lib.rtc.RtcWorkerThread
import io.agora.rtc.lib.rtm.RtmManager


abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            AGApplication.the().initWorkerThread()
            AGApplication.the().initRtmManager()
        }
        val layout = findViewById<View>(Window.ID_ANDROID_CONTENT)
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                initData()
            }
        })
        initUI(savedInstanceState)
    }

    protected abstract fun initUI(savedInstanceState: Bundle?)

    protected open fun initData() {}

    protected fun rtmManager(): RtmManager {
        return AGApplication.the().rtmManager
    }

    protected fun rtcWorker(): RtcWorkerThread {
        return AGApplication.the().rtcWorker
    }
}
