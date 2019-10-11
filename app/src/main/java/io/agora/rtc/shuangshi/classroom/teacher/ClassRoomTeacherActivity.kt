package io.agora.rtc.shuangshi.classroom.teacher

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.agora.rtc.RtcEngine
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.constant.IntentKey
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtc.shuangshi.widget.dialog.MyDialogFragment
import io.agora.rtc.shuangshi.widget.projection.ProjectionView

class ClassRoomTeacherActivity : ShareScreenActivity(), TeacherView {
    override fun showSettingDialog(settingListener: SettingFragmentDialog.SettingListener?) {
        SettingFragmentDialog().show(
            supportFragmentManager,
            "dialog_setting",
            settingListener
        )
    }

    override fun stopShare() {
        stopShareScreen()
    }

    override fun startShare() {
        startShareScreen()
    }

    override fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun surfaceIsReady(previewSurface: View?) {
        showToast("开始录屏共享")
    }

    override fun showTeacherMax(isInClass: Boolean, teacherAttr: Member) {
        mLayoutTeacherMax.visibility = View.VISIBLE

        mTeacherMaxIconShare.isSelected = teacherAttr.is_sharing
        mTeacherMaxIconMic.isSelected = teacherAttr.mute_local_audio
        mTeacherMaxIconCamera.isSelected = teacherAttr.mute_local_video
        mTeacherMaxTvName.text = teacherAttr.user_name
        if (isInClass && !teacherAttr.mute_local_video && !teacherAttr.is_projection) {
            mTeacherMaxLayoutBg.visibility = View.GONE
            mTeacherMaxLayoutVideo.visibility = View.VISIBLE
        } else {
            mTeacherMaxLayoutBg.visibility = View.VISIBLE
            mTeacherMaxLayoutVideo.visibility = View.GONE
        }
        mTeacherMaxProjectionView.showIsProjectionUI(teacherAttr.is_projection)
    }

    override fun dismissTeacherMax() {
        mLayoutTeacherMax.visibility = View.GONE
    }

    override fun showStudents(students: MutableList<Member>) {
        mRcvStudentsMore.visibility = View.VISIBLE
        studentsAdapter.list = students
        studentsAdapter.notifyDataSetChanged()
    }

    override fun dismissStudents() {
        mRcvStudentsMore.visibility = View.GONE
    }

    override fun showAllMembers(allMembers: MutableList<Member>) {
        mRcvMembersLess.visibility = View.VISIBLE
        allMembersAdapter.list = allMembers
        allMembersAdapter.notifyDataSetChanged()
    }

    override fun dismissAllMembers() {
        mRcvMembersLess.visibility = View.GONE
    }

    companion object {
        const val TAG = "ClassRoomTeacherActivity"
    }

    private val mPresenter: TeacherPresenter = TeacherPresenter(this, TeacherInteractor())

    private val allMembersAdapter: MembersAdapter = MembersAdapter(mPresenter)
    private val studentsAdapter: MembersAdapter = MembersAdapter(mPresenter)

    private lateinit var mTvRoomName: TextView
    private lateinit var mTvTimer: TextView
    private lateinit var mTvBeginOrFinish: TextView
    private lateinit var mIvIconClose: ImageView
    private lateinit var mIvIconMaximize: ImageView
    private lateinit var mIvIconMinimize: ImageView
    private lateinit var mIvIconExit: ImageView
    private lateinit var mIvIconSetting: ImageView
    private lateinit var mLayoutTitle: ConstraintLayout
    private lateinit var mRcvMembersLess: RecyclerView
    private lateinit var mRcvStudentsMore: RecyclerView

    private lateinit var mLayoutTeacherMax: FrameLayout
    private lateinit var mTeacherMaxLayoutVideo: FrameLayout
    private lateinit var mTeacherMaxIconShare: ImageView
    private lateinit var mTeacherMaxIconMic: ImageView
    private lateinit var mTeacherMaxIconCamera: ImageView
    private lateinit var mTeacherMaxLayoutBg: FrameLayout
    private lateinit var mTeacherMaxTvName: TextView
    private lateinit var mTeacherMaxSurfaceView: SurfaceView
    private lateinit var mTeacherMaxProjectionView: ProjectionView


