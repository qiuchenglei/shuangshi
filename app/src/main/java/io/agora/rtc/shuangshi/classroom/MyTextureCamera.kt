package io.agora.rtc.shuangshi.classroom

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.view.WindowManager
import io.agora.rtc.gl.RendererCommon
import io.agora.rtc.lib.util.LogUtil
import io.agora.rtc.mediaio.MediaIO
import io.agora.rtc.mediaio.TextureSource
import java.io.IOException


class MyTextureCamera(var context: Context, var width: Int = 1920, var height: Int = 1080) :
    TextureSource(null, width, height) {
    private val log:LogUtil = LogUtil("MyTextureCamera")
    private var camera: Camera? = null

    private var info: CameraInfo? = null

    override fun onTextureFrameAvailable(
        oesTextureId: Int,
        transformMatrix: FloatArray?,
        timestampNs: Long
    ) {
        super.onTextureFrameAvailable(oesTextureId, transformMatrix, timestampNs)
        var transformM = transformMatrix
        val rotation: Int = this.getFrameOrientation()
        if (info?.facing == CameraInfo.CAMERA_FACING_FRONT) {
            transformM = RendererCommon.multiplyMatrices(
                transformM,
                RendererCommon.horizontalFlipMatrix()
            )
        }
        mConsumer?.get()?.consumeTextureFrame(
            oesTextureId,
            MediaIO.PixelFormat.TEXTURE_OES.intValue(),
            mWidth,
            mHeight,
            rotation,
            System.currentTimeMillis(),
            transformM
        )
    }

    private fun getFrameOrientation(): Int {
        var rotation: Int = this.getDeviceOrientation()
        if (info!!.facing == 0) {
            rotation = 360 - rotation
        }
        return (info!!.orientation + rotation) % 360
    }

    private fun getDeviceOrientation(): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return when (wm.defaultDisplay.rotation) {
            0 -> 0
            1 -> 90
            2 -> 180
            3 -> 270
            else -> 0
        }
    }

    override fun onCapturerStarted(): Boolean {
        this.camera?.startPreview()
        return true
    }

    override fun onCapturerOpened(): Boolean {
        return try {
            openCamera()
            camera!!.setPreviewTexture(this.surfaceTexture)
            camera!!.startPreview()
            return true
        } catch (var2: IOException) {
            log.e("initialize: failed to initalize camera device")
            return false
        }
    }

    override fun onCapturerStopped() {
        camera?.stopPreview()
    }

    override fun onCapturerClosed() {
        releaseCamera()
    }

    private fun openCamera() {
        if (camera != null) {
            throw RuntimeException("camera already initialized")
        }

        info = CameraInfo()
        // Try to find a front-facing camera (e.g. for videoconferencing).
        // Try to find a front-facing camera (e.g. for videoconferencing).
        val numCameras = Camera.getNumberOfCameras()
        for (i in 0 until numCameras) {
            Camera.getCameraInfo(i, info)
            if (info!!.facing == CameraInfo.CAMERA_FACING_FRONT) {
                camera = Camera.open(i)
                break
            }
        }
        if (camera == null) {
            log.d("No front-facing camera found; opening default")
            camera = Camera.open() // opens first back-facing camera
        }
        if (camera == null) {
            throw RuntimeException("Unable to open camera")
        }

        val parms = camera!!.parameters
        val frameRates = parms.supportedPreviewFpsRange
        val minFps = frameRates[frameRates.size - 1][Camera.Parameters.PREVIEW_FPS_MIN_INDEX]
        val maxFps = frameRates[frameRates.size - 1][Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
        parms.setPreviewFpsRange(minFps, maxFps)

        // Calculate size.
        val listCameraSize: List<Camera.Size> = parms.getSupportedPreviewSizes()
        var largestSize: Camera.Size? = null
        var isNeedLargest = true

        run breaking@{
            listCameraSize.forEach {
                if (mWidth > 0 && it.width == mWidth && it.height == mHeight) {
                    isNeedLargest = false
                    return@breaking
                }
                if (largestSize == null || it.width > largestSize!!.width) {
                    largestSize = it
                }
            }
        }

        if (isNeedLargest && largestSize == null)
            return
        if (isNeedLargest) {
            mWidth = largestSize!!.width
            mHeight = largestSize!!.height
        }
        parms.setPreviewSize(mWidth, mHeight)
        parms.setRecordingHint(true)
        camera!!.parameters = parms

        val cameraPreviewSize = parms.previewSize
        val previewFacts =
            cameraPreviewSize.width.toString() + "x" + cameraPreviewSize.height
        log.i("Camera config: $previewFacts")
    }

    private fun releaseCamera() {
        if (camera != null) {
            camera?.stopPreview()
            try {
                camera?.setPreviewTexture(null as SurfaceTexture?)
            } catch (var2: Exception) {
                log.e("failed to set Preview Texture")
            }
            camera?.release()
            camera = null
            log.d("releaseCamera -- done")
        }
    }
}