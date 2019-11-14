package io.agora.rtc.shuangshi.constant

enum class Role(private val value: Int) {
    TEACHER(0),
    STUDENT(1),
    TEACHER_SHARE(2);

    fun intValue() = value

    fun strValue() = value.toString()
}
