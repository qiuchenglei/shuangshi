package io.agora.rtc.shuangshi.classroom.teacher

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.agora.rtc.RtcEngine
import io.agora.rtc.lib.custom.CustomGridLayoutManager
import io.agora.rtc.lib.custom.CustomLinearLayoutManager
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.classroom.GridItemDecoration
import io.agora.rtc.shuangshi.classroom.LinearItemDecoration
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.constant.IntentKey
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtc.shuangshi.widget.dialog.MyDialogFragment
import io.agora.rtc.shuangshi.widget.projection.ProjectionView

class ClassRoomTeacherActivity : ShareScreenActivity(), TeacherView {
    override fun onPartChanged(changeList: MutableList<Member>) {
        if (mRcvMembersLess.parent != null) {
            changeList.forEach { allMembersAdapter.updateItem(it) }
        }

        if (mRcvStudentsMore.parent != null) {
            changeList.forEach { studentsAdapter.updateItem(it) }
        }
    }

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

    override fun startShare(callback: StartShareCallback?) {
        startShareScreen(callback)
    }

    override fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun surfaceIsReady(previewSurface: View?) {
        showToast("开始录屏共享")
    }

    override fun showTeacherMax(isInClass: Boolean, teacherAttr: Member) {
        if (mLayoutTeacherMax.parent == null) {
            mLayoutMax.addView(
                mLayoutTeacherMax,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mPresenter.bindEngineVideo(mTeacherMaxSurfaceView)
        }
        mLayoutMax.visibility = View.VISIBLE

        mTeacherMaxLayoutStartShare.isSelected = teacherAttr.is_sharing
        mTeacherMaxIconMic.isSelected = teacherAttr.is_mute_audio
        mTeacherMaxIconCamera.isSelected = teacherAttr.is_mute_video
        mTeacherMaxTvName.text = teacherAttr.user_name
        if (/*isInClass &&*/ !teacherAttr.is_mute_video && !teacherAttr.is_projection) {
            mTeacherMaxLayoutBg.visibility = View.GONE
            mTeacherMaxLayoutVideo.visibility = View.VISIBLE
        } else {
            mTeacherMaxLayoutBg.visibility = View.VISIBLE
            mTeacherMaxLayoutVideo.visibility = View.GONE
        }
        mTeacherMaxProjectionView.showIsProjectionUI(teacherAttr.is_projection)
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
        studentsAdapter.mList = mutableListOf()
        mRcvStudentsMore.removeAllViews()
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
        allMembersAdapter.mList = mutableListOf()
        mRcvMembersLess.removeAllViews()
    }

    companion object {
        const val TAG = "ClassRoomTeacherActivity"
    }

    private val mPresenter: TeacherPresenter = TeacherPresenter(this, TeacherInteractor())

    private val allMembersAdapter: MembersAdapter = GridAdapter(mPresenter)
    private val studentsAdapter: MembersAdapter = LinearAdapter(mPresenter)

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
    private lateinit var mLayoutRcvLinear: FrameLayout
    private lateinit var mLayoutMax: FrameLayout
    private lateinit var mLayoutRcvGrid: FrameLayout

    private lateinit var mLayoutTeacherMax: FrameLayout
    private lateinit var mTeacherMaxLayoutVideo: FrameLayout
    private lateinit var mTeacherMaxLayoutStartShare: FrameLayout
    private lateinit var mTeacherMaxLayoutCloseShare: LinearLayout
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
        mTeacherMaxLayoutStartShare = mLayoutTeacherMax.findViewById(R.id.layout_start_share)
        mTeacherMaxLayoutCloseShare = mLayoutTeacherMax.findViewById(R.id.layout_close_share)
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
            if (mTeacherMaxIconCamera.isSelected) {
                mTeacherMaxLayoutBg.visibility = View.VISIBLE
                mTeacherMaxLayoutVideo.visibility = View.GONE
            } else {
                mTeacherMaxLayoutBg.visibility = View.GONE
                mTeacherMaxLayoutVideo.visibility = View.VISIBLE
            }
        }
        mTeacherMaxIconMic.setOnClickListener {
            mTeacherMaxIconMic.isSelected = mPresenter.onClickTeacherMaxMic()
        }
        mTeacherMaxLayoutStartShare.setOnClickListener {
            mTeacherMaxLayoutCloseShare.visibility = View.VISIBLE
            mTeacherMaxLayoutStartShare.visibility = View.GONE
            mPresenter.onClickTeacherShare(
                true,
                object : ShareScreenActivity.StartShareCallback {
                    override fun onSuccess() {}

                    override fun onFailure() {
                        mTeacherMaxLayoutCloseShare.callOnClick()
                    }
                })
        }
        mTeacherMaxLayoutCloseShare.setOnClickListener {
            mTeacherMaxLayoutCloseShare.visibility = View.GONE
            mTeacherMaxLayoutStartShare.visibility = View.VISIBLE
            mPresenter.onClickTeacherShare(false)
        }
        mTeacherMaxSurfaceView = RtcEngine.CreateRendererView(this)
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

        mLayoutMax = findViewById(R.id.layout_max)
        mLayoutRcvGrid = findViewById(R.id.layout_rcv_grid)
        mLayoutRcvLinear = findViewById(R.id.layout_rcv_linear)
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
        mIvIconExit.setOnClickListener { mPresenter.onClickClose() }

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
        mRcvMembersLess.adapter = allMembersAdapter
        mRcvMembersLess.addItemDecoration(GridItemDecoration(1))

        val linearLayoutManager =
            CustomLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRcvStudentsMore.layoutManager = linearLayoutManager
        mRcvStudentsMore.adapter = studentsAdapter
        mRcvStudentsMore.addItemDecoration(LinearItemDecoration(resources.getDimensionPixelSize(R.dimen.dp_4)))

        initTeacherMax()
    }

    override fun initData() {
        super.initData()
        val roomName = intent.getStringExtra(IntentKey.INTENT_KEY_ROOM_NAME)
        val userName = intent.getStringExtra(IntentKey.INTENT_KEY_USER_NAME)
        val userId = intent.getIntExtra(IntentKey.INTENT_KEY_USER_ID, 0)
        mTvRoomName.text = roomName
        mPresenter.onInit(roomName, userName, userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDestroy()
    }

    override fun onBackPressed() {
        mPresenter.onClickClose()
    }
}
