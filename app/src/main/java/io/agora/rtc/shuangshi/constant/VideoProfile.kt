package io.agora.rtc.shuangshi.constant

import io.agora.rtc.video.VideoEncoderConfiguration

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
