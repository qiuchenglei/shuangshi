package io.agora.rtc.shuangshi.classroom.student

import android.support.v7.widget.RecyclerView
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import io.agora.rtc.RtcEngine
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.shuangshi.base.RcvBaseAdapter
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.widget.projection.ProjectionView

class MembersAdapter(private val mPresenter: StudentPresenter, private val myUserId: Int) :
    RcvBaseAdapter<Member, MembersAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (viewType == 1) {
            return MyViewHolder(
                View.inflate(parent.context, R.layout.layout_teacher_for_student, null), 1
            )
        }
        return MyViewHolder(View.inflate(parent.context, R.layout.layout_student_for_student, null))
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int, bean: Member) {
        val surfaceView = RtcEngine.CreateRendererView(viewHolder.itemView.context)
        if (bean.class_role == Role.TEACHER.intValue()) {
            viewHolder.mIconSpeaker?.isSelected = bean.mute_remote_audio
            viewHolder.mIconSpeaker?.setOnClickListener {
                mPresenter.onClickSpeaker(bean)
                viewHolder.mIconSpeaker?.isSelected = bean.mute_remote_audio
            }
            viewHolder.mIconMax?.setOnClickListener {
                mPresenter.onClickTeacherMinOrMax(true)
            }
        } else {
            if (myUserId == bean.uid) {
                viewHolder.mIconMic?.visibility = View.VISIBLE
                viewHolder.mIconSpeaker?.visibility = View.GONE

                viewHolder.mIconMic?.isSelected = bean.mute_local_audio
                viewHolder.mIconCamera?.isSelected = bean.mute_local_video

                viewHolder.mIconMic?.setOnClickListener {
                    mPresenter.onClickMic(bean)
                    viewHolder.mIconMic?.isSelected = bean.mute_local_audio
                }
                viewHolder.mIconCamera?.setOnClickListener {
                    mPresenter.onClickCamera(bean)
                    notifyVideoView(viewHolder, bean, surfaceView)
                    viewHolder.mIconCamera?.isSelected = bean.mute_local_video
                }
                viewHolder.mIconSpeaker?.setOnClickListener(null)
            } else {
                viewHolder.mIconMic?.visibility = View.GONE
                viewHolder.mIconSpeaker?.visibility = View.VISIBLE

                viewHolder.mIconSpeaker?.isSelected = bean.mute_remote_audio

                viewHolder.mIconMic?.setOnClickListener(null)
                viewHolder.mIconCamera?.setOnClickListener(null)
                viewHolder.mIconSpeaker?.setOnClickListener {
                    mPresenter.onClickSpeaker(bean)
                    viewHolder.mIconSpeaker?.isSelected = bean.mute_remote_audio
                }
            }
        }

        viewHolder.mName.text = bean.user_name
        notifyVideoView(viewHolder, bean, surfaceView)
        mPresenter.bindEngineVideo(surfaceView, bean.uid)

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
        viewHolder.mIconCamera?.isSelected = bean.mute_local_video
    }

    override fun getItemViewType(position: Int): Int {
        return if (list!![position].class_role == Role.TEACHER.intValue()) 1 else 0
    }

    class MyViewHolder(itemView: View, viewType: Int = 0) : RecyclerView.ViewHolder(itemView) {
        // Teacher
        var mIconMax: ImageView? = null
        // student
        var mIconMic: ImageView? = null
        var mIconCamera: ImageView? = null
        var mIconSpeaker: ImageView? = null
        // all
        var mLayoutBg: FrameLayout
        var mLayoutVideo: FrameLayout
        var mName: TextView
        var mProjectionView: ProjectionView

        init {
            mLayoutVideo = itemView.findViewById(R.id.layout_video)
            mLayoutBg = itemView.findViewById(R.id.layout_bg)
            mName = itemView.findViewById(R.id.tv_name)
            if (viewType == 1) {
                mIconMax = itemView.findViewById(R.id.iv_icon_max)
            } else {
                mIconCamera = itemView.findViewById(R.id.iv_icon_camera)
                mIconMic = itemView.findViewById(R.id.iv_icon_mic)
            }
            mProjectionView = itemView.findViewById(R.id.projection_view)
        }

    }

}