package io.agora.rtc.shuangshi.setting

import io.agora.rtc.lib.util.SPUtil
import io.agora.rtc.shuangshi.constant.SPKey

class AudioSettingPresenter(var audioSettingView: AudioFragment?) {
    private var isHighQualityChecked = false
    private var isStereoChecked = false
    private var valuePlayVolume = 100

    fun onCreate() {
        isHighQualityChecked = SPUtil.get(SPKey.AUDIO_HIGH_QUALITY, false)
        isStereoChecked = SPUtil.get(SPKey.AUDIO_STEREO, false)
        valuePlayVolume = SPUtil.get(SPKey.AUDIO_SPEAKER_PHONE_VOLUME, 100)
        audioSettingView?.updateHighQualityUI(isHighQualityChecked)
        audioSettingView?.updateStereoUI(isStereoChecked)
        audioSettingView?.updatePlayVolumeUI(valuePlayVolume)
    }

    fun onClickHighQuality() {
        isHighQualityChecked = !isHighQualityChecked
        audioSettingView?.updateHighQualityUI(isHighQualityChecked)
    }

    fun onClickStereo() {
        isStereoChecked = !isStereoChecked
        audioSettingView?.updateStereoUI(isStereoChecked)
    }

    fun onSpeakerphoneProgressChanged(progress: Int, fromUser: Boolean) {
        valuePlayVolume = progress
    }

    fun apply() {
        SPUtil.put(SPKey.AUDIO_HIGH_QUALITY, isHighQualityChecked)
        SPUtil.put(SPKey.AUDIO_STEREO, isStereoChecked)
        SPUtil.put(SPKey.AUDIO_SPEAKER_PHONE_VOLUME, valuePlayVolume)
    }

    fun onDestroy() {
        audioSettingView = null
    }
}