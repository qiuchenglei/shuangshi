package io.agora.rtc.lib.projection

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import io.agora.rtc.shuangshi.R

/**
 * act和Presentation之间可以设置监听，进行互相通讯
 */
class MyPresentation(internal var context: Context, display: Display) :
    Presentation(context, display) {
    private var layoutVideo: FrameLayout? = null
    private var subView: View? = null

    fun setSubView(subView: View?) {
        this.subView = subView
        if (layoutVideo!=null) {
            layoutVideo?.removeAllViews()
            layoutVideo?.addView(subView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_presentation)
        layoutVideo = findViewById(R.id.layout_video)
        if (subView != null) {
            layoutVideo?.removeAllViews()
            layoutVideo?.addView(subView)
        }
    }

    override fun dismiss() {
        this.subView = null
        layoutVideo?.removeAllViews()
        layoutVideo = null
        super.dismiss()
    }
}
