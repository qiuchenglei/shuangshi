package io.agora.rtc.lib.projection

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.SurfaceView
import android.widget.FrameLayout
import io.agora.rtc.shuangshi.R

/**
 * act和Presentation之间可以设置监听，进行互相通讯
 */
class MyPresentation(internal var context: Context, display: Display) : Presentation(context, display) {
    lateinit var layoutVideo: FrameLayout

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_presentation)
        layoutVideo = findViewById(R.id.layout_video)
    }

    fun addVideoView(surfaceView: SurfaceView?) {
        layoutVideo.removeAllViews()
        layoutVideo.addView(surfaceView)
    }

    override fun dismiss() {
        layoutVideo.removeAllViews()
        super.dismiss()
    }
}
