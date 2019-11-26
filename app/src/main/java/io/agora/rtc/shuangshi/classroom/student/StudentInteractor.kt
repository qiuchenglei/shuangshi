package io.agora.rtc.shuangshi.classroom.student

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.lib.rtc.RtcConfig
import io.agora.rtc.lib.rtm.RtmManager
import io.agora.rtc.lib.util.LogUtil
import io.agora.rtc.shuangshi.AGApplication
import io.agora.rtc.shuangshi.classroom.*
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.rtm.*
import java.lang.Exception
import java.sql.Timestamp

class StudentInteractor {
    private lateinit var statusListener: ClassStatusListener

    private val rtcWorker = AGApplication.the().rtcWorker
    private val rtmManager = AGApplication.the().rtmManager
    private var mHandler: Handler? = null
    private var rtmChannel: RtmChannel? = null
    private val log = LogUtil("StudentInteractor")

    lateinit var myAttr: Member

    fun getTeacherAttr(): Member? {
        return if (teacherList.isEmpty()) {
            null
        } else {
            teacherList[0]
        }
    }

    val rtmChannelListener = object : RtmChannelListener {
        override fun onAttributesUpdated(attributes: MutableList<RtmChannelAttribute>?) {
            log.i("onAttributesUpdated:")
            if (attributes == null || attributes.isEmpty()) {
                resetList()
                return
            }

            val mapAttributes = mutableMapOf<String, String>()
            for (attribute in attributes) {
                if (!TextUtils.isEmpty(attribute.key) && !TextUtils.isEmpty(attribute.value))
                    mapAttributes[attribute.key] = attribute.value
            }

            val gson = Gson()
            mapAttributes.remove(KEY_OPERATION_INFO)
//            val operationInfoValue = mapAttributes.remove(KEY_OPERATION_INFO)
//            if (!TextUtils.isEmpty(operationInfoValue)) {
//                try {
//                    val operationUid = operationInfoValue!!.toInt()
//                } catch (e: JsonSyntaxException) {
//                }
//            }

            val timestampJson = mapAttributes.remove(KEY_TIME_STAMP_S)

            if (!TextUtils.isEmpty(timestampJson)) {
                try {
                    val timeStampS = timestampJson!!.toLong()
                    statusListener.onUpdateTimeStamp(timeStampS)
                } catch (e: Exception){
                }
            }

            if (mapAttributes.isEmpty() || TextUtils.isEmpty(mapAttributes[myAttr.uid.toString()])) {
                resetList()
                return
            }

            //移除已经离开而被删除的老师或学生
            val removeTeachers = teacherList.filter {
                mapAttributes[it.uid.toString()] == null
            }

            teacherList.removeAll(removeTeachers)
            teacherMap = mutableMapOf()
            teacherList.forEach {
                teacherMap[it.uid] = it
            }

            val removeStudents = studentList.filter {
                mapAttributes[it.uid.toString()] == null
            }

            var isRemove = removeStudents.isNotEmpty() || removeTeachers.isNotEmpty()
            var isAdd = false
            val changeList = mutableListOf<Member>()

            studentList.removeAll(removeStudents)
            studentMap = mutableMapOf()
            studentList.forEach { studentMap[it.uid] = it }

            for (strJson in mapAttributes.values) {
                val member: Member
                try {
                    member = gson.fromJson(strJson, Member::class.java)
                } catch (e: Exception) {
                    LogUtil.fileLog("onAtrributeUpdated error: " + strJson)
                    continue
                }
                if (member.class_role == Role.TEACHER.intValue()) {
                    val lastValue = teacherMap[member.uid]
                    if (lastValue == null) {
                        teacherList.add(member)
                        teacherMap[member.uid] = member
                        isAdd = true
                    } else {
                        if (lastValue.setData(member)) {
                            changeList.add(member)
                        }
                    }
                } else if (member.uid != myAttr.uid) {
                    val lastValue = studentMap[member.uid]
                    if (member.is_online) {
                        if (lastValue == null) {
                            studentList.add(member)
                            studentMap[member.uid] = member
                            isAdd = true
                        } else {
                            if (lastValue.setData(member)) {
                                changeList.add(member)
                            }
                        }
                    } else {
                        if (lastValue != null) {
                            studentList.remove(studentMap.remove(member.uid))
                            isRemove = true
                        }
                    }
                }
            }

            allMembers.clear()
            allMembers.addAll(teacherList)
            allMembers.addAll(studentList)
            if (isRemove || isAdd) {
                statusListener.onUpdateMembers()
            } else if (changeList.isNotEmpty()) {
                statusListener.onPartChange(changeList)
            }
        }

        override fun onMemberCountUpdated(p0: Int) {
        }

        override fun onMessageReceived(p0: RtmMessage?, p1: RtmChannelMember?) {
            statusListener.onMessageReceived(p0, p1)
        }

        override fun onMemberJoined(p0: RtmChannelMember?) {}
        override fun onMemberLeft(p0: RtmChannelMember?) {}

    }

