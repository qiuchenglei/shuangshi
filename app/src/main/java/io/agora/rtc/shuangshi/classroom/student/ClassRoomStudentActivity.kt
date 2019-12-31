package io.agora.rtc.shuangshi.classroom.student

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import io.agora.rtc.RtcEngine
import io.agora.rtc.lib.custom.CustomGridLayoutManager
import io.agora.rtc.lib.custom.CustomLinearLayoutManager
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.base.BaseActivity
import io.agora.rtc.shuangshi.classroom.GridItemDecoration
import io.agora.rtc.shuangshi.classroom.LinearItemDecoration
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.classroom.createRemoteVideoView
import io.agora.rtc.shuangshi.constant.IntentKey
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtc.shuangshi.widget.dialog.MyDialogFragment
import io.agora.rtc.shuangshi.widget.projection.ProjectionView
import io.agora.rtc.video.ViEEGLSurfaceRenderer

class ClassRoomStudentActivity : BaseActivity(), StudentView {
    override fun onPartChanged(changeList: MutableList<Member>) {
        changeList.forEach {
            if (mRcvMembersLess.parent != null) {
                changeList.forEach { allMembersAdapter.updateItem(it) }
            }
            if (it.class_role == Role.TEACHER.intValue() && mLayoutTeacherMax.parent != null) {
                mTeacherMaxTvName.text = it.user_name
                mTeacherMaxIcSpeaker.isSelected = it.is_mute_audio
                if (!it.is_mute_video) {
                    mTeacherMaxLayoutBg.visibility = View.GONE
                    mTeacherMaxLayoutVideo.visibility = View.VISIBLE
                } else {
                    mTeacherMaxLayoutBg.visibility = View.VISIBLE
                    mTeacherMaxLayoutVideo.visibility = View.GONE
                }
            }
            if (it.class_role == Role.STUDENT.intValue() && mRcvStudentsMore.parent != null)
                studentsAdapter.updateItem(it)
        }
    }

    override fun showSettingDialog(settingListener: SettingFragmentDialog.SettingListener?) {
        SettingFragmentDialog().show(
            supportFragmentManager,
            "dialog_setting",
            settingListener
        )
    }

