package io.agora.rtc.shuangshi.classroom

import io.agora.rtc.gl.EglBase
import io.agora.rtc.gl.RendererCommon
import io.agora.rtc.mediaio.BaseVideoRenderer
import io.agora.rtc.mediaio.IVideoSink
import java.nio.ByteBuffer

class MyVideoRenderer : BaseVideoRenderer("MyVideoRenderer"), IVideoSink {


    private var mEglContext: EglBase.Context? = null
    private lateinit var mConfigAttributes:IntArray
    private var mDrawer: RendererCommon.GlDrawer? = null

    override fun init(sharedContext: EglBase.Context) {
        this.mEglContext = sharedContext
    }

    override fun init(
        sharedContext: EglBase.Context,
        configAttributes: IntArray,
        drawer: RendererCommon.GlDrawer) {
        this.mEglContext = sharedContext
        this.mConfigAttributes = configAttributes
        this.mDrawer = drawer
    }

    override fun onInitialize(): Boolean {
        if (mConfigAttributes != null && mDrawer != null) {
            super.init(mEglContext, mConfigAttributes, mDrawer)
        } else {
            super.init(mEglContext)
        }

        return true
    }

    override fun onStart(): Boolean {
        return super.start()
    }

    override fun onStop() {
        super.stop()
    }

    override fun onDispose() {
        super.release()
    }

    override fun consumeTextureFrame(
        textureId: Int,
        format: Int,
        width: Int,
        height: Int,
        rotation: Int,
        timestamp: Long,
        matrix: FloatArray?
    ) {
        consume(textureId, pixelFormat, width, height, rotation, timestamp, matrix)
    }
    override fun consumeByteBufferFrame(
        buffer: ByteBuffer?,
        format: Int,
        width: Int,
        height: Int,
        rotation: Int,
        timestamp: Long
    ) {
        consume(buffer, format, width, height, rotation, timestamp)
    }

    override fun consumeByteArrayFrame(
        data: ByteArray?,
        format: Int,
        width: Int,
        height: Int,
        rotation: Int,
        timestamp: Long
    ) {
        consume(data, pixelFormat, width, height, rotation, timestamp)
    }

}