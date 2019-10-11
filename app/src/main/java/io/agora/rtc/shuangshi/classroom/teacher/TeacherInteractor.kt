package io.agora.rtc.shuangshi.classroom.teacher

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.SurfaceView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.mediaio.AgoraDefaultSource
import io.agora.rtc.shuangshi.AGApplication
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.lib.rtc.RtcConfig
import io.agora.rtc.lib.rtm.RtmManager
import io.agora.rtc.shuangshi.classroom.*
import io.agora.rtc.video.VideoCanvas
import io.agora.rtm.*

class TeacherInteractor {
    private lateinit var statusListener: ClassStatusListener

    private val rtcWorker = AGApplication.the().rtcWorker
    private val rtmManager = AGApplication.the().rtmManager
    private var mHandler: Handler? = null
    private var rtmChannel: RtmChannel? = null

    private lateinit var myAttr: Member

    public fun getTeacherAttr(): Member {
        return myAttr
    }

    public fun getMyAttr(): Member {
        return myAttr
    }

    fun init(
        roomName: String,
        userName: String,
        userId: Int,
        statusListener: ClassStatusListener
    ) {
        this.statusListener = statusListener
        mHandler = Handler(Looper.getMainLooper())
        myAttr = Member(userId, userName, Role.TEACHER.intValue(), roomName)

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
                    P2PMessage.CMD_ACCEPT_CALL -> {
                    }
                    P2PMessage.CMD_REFUSE_CALL -> {
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

    val studentList = mutableListOf<Member>()
    private var studentMap = mutableMapOf<Int, Member>()
    val allMembers = mutableListOf<Member>()

    private fun resetList() {
        studentList.clear()
    }

    fun joinChannel() {
        rtcWorker.runTask {
            val rtcEngine = rtcWorker.rtcEngine

            rtcEngine.joinChannel(null, myAttr.room_name, "", myAttr.uid)
            rtcWorker.setmRtcConfig(RtcConfig(myAttr.uid, myAttr.room_name))
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
                    // 第一次得到数据不包含自己，等待有自己的数据后再渲染出来
                    resetList()
                    return
                }

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
                    if (member.class_role == Role.STUDENT.intValue()) {
                        putMember(studentMap, studentList, member)
                    }
                }
                allMembers.clear()
                allMembers.add(getTeacherAttr())
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

    fun leaveChannel() {
        rtcWorker.runTask {
            rtcWorker.rtcEngine.stopPreview()
            rtcWorker.rtcEngine.leaveChannel()
            rtcWorker.setmRtcConfig(null)
        }

        if (rtmChannel != null)
            rtmManager.leaveChannel(rtmChannel)
    }

    fun destroy() {
        leaveChannel()
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null
    }

    fun setLocalSurfaceView(teacherViewMax: SurfaceView, isPreview: Boolean) {
        rtcWorker.runTask {
            rtcWorker.rtcEngine.apply {
                setupLocalVideo(VideoCanvas(teacherViewMax))
                if (isPreview) {
                    startPreview()
                }
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

    fun switchLocalAudio(bean: Member) {
        bean.mute_local_audio = !bean.mute_local_audio
        if (myAttr.uid == bean.uid) {
            rtcWorker.rtcEngine.muteLocalAudioStream(bean.mute_local_audio)
            addOrUpdateMyAttr()
        } else {
            sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_MUTE_AUDIO)))
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

    fun switchLocalVideo(bean: Member) {
        bean.mute_local_video = !bean.mute_local_video
        if (myAttr.uid == bean.uid) {
            rtcWorker.rtcEngine.muteLocalVideoStream(bean.mute_local_video)
            addOrUpdateMyAttr()
        } else {
            sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_MUTE_VIDEO)))
        }
    }

    fun switchTeacherShare(): Boolean {
        val member = getTeacherAttr()
        member.is_sharing = !member.is_sharing

        return member.is_sharing
    }

    fun switchTeacherLocalAudio(): Boolean {
        val member = getTeacherAttr()
        member.mute_local_audio = !member.mute_local_audio
        rtcWorker.rtcEngine.muteLocalAudioStream(member.mute_local_audio)
        return member.mute_local_audio
    }

    fun switchTeacherLocalVideo(): Boolean {
        val member = getTeacherAttr()
        member.mute_local_video = !member.mute_local_video
        rtcWorker.rtcEngine.muteLocalVideoStream(member.mute_local_video)
        return member.mute_local_video
    }

    fun onCallStudent(bean: Member) {
        when (bean.online_state) {
            0, 6 -> {
                bean.online_state = 4
                sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_CALL)))
            }
            5 -> {
                bean.online_state = 0
                sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_OFF_LINE)))
            }
        }
    }

    fun startRtcSDKSource() {
        rtcWorker.rtcEngine.setVideoSource(AgoraDefaultSource())
        rtcWorker.rtcEngine.startPreview()
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
    }
}
