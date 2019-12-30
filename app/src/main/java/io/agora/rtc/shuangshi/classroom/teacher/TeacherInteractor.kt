package io.agora.rtc.shuangshi.classroom.teacher

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.SurfaceView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.lib.rtc.RtcConfig
import io.agora.rtc.lib.rtm.RtmManager
import io.agora.rtc.lib.util.LogUtil
import io.agora.rtc.mediaio.AgoraDefaultSource
import io.agora.rtc.shuangshi.AGApplication
import io.agora.rtc.shuangshi.classroom.*
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.video.VideoCanvas
import io.agora.rtm.*
import java.lang.Exception

class TeacherInteractor {
    private lateinit var statusListener: ClassStatusListener

    private val log = LogUtil("TeacherInteractor")

    private val rtcWorker = AGApplication.the().rtcWorker
    private val rtmManager = AGApplication.the().rtmManager
    private var mHandler: Handler? = null
    private var rtmChannel: RtmChannel? = null

    private lateinit var myAttr: Member

    fun getTeacherAttr(): Member {
        return myAttr
    }

    fun getMyAttr(): Member {
        return myAttr
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
                // 第一次得到数据不包含自己，等待有自己的数据后再渲染出来
                resetList()
                return
            }

            val removeStudents = studentList.filter {
                mapAttributes[it.uid.toString()] == null
            }

            val isRemove = removeStudents.isNotEmpty()
            var isAdd = false
            val changeList = mutableListOf<Member>()

            studentList.removeAll(removeStudents)
            studentMap = mutableMapOf()
            studentList.forEach {
                studentMap[it.uid] = it
            }

            for (strJson in mapAttributes.values) {
                val member: Member
                try {
                    member = gson.fromJson(strJson, Member::class.java)
                } catch (e: Exception) {
                    LogUtil.fileLog("onAtrributeUpdated error: " + strJson)
                    continue
                }
                if (member.class_role == Role.STUDENT.intValue()) {
                    val lastValue = studentMap[member.uid]
                    if (lastValue == null) {
                        studentList.add(member)
                        studentMap[member.uid] = member
                        isAdd = true
                    } else {
                        if (lastValue.setData(member)) {
                            changeList.add(member)
                        }
                    }
                }
            }
            allMembers.clear()
            allMembers.add(getTeacherAttr())
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

    private val rtmClientListener = object : RtmManager.MyRtmClientListener() {
        override fun onMessageReceived(rtmMessage: RtmMessage?, peerId: String?) {
            if (rtmMessage == null)
                return
            val message: P2PMessage
            try {
                message = Gson().fromJson(rtmMessage.text, P2PMessage::class.java)
            } catch (e: JsonSyntaxException) {
                return
            }
            when (message.cmd) {
                P2PMessage.CMD_TEXT -> statusListener.onMessageReceived(rtmMessage, peerId)
                P2PMessage.CMD_ACCEPT_CALL -> {
//                        val attr:Member? = studentMap[peerId!!.toInt()]
//                        attr?.online_state = 5
                }
                P2PMessage.CMD_REFUSE_CALL -> {
//                        val attr:Member? = studentMap[peerId!!.toInt()]
//                        attr?.online_state = 6
                }
            }
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
        myAttr = Member(userId, userName, Role.TEACHER.intValue(), roomName)
        allMembers.add(myAttr)

        rtcWorker.setRtcEventHandler(object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                statusListener.onJoinChannelSuccess(channel, uid, elapsed)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                statusListener.onUserJoined(uid, elapsed)
            }
        })

        rtmManager.registerListener(rtmClientListener)

        rtcConfig(rtcWorker.rtcEngine)

