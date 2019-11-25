package io.agora.rtc.shuangshi.setting


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

import io.agora.rtc.shuangshi.R

class VideoFragment : Fragment() {
    private val mVideoSettingPresenter: VideoSettingPresenter = VideoSettingPresenter(this)

    private lateinit var mTvVideoInputSelect: TextView
    private lateinit var mTvVideoFpsSelect: TextView
    private lateinit var mTvVideoBitrateSelect: TextView
    private lateinit var mTvVideoCodecSelect: TextView
    private lateinit var mTvVideoDimensionSelect: TextView
    private lateinit var mFlVideoLayout: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_video, container, false)
        mTvVideoInputSelect = root.findViewById<TextView>(R.id.tv_video_input_select)
        mTvVideoFpsSelect = root.findViewById<TextView>(R.id.tv_video_fps_select)
        mTvVideoBitrateSelect = root.findViewById<TextView>(R.id.tv_video_bitrate_select)
        mTvVideoCodecSelect = root.findViewById<TextView>(R.id.tv_video_codec_select)
        mTvVideoDimensionSelect = root.findViewById<TextView>(R.id.tv_video_resolution_select)
        mFlVideoLayout = root.findViewById(R.id.fl_video_layout)

        mTvVideoInputSelect.setOnClickListener { mVideoSettingPresenter.onClickInput() }
        mTvVideoFpsSelect.setOnClickListener { mVideoSettingPresenter.onClickFps() }
        mTvVideoBitrateSelect.setOnClickListener { mVideoSettingPresenter.onClickVideoBitrate() }
        mTvVideoCodecSelect.setOnClickListener { mVideoSettingPresenter.onClickVideoCodec() }
        mTvVideoDimensionSelect.setOnClickListener { mVideoSettingPresenter.onClickVideoResolution() }

        mVideoSettingPresenter.onCreate()
        return root
    }

    fun showFpsItems(
        fpsArray: Array<String>,
        selectedIndex: Int,
        listener: OnSelectListener
    ) {
        showSettingItems(fpsArray, selectedIndex, listener, mTvVideoFpsSelect)
    }

    fun showBitrateItems(
        bitrateArray: Array<String>,
        selectedIndex: Int,
        listener: OnSelectListener
    ) {
        showSettingItems(bitrateArray, selectedIndex, listener, mTvVideoBitrateSelect)
    }

    fun showCodecItems(
        codecArray: Array<String>,
        selectedIndex: Int,
        listener: OnSelectListener
    ) {
        showSettingItems(codecArray, selectedIndex, listener, mTvVideoCodecSelect)
    }

    fun showResolutionItems(
        resolutionArray: Array<String>,
        selectedIndex: Int,
        listener: OnSelectListener
    ) {
        showSettingItems(resolutionArray, selectedIndex, listener, mTvVideoDimensionSelect)
    }

    fun updateFpsText(s: String) {
        mTvVideoFpsSelect.text = s
    }

    fun updateDimensionText(s: String) {
        mTvVideoDimensionSelect.text = s
    }

    fun updateCodecText(s: String) {
        mTvVideoCodecSelect.text = s
    }

    fun updateBitrateText(s: String) {
        mTvVideoBitrateSelect.text = s
    }

    fun apply() {
        mVideoSettingPresenter.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mVideoSettingPresenter.onDestroy()
    }
}
