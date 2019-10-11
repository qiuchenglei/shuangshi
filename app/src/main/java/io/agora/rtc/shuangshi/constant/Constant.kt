package io.agora.rtc.shuangshi.constant

import io.agora.rtc.video.VideoEncoderConfiguration

class SPKey {
    companion object {
        const val AUDIO_SPEAKER_PHONE_VOLUME = "audio_speakerphone_volume"
        const val AUDIO_HIGH_QUALITY = "audio_high_quality"
        const val AUDIO_STEREO = "audio_stereo"

        const val VIDEO_FPS = "video_fps"
        const val VIDEO_BITRATE = "video_bitrate"
        const val VIDEO_CODEC = "video_codec"
        const val VIDEO_DIMENSIONS = "video_dimensions"

        const val LAYOUT_VIEW_COUNT = "layout_view_count"
        const val LAYOUT_VIEW_MODE = "layout_view_mode"
    }
}

class IntentKey {
    companion object {
        const val INTENT_KEY_ROOM_NAME = "intent_key_room_name"
        const val INTENT_KEY_USER_NAME = "intent_key_user_name"
        const val INTENT_KEY_USER_ID = "intent_key_user_id"
    }
}

object VideoProfile {
    val VD_DEFAULT_INDEX = 2
    val VD = arrayOf<VideoEncoderConfiguration.VideoDimensions>(
        VideoEncoderConfiguration.VD_320x240,
        VideoEncoderConfiguration.VD_640x360,
        VideoEncoderConfiguration.VD_840x480,
        VideoEncoderConfiguration.VD_1280x720,
        VideoEncoderConfiguration.VideoDimensions(1920, 1080)
    )

    val FPS_DEFAULT_INDEX = 2
    val FPS = arrayOf<VideoEncoderConfiguration.FRAME_RATE>(
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_7,
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10,
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30
    )

    val BITRATE_DEFAULT_INDEX = 0
    val BITRATE = arrayOf(
        VideoEncoderConfiguration.STANDARD_BITRATE,
        VideoEncoderConfiguration.COMPATIBLE_BITRATE
    )

    val CODEC_DEFAULT_ENCODE = 1
    val CODEC = arrayOf(
        0, //software
        1  //hardware
    )
}

object AudioProfile{
    val IS_HIGH_QUALITY_DEFAULT = false
    val IS_STEREO_DEFAULT = false
    val SPEAKER_VOLUME_DEFAULT = 100
}
