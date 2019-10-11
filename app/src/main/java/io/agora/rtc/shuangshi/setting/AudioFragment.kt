package io.agora.rtc.shuangshi.setting


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener

import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.view.CheckableLinearLayout

class AudioFragment : Fragment() {

    private val audioSettingPresenter: AudioSettingPresenter = AudioSettingPresenter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_audio, container, false)
        initUI(root)
        return root
    }

    private lateinit var checkLayoutHighQuality: CheckableLinearLayout
    private lateinit var checkLayoutStereo: CheckableLinearLayout
    private lateinit var seekBarSpeakerphone: SeekBar
    private fun initUI(root: View) {
        checkLayoutHighQuality = root.findViewById(R.id.check_layout_high_quality)
        checkLayoutStereo = root.findViewById(R.id.check_layout_stereo)
        seekBarSpeakerphone = root.findViewById(R.id.seekbar_speakerphone)

        checkLayoutHighQuality.setOnClickListener { audioSettingPresenter.onClickHighQuality() }
        checkLayoutStereo.setOnClickListener { audioSettingPresenter.onClickStereo() }
        seekBarSpeakerphone.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioSettingPresenter.onSpeakerphoneProgressChanged(progress, fromUser)
            }
        })

        audioSettingPresenter.onCreate()
    }

    fun updateHighQualityUI(isChecked: Boolean) {
        checkLayoutHighQuality.isChecked = isChecked
    }

    fun updateStereoUI(isChecked: Boolean) {
        checkLayoutStereo.isChecked = isChecked
    }

    public fun apply() {
        audioSettingPresenter.apply()
    }

    fun updatePlayVolumeUI(valuePlayVolume: Int) {
        seekBarSpeakerphone.progress = valuePlayVolume
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioSettingPresenter.onDestroy()
    }
}
