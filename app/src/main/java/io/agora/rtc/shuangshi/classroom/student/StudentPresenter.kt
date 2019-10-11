package io.agora.rtc.shuangshi.classroom.student

import android.content.Context
import android.os.Handler
import android.view.SurfaceView
import io.agora.rtc.RtcEngine
import io.agora.rtc.shuangshi.R
import io.agora.rtc.lib.projection.Projection
import io.agora.rtc.lib.util.LogUtil
import io.agora.rtc.lib.util.stringForTime
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtc.shuangshi.widget.dialog.MyDialogFragment
import io.agora.rtc.video.VideoCanvas
import io.agora.rtm.ErrorInfo
import io.agora.rtm.RtmChannelMember
import io.agora.rtm.RtmMessage

class StudentPresenter(var mView: StudentView?, val mInteractor: StudentInteractor) {
    val log = LogUtil("StudentPresenter")
    fun onInit(roomName: String, userName: String, userId: Int) {

        handler = Handler()
        mInteractor.init(
            roomName,
            userName,
            userId,
            object : StudentInteractor.ClassStatusListener() {
                override fun onTeacherCall() {
                    mView?.showDialog(R.string.teacher_call_notification_text,
                        object : MyDialogFragment.DialogClickListener {
                            override fun clickYes() {
                                mInteractor.onLine(true)
                            }

                            override fun clickNo() {
                            }

                        }, "call_notification")
                }

                override fun onUpdateMembers() {
                    updateMembersUI()
                }

                override fun onErrorInfo(errorLog: String, errorInfo: ErrorInfo?) {
                    log.e(errorLog)
                }

                override fun onMessageReceived(rtmManager: RtmMessage?, peerId: String?) {
                    log.i("receive_p2p_message:$peerId, " + rtmManager?.text)
                }

                override fun onJoinRTMChannelSuccess(p0: Void?) {
                    log.d("onJoinRTMChannelSuccess")
                }

                override fun onMessageReceived(p0: RtmMessage?, p1: RtmChannelMember?) {
                    log.d("onChannelMessageReceived")
                }

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    log.d("onJoinRTCChannelSuccess")
                }
            })

        mInteractor.setLocalSurfaceView(mView!!.getTeacherViewMax(), false)

        showTeacherMaxUI()
    }

    private fun updateMembersUI(
        students: MutableList<Member> = mInteractor.studentList,
        allMembers: MutableList<Member> = mInteractor.allMembers
    ) {
        if (allMembers.size < 2) {
            showTeacherMaxUI()
            mView?.dismissAllMembers()
            mView?.dismissStudents()
        } else if (allMembers.size < 5) {
            mView?.showAllMembers(allMembers)
            mView?.dismissTeacherMax()
            mView?.dismissStudents()
        } else {
            showTeacherMaxUI()
            mView?.showStudents(students)
            mView?.dismissAllMembers()
        }
    }

    var handler: Handler? = null
    private var mTimerCount: Long = 0L

    val timerRunnable = Runnable { updateTimer() }

    private fun updateTimer() {
        if (!isInClass)
            mTimerCount = 0L

        mView?.updateTimer(stringForTime(mTimerCount, "%d:%02d:%02d"))

        if (isInClass) {
            mTimerCount++
            handler?.postDelayed(timerRunnable, 1000)
        }
    }

    fun onDestroy() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
        mView = null
        mInteractor.destroy()
    }

    private var isInClass = false
    fun onClickBeginClass() {
        isInClass = !isInClass
        mView?.showInClass(isInClass)

        handler?.removeCallbacks(timerRunnable)
        if (isInClass) {
            mTimerCount = 0L
            handler?.post(timerRunnable)
        } else {
            mView?.updateTimer("")
        }

        if (isInClass) {
            mInteractor.joinChannel()
            updateMembersUI()
        } else {
            mInteractor.leaveChannel()
            updateMembersUI()
        }
    }

    fun onClickClose() {
        mView?.showDialog(
            R.string.Are_you_sure_to_leave_the_classroom,
            object : MyDialogFragment.DialogClickListener {
                override fun clickYes() {
                    mView?.finish()
                }

                override fun clickNo() {
                }
            },
            "close"
        )
    }

    private fun showTeacherMaxUI() {
        val teacher = mInteractor.getTeacherAttr()
        val isShowMin = mInteractor.studentList.size in 1..3
        if (teacher != null)
            mView?.showTeacherMax(isInClass, teacher, isShowMin)
        else {
            mView?.showTeacherMax(isInClass, Member(0, ""), isShowMin)
        }
    }

    fun onClickMic(bean: Member) {
        mInteractor.switchLocalAudio(bean)
    }

    fun onClickCamera(bean: Member) {
        mInteractor.switchLocalVideo(bean)
    }

    fun bindEngineVideo(surfaceView: SurfaceView?, userId: Int) {
        mInteractor.bindEngineVideo(surfaceView, userId)
    }

    fun bindEngineVideo(surfaceView: SurfaceView?) {
        val attr = mInteractor.getTeacherAttr()
        if (attr == null)
            return
        bindEngineVideo(surfaceView, attr.uid)
    }

    fun onClickTeacherMinOrMax(isMax: Boolean) {
        isShowTeacherMaxByUser = isMax
    }

    private var isShowTeacherMaxByUser: Boolean = false

    fun onClickTeacherSpeaker() {
        mInteractor.changeTeacherSpeaker()
        showTeacherMaxUI()
    }

    fun onClickSpeaker(bean: Member) {
        mInteractor.changeSpeaker(bean)
    }


    private var projection: Projection = Projection()

    fun onStartProjection(
        bean: Member? = mInteractor.getTeacherAttr()
    ) : Boolean {
        if (mView == null || bean == null)
            return false

        val context = mView as Context
        val surfaceView = RtcEngine.CreateRendererView(context)

        bean.is_projection = projection.startProjection(
            context,
            Projection.ProjectionConfig(bean.uid, surfaceView)
        )

        if (bean.is_projection) {
            if (bean.uid == mInteractor.myAttr.uid) {
                mInteractor.getRtcEngine().setupLocalVideo(VideoCanvas(surfaceView))
            } else {
                mInteractor.getRtcEngine().setupRemoteVideo(
                    VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, bean.uid)
                )
            }
        }

        return bean.is_projection
    }

    fun onCancelProjection(bean: Member? = mInteractor.getTeacherAttr()) {
        bean?.is_projection = false
        projection.cancelProjection()
    }

    fun onClickSetting() {
        mView?.showSettingDialog(object : SettingFragmentDialog.SettingListener {
            override fun onApply() {
                mInteractor.changeRtcConfig()
            }

            override fun onCancel() {}
        })
    }

}