        rtmChannel = rtmManager.createChannel(myAttr.room_name, rtmChannelListener)
    }

    fun updateTimeStamp(timeStampS: Long) {
        val rtmChannelAttributeClassStart = RtmChannelAttribute(
            KEY_TIME_STAMP_S,
            timeStampS.toString(),
            myAttr.uid.toString(),
            System.currentTimeMillis()
        )
        val options = ChannelAttributeOptions(true)
        rtmManager.rtmClient.addOrUpdateChannelAttributes(
            myAttr.room_name,
            mutableListOf(rtmChannelAttributeClassStart),
            options,
            object : ResultCallback<Void> {
                override fun onFailure(p0: ErrorInfo?) {
                    statusListener.onErrorInfo("updateTimeStamp failure.", p0)
                    log.i("updateTimeStamp failed")
                }

                override fun onSuccess(p0: Void?) {
                    log.i("updateTimeStamp success")
                }
            })
    }

    private fun addOrUpdateMyAttr() {
        log.i("addOrUpdateMyAttr")
        val gson = Gson()
        val rtmChannelAttributeMe = RtmChannelAttribute(
            myAttr.uid.toString(),
            gson.toJson(myAttr),
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

        rtmManager.joinChannel(rtmChannel, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                statusListener.onJoinRTMChannelSuccess(p0)
                log.i("join rtm channel success")
                addOrUpdateMyAttr()
            }

            override fun onFailure(p0: ErrorInfo?) {
                log.i("join rtm channel failed:" + p0.toString())
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

        deleteMyAttr()

        rtmManager.unregisterListener(rtmClientListener)
        rtmManager.leaveChannel(rtmChannel)
        rtmManager.releaseChannel(rtmChannel)
        rtmChannel = null
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
                    statusListener.onErrorInfo("deleteChannelAttributesByKeys failure.", p0)
                    log.i("deleteChannelAttributesByKeys failed")
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
                    statusListener.onErrorInfo("addOrUpdateChannelAttributes failure.", p0)
                    log.i("addOrUpdateChannelAttributes in delete failed")
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
        } else {
            rtcWorker.rtcEngine.setupRemoteVideo(
                VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, userId)
            )
        }
    }

    fun switchLocalAudio(bean: Member) {
        bean.is_mute_audio = !bean.is_mute_audio
        if (myAttr.uid == bean.uid) {
            rtcWorker.rtcEngine.muteLocalAudioStream(bean.is_mute_audio)
            addOrUpdateMyAttr()
        } else {
            if (bean.is_mute_audio) {
                sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_MUTE_AUDIO)))
            } else {
                sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_UN_MUTE_AUDIO)))
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

    fun switchLocalVideo(bean: Member) {
        bean.is_mute_video = !bean.is_mute_video
        if (myAttr.uid == bean.uid) {
            rtcWorker.rtcEngine.muteLocalVideoStream(bean.is_mute_video)
            addOrUpdateMyAttr()
        } else {
            if (bean.is_mute_video) {
                sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_MUTE_VIDEO)))
            } else {
                sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_UN_MUTE_VIDEO)))
            }
        }
    }

    fun setTeacherShare(isSharing: Boolean) {
        val member = getTeacherAttr()
        member.is_sharing = isSharing
    }

    fun switchTeacherLocalAudio(): Boolean {
        val member = getTeacherAttr()
        member.is_mute_audio = !member.is_mute_audio
        rtcWorker.rtcEngine.muteLocalAudioStream(member.is_mute_audio)
        addOrUpdateMyAttr()
        return member.is_mute_audio
    }

    fun switchTeacherLocalVideo(): Boolean {
        val member = getTeacherAttr()
        member.is_mute_video = !member.is_mute_video
        rtcWorker.rtcEngine.muteLocalVideoStream(member.is_mute_video)
        addOrUpdateMyAttr()
        return member.is_mute_video
    }

    fun onCallStudent(bean: Member) {
//        when (bean.online_state) {
//            0, 6 -> {
//                bean.online_state = 4
//                sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_CALL)))
//            }
//            5 -> {
//                bean.online_state = 0
//                sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_OFF_LINE)))
//            }
//        }
        if (bean.is_online) {
            sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_OFF_LINE)))
        } else {
            sendP2PMsg(bean.uid, Gson().toJson(P2PMessage(P2PMessage.CMD_CALL)))
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
        abstract fun onPartChange(changeList: MutableList<Member>)
        abstract fun onUpdateTimeStamp(timestampS: Long)
    }
}
