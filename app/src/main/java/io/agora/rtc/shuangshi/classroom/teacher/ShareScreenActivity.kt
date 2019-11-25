package io.agora.rtc.shuangshi.classroom.teacher

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.View

import io.agora.rtc.lib.shareScreen.ui.RecordService
import io.agora.rtc.lib.shareScreen.ui.SurfaceReadyListener
import io.agora.rtc.shuangshi.base.BaseActivity

abstract class ShareScreenActivity : BaseActivity(), SurfaceReadyListener {
    private var mediaProjection: MediaProjection? = null
    private var recordService: RecordService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val metrics = DisplayMetrics()
            getWindowManager().getDefaultDisplay().getMetrics(metrics)
            val binder = service as RecordService.RecordBinder
            recordService = binder.recordService
            recordService!!.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
            recordService!!.setSurfaceReadyListener(this@ShareScreenActivity)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            recordService = null
        }
    }

    override fun initData() {
        super.initData()
        val intent = Intent(this, RecordService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    interface StartShareCallback {
        fun onSuccess()
        fun onFailure()
    }

    private var callback: StartShareCallback? = null

    protected fun startShareScreen(callback: StartShareCallback? = null) {
        this.callback = callback
        recordService?.isEnableViewRecord = false
        val projectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, RECORD_REQUEST_CODE)
    }

    protected fun stopShareScreen() {
        recordService?.isEnableViewRecord = false
        recordService?.stopRecord()
    }

    protected fun stopShareView() {
        recordService?.isEnableViewRecord = false
        recordService?.stopRecord()
    }

    protected fun startShareView(shareView:View) {
        recordService?.setRecordView(shareView)
        recordService?.isEnableViewRecord = true
        recordService?.startRecord(rtcWorker().rtcEngine)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopShareScreen()
        unbindService(connection)
    }

    protected fun onShareScreenFailed() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val projectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
            recordService?.setMediaProject(mediaProjection)
            recordService?.startRecord(rtcWorker().rtcEngine)
            callback?.onSuccess()
        } else {
            onShareScreenFailed()
            callback?.onFailure()
        }
    }

    companion object {
        private val RECORD_REQUEST_CODE = 101
        private val TAG = "ShareScreenActivity"
    }
}
