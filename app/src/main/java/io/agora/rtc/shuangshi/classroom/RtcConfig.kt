package io.agora.rtc.shuangshi.classroom

import io.agora.rtc.Constants
import io.agora.rtc.RtcEngine
import io.agora.rtc.lib.util.SPUtil
import io.agora.rtc.shuangshi.constant.AudioProfile
import io.agora.rtc.shuangshi.constant.SPKey
import io.agora.rtc.shuangshi.constant.VideoProfile
import io.agora.rtc.video.VideoEncoderConfiguration

public fun rtcConfig(rtcEngine: RtcEngine) {
    val isHighQuality = SPUtil.get(SPKey.AUDIO_HIGH_QUALITY, AudioProfile.IS_HIGH_QUALITY_DEFAULT)
    val isStereo = SPUtil.get(SPKey.AUDIO_STEREO, AudioProfile.IS_STEREO_DEFAULT)
    val speakerVolume = SPUtil.get(SPKey.AUDIO_SPEAKER_PHONE_VOLUME, AudioProfile.SPEAKER_VOLUME_DEFAULT)

    val vdIndex = SPUtil.get(SPKey.VIDEO_DIMENSIONS, VideoProfile.VD_DEFAULT_INDEX)
    val fpsIndex = SPUtil.get(SPKey.VIDEO_FPS, VideoProfile.FPS_DEFAULT_INDEX)
    val bitrateIndex = SPUtil.get(SPKey.VIDEO_BITRATE, VideoProfile.BITRATE_DEFAULT_INDEX)
    val codecIndex = SPUtil.get(SPKey.VIDEO_CODEC, VideoProfile.CODEC_DEFAULT_ENCODE)

    rtcEngine.apply {
        enableAudio()
        enableVideo()
        setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        var profile = Constants.AUDIO_PROFILE_DEFAULT
        if  (isStereo && !isHighQuality) {
            profile = Constants.AUDIO_PROFILE_MUSIC_STANDARD_STEREO
        } else if (isHighQuality && !isStereo) {
            profile = Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY
        } else if (isHighQuality && isStereo) {
            profile = Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO
        }
        setAudioProfile(profile, Constants.AUDIO_SCENARIO_DEFAULT)

        adjustPlaybackSignalVolume(speakerVolume)

        setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoProfile.VD[vdIndex],
                VideoProfile.FPS[fpsIndex],
                VideoProfile.BITRATE[bitrateIndex],
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE
            )
        )
        setParameters("{\"che.hardware_encoding\":$codecIndex}")

//        setParameters("{\"rtc.force_unified_communication_mode\":true}");//uc模式
    }
}

public fun changeConfigInChannel(rtcEngine: RtcEngine) {
    // setAudioProfile() or che.hardware_encoding will not work in channel

    val speakerVolume = SPUtil.get(SPKey.AUDIO_SPEAKER_PHONE_VOLUME, AudioProfile.SPEAKER_VOLUME_DEFAULT)
    val vdIndex = SPUtil.get(SPKey.VIDEO_DIMENSIONS, VideoProfile.VD_DEFAULT_INDEX)
    val fpsIndex = SPUtil.get(SPKey.VIDEO_FPS, VideoProfile.FPS_DEFAULT_INDEX)
    val bitrateIndex = SPUtil.get(SPKey.VIDEO_BITRATE, VideoProfile.BITRATE_DEFAULT_INDEX)
    rtcEngine.apply {
        enableAudio()
        enableVideo()
        setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        adjustPlaybackSignalVolume(speakerVolume)

        setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoProfile.VD[vdIndex],
                VideoProfile.FPS[fpsIndex],
                VideoProfile.BITRATE[bitrateIndex],
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE
            )
        )
    }
}