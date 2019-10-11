package io.agora.rtc.shuangshi.classroom.teacher

import android.support.v7.widget.RecyclerView
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import io.agora.rtc.RtcEngine
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.shuangshi.base.RcvBaseAdapter
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.view.CheckableLinearLayout
import io.agora.rtc.shuangshi.widget.projection.ProjectionView

class MembersAdapter(val mPresenter: TeacherPresenter) :
    RcvBaseAdapter<Member, MembersAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (viewType == 1) {
            return MyViewHolder(
                View.inflate(parent.context, R.layout.layout_teacher_for_teacher, null),
                1
            )
        }
        return MyViewHolder(View.inflate(parent.context, R.layout.layout_student_for_teacher, null))
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int, bean: Member) {
        viewHolder.mIconMic.isSelected = bean.mute_local_audio
        notifyVideoView(viewHolder, bean, RtcEngine.CreateRendererView(viewHolder.itemView.context))

        viewHolder.mIconMic.setOnClickListener {
            mPresenter.onClickMic(bean)
            viewHolder.mIconMic.isSelected = bean.mute_local_audio
        }
        viewHolder.mIconCamera.setOnClickListener {
            mPresenter.onClickCamera(bean)
            notifyVideoView(
                viewHolder,
                bean,
                RtcEngine.CreateRendererView(viewHolder.itemView.context)
            )
        }

        if (bean.class_role == Role.TEACHER.intValue()) {
            viewHolder.mIconShare?.isSelected = bean.is_sharing
            viewHolder.mIconShare?.setOnClickListener {
                val isSharing = mPresenter.onClickTeacherShare()
                viewHolder.mIconShare?.isSelected = isSharing
            }
        } else {
            viewHolder.mLayoutChosen?.isSelected = bean.online_state in listOf(1, 2, 5)
            viewHolder.mLayoutChosen?.setOnClickListener { mPresenter.onCallStudent(bean) }
        }

        viewHolder.mProjectionView.showIsProjectionUI(bean.is_projection)
        viewHolder.mProjectionView.projectionListener = object : ProjectionView.OnProjectionListener {
            override fun onStartProjection() {
                mPresenter.onStartProjection()
                if (bean.is_projection) {
                    viewHolder.mLayoutVideo.visibility = View.GONE
                    viewHolder.mLayoutVideo.removeAllViews()
                    viewHolder.mLayoutBg.visibility = View.VISIBLE
                }
            }

            override fun onCancelProjection() {
                mPresenter.onCancelProjection(bean)
                notifyVideoView(viewHolder, bean, RtcEngine.CreateRendererView(viewHolder.itemView.context))
            }
        }
    }

    val KEY_TAG_UID = 100;

    private fun notifyVideoView(viewHolder: MyViewHolder, bean: Member, surfaceView: SurfaceView) {
        surfaceView.setTag(KEY_TAG_UID, bean.uid)
        if (bean.mute_local_video || bean.is_projection) {
            viewHolder.mLayoutVideo.visibility = View.GONE
            viewHolder.mLayoutVideo.removeAllViews()
            viewHolder.mLayoutBg.visibility = View.VISIBLE
        } else {
            if (viewHolder.mLayoutVideo.childCount > 0) {
                val lasSurfaceView = viewHolder.mLayoutVideo.getChildAt(0)
                if (lasSurfaceView.getTag(KEY_TAG_UID) as Int != bean.uid) {
                    viewHolder.mLayoutVideo.removeAllViews()
                    viewHolder.mLayoutVideo.addView(surfaceView)
                    mPresenter.bindEngineVideo(surfaceView, bean.uid)
                }
            } else {
                viewHolder.mLayoutVideo.addView(surfaceView)
                mPresenter.bindEngineVideo(surfaceView, bean.uid)
            }
            viewHolder.mLayoutVideo.visibility = View.VISIBLE
            viewHolder.mLayoutBg.visibility = View.GONE
        }
        viewHolder.mIconCamera.isSelected = bean.mute_local_video
    }

    override fun getItemViewType(position: Int): Int {
        if (list!![position].class_role == Role.TEACHER.intValue()) {
            return 1
        }
        return 0
    }

    class MyViewHolder(itemView: View, viewType: Int = 0) : RecyclerView.ViewHolder(itemView) {
        // teacher
        var mIconShare: ImageView? = null
        // student
        var mLayoutChosen: CheckableLinearLayout? = null
        // all
        var mIconMic: ImageView
        var mIconCamera: ImageView
        var mLayoutVideo: FrameLayout
        var mLayoutBg: FrameLayout
        var mProjectionView: ProjectionView

        init {
            mLayoutVideo = itemView.findViewById(R.id.layout_video)
            mLayoutBg = itemView.findViewById(R.id.layout_bg)
            mIconCamera = itemView.findViewById(R.id.iv_icon_camera)
            mIconMic = itemView.findViewById(R.id.iv_icon_mic)
            if (viewType == 1) {
                mIconShare = itemView.findViewById(R.id.ic_share)
            } else {
                mLayoutChosen = itemView.findViewById(R.id.checkable_layout_chosen)
            }
            mProjectionView = itemView.findViewById(R.id.projection_view)
        }
    }
}