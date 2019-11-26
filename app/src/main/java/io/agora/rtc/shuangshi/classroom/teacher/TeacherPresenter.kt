package io.agora.rtc.shuangshi.classroom.teacher

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
import io.agora.rtc.video.ViEEGLSurfaceRenderer
import io.agora.rtc.video.VideoCanvas
import io.agora.rtm.ErrorInfo
import io.agora.rtm.RtmChannelMember
import io.agora.rtm.RtmMessage

class TeacherPresenter(var mView: TeacherView?, val mInteractor: TeacherInteractor) {
    val log = LogUtil("StudentPresenter")
    fun onInit(roomName: String, userName: String, userId: Int) {

        handler = Handler()
        mInteractor.init(
            roomName,
            userName,
            userId,
            object : TeacherInteractor.ClassStatusListener() {
                override fun onUpdateTimeStamp(timestampS: Long) {
                    handler?.post {
                        if (isInClass != timestampS > 0) {
                            isInClass = timestampS > 0
                            handler?.removeCallbacks(timerRunnable)
                            if (isInClass) {
                                mTimerCount = System.currentTimeMillis() / 1000 - timestampS
                                handler?.post(timerRunnable)
                            } else {
                                mView?.updateTimer("00:00:00")
                            }
                            mView?.showInClass(isInClass)
                        }
                    }
                }

                override fun onPartChange(changeList: MutableList<Member>) {
                    handler?.post {
                        mView?.onPartChanged(changeList)
                    }
                }

                override fun onUpdateMembers() {
                    handler?.post {
                        updateMembersUI()
                    }
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

        mView?.updateTimer("00:00:00")

        bindEngineVideo(mView!!.getTeacherViewMax())
        mInteractor.joinChannel()
        updateMembersUI()
    }

    fun onClickTeacherShare(
        isSharing: Boolean,
        callback: ShareScreenActivity.StartShareCallback? = null
    ) {
        mInteractor.setTeacherShare(isSharing)
        if (isSharing) {
            mView?.startShare(callback)
        } else {
            mView?.stopShare()
            mInteractor.startRtcSDKSource()
        }
    }

    fun onClickTeacherMaxMic(): Boolean {
        return mInteractor.switchTeacherLocalAudio()
    }

    fun onClickTeacherMaxCamera(): Boolean {
        return mInteractor.switchTeacherLocalVideo()
    }

    fun onClickMic(bean: Member) {
        mInteractor.switchLocalAudio(bean)
    }

    fun onClickCamera(bean: Member) {
        mInteractor.switchLocalVideo(bean)
    }

    fun bindEngineVideo(surfaceView: SurfaceView?, userId: Int = mInteractor.getTeacherAttr().uid) {
        mInteractor.bindEngineVideo(surfaceView, userId)
    }

    private fun updateMembersUI(
        students: MutableList<Member> = mInteractor.studentList,
        allMembers: MutableList<Member> = mInteractor.allMembers
    ) {
        if (students.isEmpty()) {
            mView?.dismissAllMembers()
            mView?.dismissStudents()
            showTeacherMaxUI()
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
            mInteractor.updateTimeStamp(System.currentTimeMillis() / 1000)
        } else {
            mView?.updateTimer("00:00:00")
            mInteractor.updateTimeStamp(-1)
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
        mView?.showTeacherMax(isInClass, mInteractor.getTeacherAttr())
    }

    fun onCallStudent(bean: Member) {
        if (!bean.is_online) {
            showToast("你对${bean.user_name}发送了点名邀请")
        }
        mInteractor.onCallStudent(bean)
    }

    private fun showToast(s: String) {
        mView?.showToast(s)
    }

    private var projection: Projection = Projection()

    fun createSurfaceView(uid:Int, context: Context): SurfaceView{
        val surfaceView:SurfaceView
        if (uid == mInteractor.getMyAttr().uid) {
            surfaceView = RtcEngine.CreateRendererView(context)
        } else {
            surfaceView = ViEEGLSurfaceRenderer(context.applicationContext)
        }
        return surfaceView
    }

    fun onStartProjection(
        bean: Member = mInteractor.getTeacherAttr()
    ): Boolean {
        if (mView == null)
            return bean.is_projection

        val context = mView as Context
        val surfaceView:SurfaceView = createSurfaceView(bean.uid, context)

        bean.is_projection = projection.startProjection(
            context,
            Projection.ProjectionConfig(bean.uid, surfaceView)
        )

        if (bean.is_projection) {
            if (bean.uid == mInteractor.getMyAttr().uid) {
                mInteractor.getRtcEngine().setupLocalVideo(VideoCanvas(surfaceView))
            } else {
                mInteractor.getRtcEngine().setupRemoteVideo(
                    VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, bean.uid)
                )
            }
        }

        return bean.is_projection
    }

    fun onCancelProjection(bean: Member = mInteractor.getTeacherAttr()) {
        bean.is_projection = false
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