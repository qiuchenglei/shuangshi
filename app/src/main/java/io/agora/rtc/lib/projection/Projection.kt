package io.agora.rtc.lib.projection

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast

class Projection {

    private var myPresentation: MyPresentation? = null

    private var projectionConfig: ProjectionConfig? = null
        get() = field
    private var isProjection = false
        get() = field


    public fun startProjection(context: Context, projectionConfig: ProjectionConfig): Boolean {
        if (isProjection) {
            cancelProjection()
        }

        val displayManager =
            context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
        val arrayOfDisplay =
            displayManager?.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        if (arrayOfDisplay == null || arrayOfDisplay.isEmpty()) {
            Toast.makeText(context, "不支持分屏", Toast.LENGTH_SHORT).show()
            isProjection = false
        } else {
            showPresentation(context, arrayOfDisplay[0], projectionConfig.surfaceView)//取第一个分屏使用
            this.projectionConfig = projectionConfig
            isProjection = true
        }

        return isProjection
    }

    /**
     * 主屏back键/home键隐藏后，副屏仍可使用。但是，再次打开主屏，副屏会失联，所以作如下设置
     *
     * @param display
     */
    private fun showPresentation(context: Context, display: Display, surfaceView: SurfaceView) {
        myPresentation = MyPresentation(
            context.getApplicationContext(),
            display
        )

        myPresentation?.addVideoView(surfaceView)
        myPresentation?.getWindow()
            ?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)//TYPE_SYSTEM_ALERT / TYPE_PHONE
        myPresentation?.show()
    }

    public fun cancelProjection() {
        this.isProjection = false
        myPresentation?.dismiss()
        myPresentation = null
        projectionConfig = null
    }

    class ProjectionConfig(
        var uid: Int = 0,
        var surfaceView: SurfaceView
    )
}