    fun init(
        roomName: String,
        userName: String,
        userId: Int,
        statusListener: ClassStatusListener
    ) {
        this.statusListener = statusListener
        mHandler = Handler(Looper.getMainLooper())
        myAttr = Member(userId, userName, Role.STUDENT.intValue(), roomName)
        studentList.add(myAttr)
        allMembers.add(myAttr)

        rtcWorker.setRtcEventHandler(object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                statusListener.onJoinChannelSuccess(channel, uid, elapsed)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                statusListener.onUserJoined(uid, elapsed)
            }
        })

        rtmManager.registerListener(object : RtmManager.MyRtmClientListener() {
            override fun onMessageReceived(rtmMessage: RtmMessage?, peerId: String?) {
                if (rtmMessage == null)
                    return
                val message: P2PMessage = Gson().fromJson(rtmMessage.text, P2PMessage::class.java)

                when (message.cmd) {
                    P2PMessage.CMD_TEXT -> statusListener.onMessageReceived(rtmMessage, peerId)
                    P2PMessage.CMD_MUTE_AUDIO -> {
                        muteLocalAudio(true)
                        statusListener.onPartChange(mutableListOf(myAttr))
                    }
                    P2PMessage.CMD_MUTE_VIDEO -> {
                        muteLocalVideo(true)
                        statusListener.onPartChange(mutableListOf(myAttr))
                    }
                    P2PMessage.CMD_UN_MUTE_AUDIO -> {
                        muteLocalAudio(false)
                        statusListener.onPartChange(mutableListOf(myAttr))
                    }
                    P2PMessage.CMD_UN_MUTE_VIDEO -> {
                        muteLocalVideo(false)
                        statusListener.onPartChange(mutableListOf(myAttr))
                    }
                    P2PMessage.CMD_CALL -> {
                        statusListener.onTeacherCall()
                    }
                    P2PMessage.CMD_OFF_LINE -> {
                        onLine(false)
                    }
                }
            }
        })

        rtcConfig(rtcWorker.rtcEngine)


        rtmChannel = rtmManager.createChannel(myAttr.room_name, rtmChannelListener)
    }

    private fun addOrUpdateMyAttr() {
        log.i("addOrUpdateMyAttr")
        val rtmChannelAttributeMe = RtmChannelAttribute(
            myAttr.uid.toString(),
            Gson().toJson(myAttr),
            myAttr.uid.toString(),
            System.currentTimeMillis()
        )
        val rtmChannelAttributeUpdate = RtmChannelAttribute(
            KEY_OPERATION_INFO,
            myAttr.uid.toString(),
            myAttr.uid.toString(),
            System.currentTimeMillis()
        )
        val options = ChannelAttributeOptions(true)
        rtmManager.rtmClient.addOrUpdateChannelAttributes(
            myAttr.room_name,
            mutableListOf(rtmChannelAttributeMe, rtmChannelAttributeUpdate),
            options,
            object : ResultCallback<Void> {
                override fun onFailure(p0: ErrorInfo?) {
                    log.i("addOrUpdateMyAttr failed")
                    statusListener.onErrorInfo("addOrUpdateChannelAttributes failure.", p0)
                }

                override fun onSuccess(p0: Void?) {
                    log.i("addOrUpdateMyAttr success")
                }
            })
    }

    val teacherList = mutableListOf<Member>()
    private var teacherMap = mutableMapOf<Int, Member>()
    val studentList = mutableListOf<Member>()
    private var studentMap = mutableMapOf<Int, Member>()
    val allMembers = mutableListOf<Member>()

    fun joinChannel() {
        rtcWorker.runTask {
            val rtcEngine = rtcWorker.rtcEngine
            rtcEngine.muteLocalAudioStream(true)
            rtcEngine.joinChannel(null, myAttr.room_name, "", myAttr.uid)
            rtcWorker.setmRtcConfig(RtcConfig(myAttr.uid, myAttr.room_name))
        }

        myAttr.is_mute_audio = true
        addOrUpdateMyAttr()
        rtmManager.joinChannel(rtmChannel, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                log.i("join rtm channel success")
                statusListener.onJoinRTMChannelSuccess(p0)
            }

            override fun onFailure(p0: ErrorInfo?) {
                log.i("join rtm channel failed:" + p0.toString())
                statusListener.onErrorInfo("join channel failure.", p0)
                leaveChannel()
            }
        })
    }

    private fun resetList() {
        teacherList.clear()
        studentList.clear()
//        studentList.add(myAttr)
    }

    fun leaveChannel() {
        rtcWorker.runTask {
            rtcWorker.rtcEngine.stopPreview()
            rtcWorker.rtcEngine.leaveChannel()
            rtcWorker.setmRtcConfig(null)
        }

        deleteMyAttr()

        rtmManager.leaveChannel(rtmChannel)
        rtmManager.releaseChannel(rtmChannel)
        rtmChannel = null

        resetList()
    }

    private fun deleteMyAttr() {
        val rtmChannelAttributeUpdate = RtmChannelAttribute(
            KEY_OPERATION_INFO,
            myAttr.uid.toString(),
            myAttr.uid.toString(),
            System.currentTimeMillis()
        )
        val options = ChannelAttributeOptions(true)
        rtmManager.rtmClient.deleteChannelAttributesByKeys(
            myAttr.room_name,
            mutableListOf(myAttr.uid.toString()),
            options,
            object : ResultCallback<Void> {
                override fun onFailure(p0: ErrorInfo?) {
                    log.i("deleteChannelAttributesByKeys failed")
                    statusListener.onErrorInfo("deleteChannelAttributesByKeys failure.", p0)
                }

                override fun onSuccess(p0: Void?) {
                    log.i("deleteChannelAttributesByKeys success")
                }
            })
        rtmManager.rtmClient.addOrUpdateChannelAttributes(
            myAttr.room_name,
            mutableListOf(rtmChannelAttributeUpdate),
            options,
            object : ResultCallback<Void> {
                override fun onFailure(p0: ErrorInfo?) {
                    log.i("addOrUpdateChannelAttributes in delete failed")
                    statusListener.onErrorInfo("addOrUpdateChannelAttributes failure.", p0)
                }

                override fun onSuccess(p0: Void?) {
                    log.i("addOrUpdateChannelAttributes in delete success")
                }
            })
    }

    fun destroy() {
        leaveChannel()
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null
    }

    fun bindEngineVideo(surfaceView: SurfaceView?, userId: Int) {
        if (myAttr.uid == userId) {
            rtcWorker.rtcEngine.setupLocalVideo(VideoCanvas(surfaceView))
            rtcWorker.rtcEngine.startPreview()
        } else {
            rtcWorker.rtcEngine.setupRemoteVideo(
                VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, userId)
            )
        }
    }

    private fun muteLocalAudio(isMute: Boolean) {
        myAttr.is_mute_audio = isMute
        rtcWorker.rtcEngine.muteLocalAudioStream(isMute)
        addOrUpdateMyAttr()
    }

    private fun muteLocalVideo(isMute: Boolean) {
        myAttr.is_mute_video = isMute
        rtcWorker.rtcEngine.muteLocalVideoStream(isMute)
        addOrUpdateMyAttr()
    }

