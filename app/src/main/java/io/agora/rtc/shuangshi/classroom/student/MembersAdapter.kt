package io.agora.rtc.shuangshi.classroom.student

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import io.agora.rtc.RtcEngine
import io.agora.rtc.lib.util.DensityUtil
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.shuangshi.base.RcvBaseAdapter
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.widget.projection.ProjectionView

abstract class MembersAdapter(private val mPresenter: StudentPresenter, private val myUserId: Int) :
    RcvBaseAdapter<Member, MembersAdapter.MyViewHolder>() {
    override fun isEqual(old: Member, new: Member): Boolean {
        return old.uid == new.uid
    }

    abstract fun getItemWidth(ctx: Context): Int
    abstract fun getItemHeight(ctx: Context): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val ctx = parent.context
        if (viewType == 1) {
            return MyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_teacher_for_student, parent,
                    false
                ),
                getItemWidth(ctx),
                getItemHeight(ctx),
                1
            )
        }
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_student_for_student, parent,
                false
            ),
            getItemWidth(ctx),
            getItemHeight(ctx)
        )
    }

    override fun onBindViewHolder(
        viewHolder: MyViewHolder,
        position: Int,
        bean: Member,
        payloads: MutableList<Any>
    ) {
        val ctx = viewHolder.itemView.context
        val width = getItemWidth(ctx)
        if (width != viewHolder.itemView.layoutParams.width) {
            viewHolder.itemView.layoutParams.width = width
        }

        if (bean.class_role == Role.TEACHER.intValue()) {
            viewHolder.mIconMax?.setOnClickListener {
                mPresenter.onClickTeacherMinOrMax(true)
            }
        } else {
            viewHolder.mName.isSelected = bean.is_online
            if (myUserId == bean.uid) {
                viewHolder.mLayoutMe?.visibility = View.VISIBLE
                viewHolder.mIconSpeaker.visibility = View.GONE
                viewHolder.mIconMic?.isSelected = bean.is_mute_audio
                viewHolder.mIconCamera?.isSelected = bean.is_mute_video
                viewHolder.mIconMic?.setOnClickListener {
                    mPresenter.onClickMic(bean)
                    viewHolder.mIconMic?.isSelected = bean.is_mute_audio
                }
                viewHolder.mIconCamera?.setOnClickListener {
                    mPresenter.onClickCamera(bean)
                    notifyVideoView(
                        viewHolder, bean,
                        RtcEngine.CreateRendererView(viewHolder.itemView.context)
                    )
                    viewHolder.mIconCamera?.isSelected = bean.is_mute_video
                }
            } else {
                viewHolder.mLayoutMe?.visibility = View.GONE
                viewHolder.mIconSpeaker.visibility = View.VISIBLE
                viewHolder.mIconMic?.setOnClickListener(null)
                viewHolder.mIconCamera?.setOnClickListener(null)
            }
        }

        viewHolder.mIconSpeaker.isSelected = bean.is_mute_audio
//            viewHolder.mIconSpeaker?.setOnClickListener {
//                mPresenter.onClickSpeaker(bean)
//                viewHolder.mIconSpeaker?.isSelected = bean.mute_remote_audio
//            }
        viewHolder.mName.text = bean.user_name
        notifyVideoView(viewHolder, bean, RtcEngine.CreateRendererView(viewHolder.itemView.context))

        viewHolder.mProjectionView.showIsProjectionUI(bean.is_projection)
        viewHolder.mProjectionView.projectionListener =
            object : ProjectionView.OnProjectionListener {
                override fun onStartProjection() {
                    if (mPresenter.onStartProjection(bean)) {
                        viewHolder.mLayoutVideo.visibility = View.GONE
                        viewHolder.mLayoutVideo.removeAllViews()
                        viewHolder.mLayoutBg.visibility = View.VISIBLE
                    }
                }

                override fun onCancelProjection() {
                    mPresenter.onCancelProjection(bean)
                    notifyVideoView(
                        viewHolder, bean,
                        RtcEngine.CreateRendererView(viewHolder.itemView.context)
                    )
                }
            }
    }

    private fun notifyVideoView(viewHolder: MyViewHolder, bean: Member, surfaceView: SurfaceView) {
        surfaceView.tag = bean.uid
        if (bean.is_mute_video || bean.is_projection) {
            viewHolder.mLayoutVideo.visibility = View.GONE
            viewHolder.mLayoutVideo.removeAllViews()
            viewHolder.mLayoutBg.visibility = View.VISIBLE
        } else {
            if (viewHolder.mLayoutVideo.childCount > 0) {
                val lasSurfaceView = viewHolder.mLayoutVideo.getChildAt(0)
                if (lasSurfaceView.tag != null && lasSurfaceView.tag as Int != bean.uid) {
                    viewHolder.mLayoutVideo.removeAllViews()
                    surfaceView.setZOrderMediaOverlay(true)
                    viewHolder.mLayoutVideo.addView(surfaceView)
                    mPresenter.bindEngineVideo(surfaceView, bean.uid)
                }
            } else {
                surfaceView.setZOrderMediaOverlay(true)
                viewHolder.mLayoutVideo.addView(surfaceView)
                mPresenter.bindEngineVideo(surfaceView, bean.uid)
            }
            viewHolder.mLayoutVideo.visibility = View.VISIBLE
            viewHolder.mLayoutBg.visibility = View.GONE
        }
        viewHolder.mIconCamera?.isSelected = bean.is_mute_video
    }

    override fun getItemViewType(position: Int): Int {
        return if (mList[position].class_role == Role.TEACHER.intValue()) 1 else 0
    }

    class MyViewHolder(itemView: View, width: Int, height: Int, viewType: Int = 0) :
        RecyclerView.ViewHolder(itemView) {
        // Teacher
        var mIconMax: ImageView? = null
        // student
        var mIconMic: ImageView? = null
        var mIconCamera: ImageView? = null
        var mLayoutMe: FrameLayout? = null
        // all
        var mIconSpeaker: ImageView
        var mLayoutBg: FrameLayout
        var mLayoutVideo: FrameLayout
        var mName: TextView
        var mProjectionView: ProjectionView

        init {
            mLayoutVideo = itemView.findViewById(R.id.layout_video)
            mLayoutBg = itemView.findViewById(R.id.layout_bg)
            mName = itemView.findViewById(R.id.tv_name)
            mIconSpeaker = itemView.findViewById(R.id.iv_icon_speaker)
            if (viewType == 1) {
                mIconMax = itemView.findViewById(R.id.iv_icon_max)
            } else {
                mIconCamera = itemView.findViewById(R.id.iv_icon_camera)
                mIconMic = itemView.findViewById(R.id.iv_icon_mic)
                mLayoutMe = itemView.findViewById(R.id.fl_me)
            }
            mProjectionView = itemView.findViewById(R.id.projection_view)

            itemView.layoutParams = ViewGroup.LayoutParams(width, height)
        }

    }

}