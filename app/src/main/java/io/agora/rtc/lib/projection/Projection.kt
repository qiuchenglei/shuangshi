package io.agora.rtc.lib.projection

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.net.Uri
import android.view.Display
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import android.os.Build
import android.provider.Settings
import android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT


class Projection {

    private var myPresentation: MyPresentation? = null

    private var projectionConfig: ProjectionConfig? = null
        get() = field
    private var isProjection = false
        get() = field

    private fun checkPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context)
        }
        return true
    }

    fun startProjection(context: Context, projectionConfig: ProjectionConfig): Boolean {
        if (isProjection) {
            cancelProjection()
        }

        if (!checkPermission(context)) {
            Toast.makeText(context, "未获取到权限，请在设置中授权", Toast.LENGTH_SHORT).show()
            return false
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
/*

        val layout_parms: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        } else {

            layout_parms = WindowManager.LayoutParams.TYPE_PHONE

        }

        val yourparams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layout_parms,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
*/

        myPresentation?.setSubView(surfaceView)
        myPresentation?.getWindow()
            ?.setType(TYPE_SYSTEM_ALERT)//TYPE_SYSTEM_ALERT / TYPE_PHONE
        myPresentation?.show()
    }

    fun cancelProjection() {
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