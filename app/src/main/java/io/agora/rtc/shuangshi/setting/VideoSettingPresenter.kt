package io.agora.rtc.shuangshi.setting

import android.content.Context
import io.agora.rtc.shuangshi.R
import io.agora.rtc.lib.util.SPUtil
import io.agora.rtc.shuangshi.constant.SPKey
import io.agora.rtc.shuangshi.constant.VideoProfile

class VideoSettingPresenter(var mView: VideoFragment?) {
    var context: Context? = null

    lateinit var fpsArray: Array<String>
    lateinit var bitrateArray: Array<String>
    lateinit var codecArray: Array<String>
    lateinit var dimensionsArray: Array<String>

    var fpsSelectedIndex: Int = 0
    var bitrateSelectedIndex: Int = 0
    var codecSelectedIndex: Int = 0
    var dimensionsSelectedIndex: Int = 0

    fun onCreate() {
        context = mView?.activity

        fpsArray = context!!.resources.getStringArray(R.array.fps)
        bitrateArray = context!!.resources.getStringArray(R.array.bitrate)
        codecArray = context!!.resources.getStringArray(R.array.codec)
        dimensionsArray = context!!.resources.getStringArray(R.array.dimensions)

        fpsSelectedIndex = SPUtil.get(SPKey.VIDEO_FPS, VideoProfile.FPS_DEFAULT_INDEX) // default 15fps
        bitrateSelectedIndex = SPUtil.get(SPKey.VIDEO_BITRATE, VideoProfile.BITRATE_DEFAULT_INDEX) // default standard
        codecSelectedIndex = SPUtil.get(SPKey.VIDEO_CODEC, VideoProfile.CODEC_DEFAULT_ENCODE) // default hardware
        dimensionsSelectedIndex = SPUtil.get(SPKey.VIDEO_DIMENSIONS, VideoProfile.VD_DEFAULT_INDEX) // default 480p
    }

    fun onDestroy() {
        mView = null
        context = null
    }

    fun apply() {
        SPUtil.put(SPKey.VIDEO_FPS, fpsSelectedIndex)
        SPUtil.put(SPKey.VIDEO_BITRATE, bitrateSelectedIndex)
        SPUtil.put(SPKey.VIDEO_CODEC, codecSelectedIndex)
        SPUtil.put(SPKey.VIDEO_DIMENSIONS, dimensionsSelectedIndex)
    }

    fun onClickInput() {
    }

    fun onClickFps() {
        mView?.showFpsItems(fpsArray, fpsSelectedIndex, object : OnSelectListener {
            override fun onSelect(index: Int) {
                mView?.updateFpsText(fpsArray[index])
                fpsSelectedIndex = index
            }
        })
    }

    fun onClickVideoBitrate() {
        mView?.showBitrateItems(bitrateArray, bitrateSelectedIndex, object : OnSelectListener {
            override fun onSelect(index: Int) {
                mView?.updateBitrateText(bitrateArray[index])
                bitrateSelectedIndex = index
            }
        })
    }

    fun onClickVideoCodec() {
        mView?.showCodecItems(codecArray, codecSelectedIndex, object : OnSelectListener {
            override fun onSelect(index: Int) {
                mView?.updateCodecText(codecArray[index])
                codecSelectedIndex = index
            }
        })
    }

    fun onClickVideoResolution() {
        mView?.showResolutionItems(
            dimensionsArray,
            dimensionsSelectedIndex,
            object : OnSelectListener {
                override fun onSelect(index: Int) {
                    mView?.updateResolutionText(dimensionsArray[index])
                    dimensionsSelectedIndex = index
                }
            })
    }
}