    private fun initTeacherMax() {
        mLayoutTeacherMax = findViewById<FrameLayout>(R.id.layout_teacher_max)
        mTeacherMaxLayoutVideo = mLayoutTeacherMax.findViewById(R.id.layout_video)
        mTeacherMaxLayoutBg = mLayoutTeacherMax.findViewById(R.id.layout_bg)
        mTeacherMaxIconShare = mLayoutTeacherMax.findViewById(R.id.ic_share)
        mTeacherMaxIconCamera = mLayoutTeacherMax.findViewById(R.id.iv_icon_camera)
        mTeacherMaxIconMic = mLayoutTeacherMax.findViewById(R.id.iv_icon_mic)
        mTeacherMaxTvName = mLayoutTeacherMax.findViewById(R.id.tv_name)
        mTeacherMaxProjectionView = mLayoutTeacherMax.findViewById(R.id.projection_view)

        mTeacherMaxProjectionView.projectionListener =
            object : ProjectionView.OnProjectionListener {
                override fun onStartProjection() {
                    val isProjection = mPresenter.onStartProjection()
                    mTeacherMaxProjectionView.showIsProjectionUI(isProjection)
                    if (isProjection) {
                        mTeacherMaxLayoutVideo.visibility = View.GONE
                        mTeacherMaxLayoutBg.visibility = View.VISIBLE
                    }
                    mPresenter.bindEngineVideo(mTeacherMaxSurfaceView)
                }

                override fun onCancelProjection() {
                    mPresenter.onCancelProjection()
                    mTeacherMaxLayoutVideo.visibility = View.VISIBLE
                    mTeacherMaxLayoutBg.visibility = View.GONE
                    mPresenter.bindEngineVideo(mTeacherMaxSurfaceView)
                }
            }
        mTeacherMaxIconCamera.setOnClickListener {
            mTeacherMaxIconCamera.isSelected = mPresenter.onClickTeacherMaxCamera()
        }
        mTeacherMaxIconMic.setOnClickListener {
            mTeacherMaxIconMic.isSelected = mPresenter.onClickTeacherMaxMic()
        }
        mTeacherMaxIconShare.setOnClickListener {
            mTeacherMaxIconShare.isSelected = mPresenter.onClickTeacherShare()
        }

        mTeacherMaxSurfaceView = RtcEngine.CreateRendererView(this)
        mTeacherMaxLayoutVideo.addView(mTeacherMaxSurfaceView)
        mTeacherMaxSurfaceView.setZOrderMediaOverlay(false)
    }

    override fun updateTimer(strForTime: String) {
        val str = getString(R.string.class_timer_pre) + strForTime
        mTvTimer.text = str
    }

    override fun getTeacherViewMax(): SurfaceView {
        return mTeacherMaxSurfaceView
    }

    override fun showInClass(inClass: Boolean) {
        mTvBeginOrFinish.isSelected = inClass
        if (inClass) {
            mTvBeginOrFinish.text = getString(R.string.class_finish)
            mTvTimer.visibility = View.VISIBLE
        } else {
            mTvBeginOrFinish.text = getString(R.string.class_begin)
            mTvTimer.visibility == View.GONE
        }
    }

    override fun showDialog(
        resStr: Int,
        listener: MyDialogFragment.DialogClickListener,
        tag: String
    ) {
        MyDialogFragment.newInstance(listener, resStr).show(supportFragmentManager, tag)
    }

    override fun initUI(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_class_room_teacher)

        mTvRoomName = findViewById<TextView>(R.id.tv_room_name)
        mTvTimer = findViewById<TextView>(R.id.tv_timer)
        mTvBeginOrFinish = findViewById<TextView>(R.id.tv_begin_or_finish)
        mIvIconClose = findViewById<ImageView>(R.id.iv_icon_close)
        mIvIconMaximize = findViewById<ImageView>(R.id.iv_icon_maximize)
        mIvIconMinimize = findViewById<ImageView>(R.id.iv_icon_minimize)
        mIvIconExit = findViewById<ImageView>(R.id.iv_icon_exit)
        mIvIconSetting = findViewById<ImageView>(R.id.iv_icon_setting)
        mLayoutTitle = findViewById<ConstraintLayout>(R.id.layout_title)
        mRcvMembersLess = findViewById<RecyclerView>(R.id.rcv_members_less)
        mRcvStudentsMore = findViewById<RecyclerView>(R.id.rcv_students_more)

        mTvBeginOrFinish.setOnClickListener { mPresenter.onClickBeginClass() }
        mIvIconClose.setOnClickListener { mPresenter.onClickClose() }
        mIvIconSetting.setOnClickListener { mPresenter.onClickSetting() }

        val gridLayoutManager = GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false)
        mRcvMembersLess.layoutManager = gridLayoutManager
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (allMembersAdapter.itemCount < 3 || (position == 0 && allMembersAdapter.itemCount == 3))
                    return 2
                else
                    return 1
            }
        }
        mRcvMembersLess.adapter = allMembersAdapter
        mRcvStudentsMore.adapter = studentsAdapter

        initTeacherMax()
    }

    override fun initData() {
        super.initData()
        val roomName = intent.getStringExtra(IntentKey.INTENT_KEY_ROOM_NAME)
        val userName = intent.getStringExtra(IntentKey.INTENT_KEY_USER_NAME)
        val userId = intent.getIntExtra(IntentKey.INTENT_KEY_USER_ID, 0)
        mPresenter.onInit(roomName, userName, userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDestroy()
    }

}
