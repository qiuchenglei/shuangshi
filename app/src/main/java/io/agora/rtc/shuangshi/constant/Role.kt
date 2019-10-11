package io.agora.rtc.shuangshi.constant

enum class Role(private val value: Int) {
    TEACHER(1),
    STUDENT(2);

    fun intValue() = value

    fun strValue() = value.toString()
}
