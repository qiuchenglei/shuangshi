package io.agora.rtc.shuangshi.classroom

import android.content.Context
import android.view.SurfaceView
import io.agora.rtc.mediaio.AgoraSurfaceView
import io.agora.rtc.video.ViEEGLSurfaceRenderer


fun createLocalVideoView(context: Context): SurfaceView {
    return AgoraSurfaceView(context)
}

fun createRemoteVideoView(context: Context): SurfaceView {
    return ViEEGLSurfaceRenderer(context.applicationContext)
}
