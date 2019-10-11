package io.agora.rtc.lib.util

import java.util.*


fun stringForTime(timeS: Long, formatStrHMS: String, formatStrMS: String? = null): String {
    val seconds = timeS % 60
    val minutes = timeS / 60 % 60
    val hours = timeS / 3600

    return if (formatStrMS == null || hours > 0) {
        Formatter(StringBuffer(), Locale.getDefault())
            .format(formatStrHMS, hours, minutes, seconds).toString()
    } else {
        Formatter(StringBuffer(), Locale.getDefault())
            .format(formatStrMS, minutes, seconds).toString()
    }
}