//    fun changeTeacherSpeaker() {
//        val attr = getTeacherAttr()
//        if (attr != null)
//            attr.is_mute_audio = !attr.is_mute_audio
//    }

//    fun changeSpeaker(bean: Member) {
//        bean.is_mute_audio = !bean.is_mute_audio
//        rtcWorker.rtcEngine.muteRemoteAudioStream(bean.uid, bean.is_mute_audio)
//    }

    fun onLine(isOnLine: Boolean) {
        myAttr.is_online = isOnLine
        muteLocalAudio(!isOnLine)
        statusListener.onPartChange(mutableListOf(myAttr))
    }

    fun switchLocalAudio(bean: Member) {
        bean.is_mute_audio = !bean.is_mute_audio
        if (bean.uid == myAttr.uid)
            muteLocalAudio(bean.is_mute_audio)
    }

    fun switchLocalVideo(bean: Member) {
        bean.is_mute_video = !bean.is_mute_video
        if (bean.uid == myAttr.uid)
            muteLocalVideo(bean.is_mute_video)
    }

    fun getRtcEngine(): RtcEngine {
        return rtcWorker.rtcEngine
    }

    fun changeRtcConfig() {
        changeConfigInChannel(rtcWorker.rtcEngine)
    }

    fun acceptCall(isAccept: Boolean) {
        val teacherAttr = getTeacherAttr()
        if (isAccept) {
            if (teacherAttr != null) {
                sendP2PMsg(teacherAttr.uid, Gson().toJson(P2PMessage.CMD_ACCEPT_CALL))
            }
            onLine(true)
        } else {
            if (teacherAttr != null) {
                sendP2PMsg(teacherAttr.uid, Gson().toJson(P2PMessage.CMD_REFUSE_CALL))
            }
        }
    }

    fun sendP2PMsg(uid: Int, text: String) {
        rtmManager.sendP2PMsg(uid.toString(), text, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
            }

            override fun onFailure(p0: ErrorInfo?) {
                statusListener.onErrorInfo("send p2p message failed.", p0)
            }
        })
    }

    abstract class ClassStatusListener : IRtcEngineEventHandler() {
        abstract fun onUpdateMembers()
        abstract fun onJoinRTMChannelSuccess(p0: Void?)
        abstract fun onMessageReceived(p0: RtmMessage?, p1: RtmChannelMember?)
        abstract fun onMessageReceived(rtmManager: RtmMessage?, peerId: String?)
        abstract fun onErrorInfo(errorLog: String, errorInfo: ErrorInfo?)
        abstract fun onTeacherCall()
        abstract fun onPartChange(changeList: MutableList<Member>)
        abstract fun onUpdateTimeStamp(timestampS: Long)
    }
}