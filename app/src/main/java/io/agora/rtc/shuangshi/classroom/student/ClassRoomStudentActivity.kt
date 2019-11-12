package io.agora.rtc.shuangshi.classroom.student

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
import io.agora.rtc.RtcEngine
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.base.BaseActivity
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.constant.IntentKey
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtc.shuangshi.widget.dialog.MyDialogFragment
import io.agora.rtc.shuangshi.widget.projection.ProjectionView

class ClassRoomStudentActivity : BaseActivity(), StudentView {
    override fun showSettingDialog(settingListener: SettingFragmentDialog.SettingListener?) {
        SettingFragmentDialog().show(
            supportFragmentManager,
            "dialog_setting",
            settingListener
        )
    }

    override fun showTeacherMax(isInClass: Boolean, teacherAttr: Member, isShowMin: Boolean) {
        mLayoutTeacherMax.visibility = View.VISIBLE
        mTeacherMaxIcMin.visibility = if (isShowMin) View.VISIBLE else View.GONE

        mTeacherMaxTvName.text = teacherAttr.user_name
        mTeacherMaxIcSpeaker.isSelected = teacherAttr.mute_remote_audio
        if (isInClass && !teacherAttr.mute_local_video) {
            mTeacherMaxLayoutBg.visibility = View.GONE
            mTeacherMaxLayoutVideo.visibility = View.VISIBLE
        } else {
            mTeacherMaxLayoutBg.visibility = View.VISIBLE
            mTeacherMaxLayoutVideo.visibility = View.GONE
        }
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
        const val TAG = "ClassRoomStudentActivity"
    }

    private val mPresenter: StudentPresenter = StudentPresenter(this, StudentInteractor())

    private lateinit var allMembersAdapter: MembersAdapter
    private lateinit var studentsAdapter: MembersAdapter

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

        mTeacherMaxProjectionView.projectionListener = object : ProjectionView.OnProjectionListener {
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
        mTeacherMaxIcSpeaker.setOnClickListener { mPresenter.onClickTeacherSpeaker() }

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
        } else {
            mTvBeginOrFinish.text = getString(R.string.class_begin)
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
        setContentView(R.layout.activity_class_room_student)

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
        mIvIconSetting.setOnClickListener {mPresenter.onClickSetting()}
        mIvIconExit.setOnClickListener { mPresenter.onClickClose() }

        initTeacherMax()
    }

    override fun initData() {
        val roomName = intent.getStringExtra(IntentKey.INTENT_KEY_ROOM_NAME)
        val userName = intent.getStringExtra(IntentKey.INTENT_KEY_USER_NAME)
        val userId = intent.getIntExtra(IntentKey.INTENT_KEY_USER_ID, 0)

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
        allMembersAdapter = MembersAdapter(mPresenter, userId)
        studentsAdapter = MembersAdapter(mPresenter, userId)
        mRcvMembersLess.adapter = allMembersAdapter
        mRcvStudentsMore.adapter = studentsAdapter

        mPresenter.onInit(roomName, userName, userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDestroy()
    }

}
