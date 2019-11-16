package io.agora.rtc.shuangshi.classroom

import io.agora.rtc.shuangshi.constant.Role

data class Member(
    var uid: Int,
    var user_name: String,
    var class_role: Int = Role.STUDENT.intValue(),
    @Transient var room_name: String = "",
    var is_mute_audio: Boolean = false,
    var is_mute_video: Boolean = false,
    var is_online: Boolean = false,
    @Transient var online_state: Int = 0, // 针对学生的字段： 0:初始状态， 1： 学生举手， 2：老师接受举手， 3：老师拒绝举手， 4：老师点名， 5：学生接受点名 6：学生拒绝点名
    @Transient var is_sharing: Boolean = false, // 针对老师的字段，用于更新是否正在
    @Transient var mute_remote_audio: Boolean = true, // 关闭其他人的audio
    @Transient var is_projection: Boolean = false
) {
    fun setData(memberFromServer: Member): Boolean {
        var isChanged = false
        if (this.uid != memberFromServer.uid) {
            this.uid = memberFromServer.uid
            isChanged = true
        }
        if (!this.user_name.equals(memberFromServer.user_name)) {
            this.user_name = memberFromServer.user_name
            isChanged = true
        }
        if (this.class_role != memberFromServer.class_role) {
            this.class_role = memberFromServer.class_role
            isChanged = true
        }
        if (this.is_mute_audio != memberFromServer.is_mute_audio) {
            this.is_mute_audio = memberFromServer.is_mute_audio
            isChanged = true
        }
        if (this.is_mute_video != memberFromServer.is_mute_video) {
            this.is_mute_video = memberFromServer.is_mute_video
            isChanged = true
        }
        if (this.is_online != memberFromServer.is_online) {
            this.is_online = memberFromServer.is_online
            isChanged = true
        }
        return isChanged
    }
}

val KEY_OPERATION_INFO = "changed_uid"
data class OperationInfo(val changed_uid: Int)

val KEY_TIME_STAMP_S = "timestamp"
data class TimeStampS(val timeStampS: Long)

data class P2PMessage(val cmd: Int = CMD_TEXT, var text: String = "") {
    companion object {
        val CMD_TEXT = 100
        val CMD_MUTE_AUDIO = 101
        val CMD_UN_MUTE_AUDIO = 102
        val CMD_MUTE_VIDEO = 103
        val CMD_UN_MUTE_VIDEO = 104
        val CMD_CALL = 105
        val CMD_ACCEPT_CALL = 106
        val CMD_REFUSE_CALL = 107
        val CMD_OFF_LINE = 108
    }
}

fun putMember(
    map: MutableMap<Int, Member>,
    list: MutableList<Member>,
    value: Member,
    key: Int = value.uid
) {
    val lastValue = map[key]
    if (lastValue == null) {
        list.add(value)
        map[key] = value
    } else {
        lastValue.setData(value)
    }
}