    override fun showTeacherMax(isInClass: Boolean, teacherAttr: Member, isShowMin: Boolean) {
        if (mLayoutTeacherMax.parent == null) {
            mLayoutMax.addView(
                mLayoutTeacherMax,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mPresenter.bindEngineVideo(mTeacherMaxSurfaceView)
        }
        mLayoutMax.visibility = View.VISIBLE

        mTeacherMaxIcMin.visibility = if (isShowMin) View.VISIBLE else View.GONE

        mTeacherMaxTvName.text = teacherAttr.user_name
        mTeacherMaxIcSpeaker.isSelected = teacherAttr.is_mute_audio
        if (!teacherAttr.is_mute_video) {
            mTeacherMaxLayoutBg.visibility = View.GONE
            mTeacherMaxLayoutVideo.visibility = View.VISIBLE
        } else {
            mTeacherMaxLayoutBg.visibility = View.VISIBLE
            mTeacherMaxLayoutVideo.visibility = View.GONE
        }
    }

    override fun dismissTeacherMax() {
        mLayoutMax.removeView(mLayoutTeacherMax)
        mLayoutMax.visibility = View.GONE
    }

    override fun showStudents(students: MutableList<Member>) {
        if (mRcvStudentsMore.parent == null) {
            mLayoutRcvLinear.addView(
                mRcvStudentsMore,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        mLayoutRcvLinear.visibility = View.VISIBLE

        studentsAdapter.mList = students
        studentsAdapter.notifyDataSetChanged()
    }

    override fun dismissStudents() {
        mLayoutRcvLinear.removeView(mRcvStudentsMore)
        mLayoutRcvLinear.visibility = View.GONE
        mRcvStudentsMore.removeAllViews()
        studentsAdapter.mList = mutableListOf()
    }

    override fun showAllMembers(allMembers: MutableList<Member>) {
        if (mRcvMembersLess.parent == null) {
            mLayoutRcvGrid.addView(
                mRcvMembersLess,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        mLayoutRcvGrid.visibility = View.VISIBLE

        allMembersAdapter.mList = allMembers
        allMembersAdapter.notifyDataSetChanged()
    }

    override fun dismissAllMembers() {
        mLayoutRcvGrid.removeView(mRcvMembersLess)
        mLayoutRcvGrid.visibility = View.GONE
        mRcvMembersLess.removeAllViews()
        allMembersAdapter.mList = mutableListOf()
    }

    companion object {
        const val TAG = "ClassRoomStudentActivity"
    }

    private val mPresenter: StudentPresenter = StudentPresenter(this, StudentInteractor())

    private lateinit var allMembersAdapter: MembersAdapter
    private lateinit var studentsAdapter: MembersAdapter

    private lateinit var mTvRoomName: TextView
    private lateinit var mTvTimer: TextView
    private lateinit var mIvIconClose: ImageView
    private lateinit var mIvIconMaximize: ImageView
    private lateinit var mIvIconMinimize: ImageView
    private lateinit var mIvIconExit: ImageView
    private lateinit var mIvIconSetting: ImageView
    private lateinit var mLayoutTitle: ConstraintLayout
    private lateinit var mRcvMembersLess: RecyclerView
    private lateinit var mRcvStudentsMore: RecyclerView
    private lateinit var mLayoutRcvLinear: FrameLayout
    private lateinit var mLayoutMax: FrameLayout
    private lateinit var mLayoutRcvGrid: FrameLayout

    private lateinit var mLayoutTeacherMax: FrameLayout
    private lateinit var mTeacherMaxLayoutVideo: FrameLayout
    private lateinit var mTeacherMaxLayoutBg: FrameLayout
    private lateinit var mTeacherMaxTvName: TextView
    private lateinit var mTeacherMaxIcSpeaker: ImageView
    private lateinit var mTeacherMaxIcMin: ImageView
    private lateinit var mTeacherMaxSurfaceView: SurfaceView
    private lateinit var mTeacherMaxProjectionView: ProjectionView

    private fun initTeacherMax() {
        mLayoutTeacherMax = findViewById(R.id.layout_teacher_max)
        mTeacherMaxLayoutVideo = mLayoutTeacherMax.findViewById(R.id.layout_video)
        mTeacherMaxLayoutBg = mLayoutTeacherMax.findViewById(R.id.layout_bg)
        mTeacherMaxTvName = mLayoutTeacherMax.findViewById(R.id.tv_name)
        mTeacherMaxIcMin = mLayoutTeacherMax.findViewById(R.id.iv_icon_min)
        mTeacherMaxIcSpeaker = mLayoutTeacherMax.findViewById(R.id.iv_icon_speaker)
        mTeacherMaxIcMin.visibility = View.GONE
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
        mTeacherMaxIcMin.setOnClickListener { mPresenter.onClickTeacherMinOrMax(false) }
//        mTeacherMaxIcSpeaker.setOnClickListener { mPresenter.onClickTeacherSpeaker() }

        mTeacherMaxSurfaceView = createRemoteVideoView(applicationContext)
        mTeacherMaxLayoutVideo.addView(
            mTeacherMaxSurfaceView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
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
    }

    override fun showDialog(
        resStr: Int,
        listener: MyDialogFragment.DialogClickListener,
        tag: String
    ) {
        MyDialogFragment.newInstance(listener, resStr).show(supportFragmentManager, tag)
    }

    override fun initUI(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_class_room_student)

        mLayoutMax = findViewById(R.id.layout_max)
        mLayoutRcvGrid = findViewById(R.id.layout_rcv_grid)
        mLayoutRcvLinear = findViewById(R.id.layout_rcv_linear)
        mTvRoomName = findViewById<TextView>(R.id.tv_room_name)
        mTvTimer = findViewById<TextView>(R.id.tv_timer)
        mIvIconClose = findViewById<ImageView>(R.id.iv_icon_close)
        mIvIconMaximize = findViewById<ImageView>(R.id.iv_icon_maximize)
        mIvIconMinimize = findViewById<ImageView>(R.id.iv_icon_minimize)
        mIvIconExit = findViewById<ImageView>(R.id.iv_icon_exit)
        mIvIconSetting = findViewById<ImageView>(R.id.iv_icon_setting)
        mLayoutTitle = findViewById<ConstraintLayout>(R.id.layout_title)
        mRcvMembersLess = findViewById<RecyclerView>(R.id.rcv_members_less)
        mRcvStudentsMore = findViewById<RecyclerView>(R.id.rcv_students_more)

        mIvIconClose.setOnClickListener { mPresenter.onClickClose() }
        mIvIconSetting.setOnClickListener { mPresenter.onClickSetting() }
        mIvIconExit.setOnClickListener { mPresenter.onClickClose() }

        initTeacherMax()
    }

    override fun initData() {
        val roomName = intent.getStringExtra(IntentKey.INTENT_KEY_ROOM_NAME)
        val userName = intent.getStringExtra(IntentKey.INTENT_KEY_USER_NAME)
        val userId = intent.getIntExtra(IntentKey.INTENT_KEY_USER_ID, 0)

        mTvRoomName.text = roomName

        val gridLayoutManager =
            CustomGridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false)
        mRcvMembersLess.layoutManager = gridLayoutManager
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (allMembersAdapter.itemCount < 3 || (position == 0 && allMembersAdapter.itemCount == 3))
                    return 2
                else
                    return 1
            }
        }
        allMembersAdapter = GridAdapter(mPresenter, userId)
        studentsAdapter = LinearAdapter(mPresenter, userId)
        mRcvMembersLess.adapter = allMembersAdapter
        mRcvMembersLess.addItemDecoration(GridItemDecoration(1))

        val linearLayoutManager =
            CustomLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRcvStudentsMore.layoutManager = linearLayoutManager
        mRcvStudentsMore.adapter = studentsAdapter
        mRcvStudentsMore.addItemDecoration(LinearItemDecoration(resources.getDimensionPixelSize(R.dimen.dp_4)))

        mPresenter.onInit(roomName, userName, userId)
    }

    override fun finish() {
        super.finish()
        mPresenter.onDestroy()
    }

    override fun onBackPressed() {
        mPresenter.onClickClose()
    }
}
