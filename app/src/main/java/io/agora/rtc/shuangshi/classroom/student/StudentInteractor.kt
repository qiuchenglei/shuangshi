package io.agora.rtc.shuangshi.classroom.student

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.SurfaceView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.shuangshi.AGApplication
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.lib.rtc.RtcConfig
import io.agora.rtc.lib.rtm.RtmManager
import io.agora.rtc.shuangshi.classroom.*
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.rtm.*

class StudentInteractor {
    private lateinit var statusListener: ClassStatusListener

    private val rtcWorker = AGApplication.the().rtcWorker
    private val rtmManager = AGApplication.the().rtmManager
    private var mHandler: Handler? = null
    private var rtmChannel: RtmChannel? = null

    lateinit var myAttr: Member

    public fun getTeacherAttr(): Member? {
        return if (teacherList.isEmpty()) {
            null
        } else {
            teacherList[0]
        }
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

        rtcWorker.setRtcEventHandler(object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                statusListener.onJoinChannelSuccess(channel, uid, elapsed)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                statusListener.onUserJoined(uid, elapsed)
            }
        })

        rtmManager.registerListener(object : RtmManager.MyRtmClientListener {
            override fun onLoginStatusChanged(loginStatus: Int) {
            }

            override fun onTokenExpired() {
            }

            override fun onConnectionStateChanged(p0: Int, p1: Int) {
            }

            override fun onMessageReceived(rtmMessage: RtmMessage?, peerId: String?) {
                if (rtmMessage == null)
                    return
                val message: P2PMessage = Gson().fromJson(rtmMessage.text, P2PMessage::class.java)

                when (message.cmd) {
                    P2PMessage.CMD_TEXT -> statusListener.onMessageReceived(rtmMessage, peerId)
                    P2PMessage.CMD_MUTE_AUDIO -> {
                        muteLocalAudio(true)
                        statusListener.onUpdateMembers()
                    }
                    P2PMessage.CMD_MUTE_VIDEO -> {
                        muteLocalVideo(true)
                        statusListener.onUpdateMembers()
                    }
                    P2PMessage.CMD_UN_MUTE_AUDIO -> {
                        muteLocalAudio(false)
                        statusListener.onUpdateMembers()
                    }
                    P2PMessage.CMD_UN_MUTE_VIDEO -> {
                        muteLocalVideo(false)
                        statusListener.onUpdateMembers()
                    }
                    P2PMessage.CMD_CALL -> {
                        statusListener.onTeacherCall()
                    }
                }
            }
        })

        rtcConfig(rtcWorker.rtcEngine)
    }

    private fun addOrUpdateMyAttr() {
        val rtmChannelAttributeMe = RtmChannelAttribute(
            myAttr.uid.toString(),
            myAttr.user_name,
            myAttr.uid.toString(),
            System.currentTimeMillis()
        )
        val rtmChannelAttributeUpdate = RtmChannelAttribute(
            KEY_OPERATION_INFO,
            Gson().toJson(OperationInfo(myAttr.uid)),
            myAttr.uid.toString(),
            System.currentTimeMillis()
        )
        val options = ChannelAttributeOptions(true)
        rtmManager.rtmClient.addOrUpdateChannelAttributes(
            myAttr.uid.toString(),
            mutableListOf(rtmChannelAttributeMe, rtmChannelAttributeUpdate),
            options,
            object : ResultCallback<Void> {
                override fun onFailure(p0: ErrorInfo?) {
                    statusListener.onErrorInfo("addOrUpdateChannelAttributes failure.", p0)
                }

                override fun onSuccess(p0: Void?) {
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
            rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

            rtcEngine.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x480,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE
                )
            )

//                mRtcEngine.setParameters("{\"rtc.force_unified_communication_mode\":true}");//uc模式

            rtcEngine.joinChannel(null, myAttr.room_name, "", myAttr.uid)
            rtcWorker.setmRtcConfig(RtcConfig(myAttr.uid, myAttr.room_name))

            addOrUpdateMyAttr()
        }

        rtmChannel = rtmManager.createAndJoinChannel(myAttr.room_name, object : RtmChannelListener {
            override fun onAttributesUpdated(attributes: MutableList<RtmChannelAttribute>?) {
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
                val operationInfoJson = mapAttributes.remove(KEY_OPERATION_INFO)
                if (!TextUtils.isEmpty(operationInfoJson)) {
                    try {
                        val operationInfo: OperationInfo =
                            gson.fromJson(operationInfoJson, OperationInfo::class.java)
                    } catch (e: JsonSyntaxException) {
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
                teacherList.forEach { teacherMap[it.uid] = it }

                val removeStudents = studentList.filter {
                    mapAttributes[it.uid.toString()] == null
                }

                studentList.removeAll(removeStudents)
                studentMap = mutableMapOf()
                studentList.forEach { studentMap[it.uid] = it }

                for (strJson in mapAttributes.values) {
                    val member: Member
                    try {
                        member = gson.fromJson(strJson, Member::class.java)
                    } catch (e: JsonSyntaxException) {
                        break
                    }
                    if (member.class_role == Role.TEACHER.intValue()) {
                        putMember(teacherMap, teacherList, member)
                    } else {
                        putMember(studentMap, studentList, member)
                    }
                }
                allMembers.clear()
                allMembers.addAll(teacherList)
                allMembers.addAll(studentList)
                statusListener.onUpdateMembers()
            }

            override fun onMemberCountUpdated(p0: Int) {
            }

            override fun onMessageReceived(p0: RtmMessage?, p1: RtmChannelMember?) {
                statusListener.onMessageReceived(p0, p1)
            }

            override fun onMemberJoined(p0: RtmChannelMember?) {}
            override fun onMemberLeft(p0: RtmChannelMember?) {}

        }, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                addOrUpdateMyAttr()
                statusListener.onJoinRTMChannelSuccess(p0)
            }

            override fun onFailure(p0: ErrorInfo?) {
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

        if (rtmChannel != null)
            rtmManager.leaveChannel(rtmChannel)

        resetList()

        rtmManager.logout()
    }

    fun destroy() {
        leaveChannel()
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null
    }

    fun setLocalSurfaceView(teacherViewMax: SurfaceView, isPreview: Boolean) {
        rtcWorker.runTask {
            rtcWorker.rtcEngine.setupLocalVideo(VideoCanvas(teacherViewMax))
            if (isPreview) {
                rtcWorker.rtcEngine.startPreview()
            }
        }
    }

    fun bindEngineVideo(surfaceView: SurfaceView?, userId: Int) {
        if (myAttr.uid == userId) {
            rtcWorker.runTask { rtcWorker.rtcEngine.setupLocalVideo(VideoCanvas(surfaceView)) }
        } else {
            rtcWorker.runTask {
                rtcWorker.rtcEngine.setupRemoteVideo(
                    VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, userId)
                )
            }
        }
    }

    private fun muteLocalAudio(isMute: Boolean) {
        myAttr.mute_local_audio = isMute
        rtcWorker.rtcEngine.muteLocalAudioStream(isMute)
        addOrUpdateMyAttr()
    }

    private fun muteLocalVideo(isMute: Boolean) {
        myAttr.mute_local_video = isMute
        rtcWorker.rtcEngine.muteLocalVideoStream(isMute)
        addOrUpdateMyAttr()
    }

    fun changeTeacherSpeaker() {
        val attr = getTeacherAttr()
        if (attr != null)
            attr.mute_remote_audio = !attr.mute_remote_audio
    }

    fun changeSpeaker(bean: Member) {
        bean.mute_remote_audio = !bean.mute_remote_audio
        rtcWorker.rtcEngine.muteRemoteAudioStream(bean.uid, bean.mute_remote_audio)
    }

    fun onLine(isOnLine: Boolean) {
        myAttr.is_online = isOnLine
        addOrUpdateMyAttr()
    }

    fun switchLocalAudio(bean: Member) {
        bean.mute_local_audio = !bean.mute_local_audio
        if (bean.uid == myAttr.uid)
            muteLocalAudio(bean.mute_local_audio)
    }

    fun switchLocalVideo(bean: Member) {
        bean.mute_local_video = !bean.mute_local_video
        if (bean.uid == myAttr.uid)
            muteLocalVideo(bean.mute_local_audio)
    }

    fun getRtcEngine(): RtcEngine {
        return rtcWorker.rtcEngine
    }

    fun changeRtcConfig() {
        changeConfigInChannel(rtcWorker.rtcEngine)
    }

    abstract class ClassStatusListener : IRtcEngineEventHandler() {
        abstract fun onUpdateMembers()
        abstract fun onJoinRTMChannelSuccess(p0: Void?)
        abstract fun onMessageReceived(p0: RtmMessage?, p1: RtmChannelMember?)
        abstract fun onMessageReceived(rtmManager: RtmMessage?, peerId: String?)
        abstract fun onErrorInfo(errorLog: String, errorInfo: ErrorInfo?)
        abstract fun onTeacherCall()
    }
}