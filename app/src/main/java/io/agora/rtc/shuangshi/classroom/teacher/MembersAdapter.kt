package io.agora.rtc.shuangshi.classroom.teacher

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.agora.rtc.RtcEngine
import io.agora.rtc.lib.util.DensityUtil
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.shuangshi.base.RcvBaseAdapter
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.view.CheckableLinearLayout
import io.agora.rtc.shuangshi.widget.projection.ProjectionView

class MembersAdapter(val mPresenter: TeacherPresenter) :
    RcvBaseAdapter<Member, MembersAdapter.MyViewHolder>() {
    override fun isEqual(old: Member, new: Member): Boolean {
        return old.uid == new.uid
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (viewType == 1) {
            return MyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_teacher_for_teacher,
                    parent,
                    false
                ),
                1
            )
        }
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_student_for_teacher,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(
        viewHolder: MyViewHolder,
        position: Int,
        bean: Member,
        payloads: MutableList<Any>
    ) {
//        if (payloads.isEmpty()) {
            val ctx = viewHolder.itemView.context
            if (itemCount < 2) {
                viewHolder.itemView.layoutParams.width = DensityUtil.getScreenWidth(ctx)
            } else {
                viewHolder.itemView.layoutParams.width = DensityUtil.getScreenWidth(ctx) / 2
            }

            viewHolder.mIconMic.isSelected = bean.is_mute_audio
            viewHolder.mName.text = bean.user_name
            notifyVideoView(
                viewHolder,
                bean,
                RtcEngine.CreateRendererView(viewHolder.itemView.context)
            )

            viewHolder.mIconMic.setOnClickListener {
                mPresenter.onClickMic(bean)
                viewHolder.mIconMic.isSelected = bean.is_mute_audio
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
                if (bean.is_sharing) {
                    viewHolder.mLayoutCloseShare?.visibility = View.VISIBLE
                    viewHolder.mLayoutStartShare?.visibility = View.GONE
                } else {
                    viewHolder.mLayoutCloseShare?.visibility = View.GONE
                    viewHolder.mLayoutStartShare?.visibility = View.VISIBLE
                }
                viewHolder.mLayoutStartShare?.setOnClickListener {
                    viewHolder.mLayoutCloseShare?.visibility = View.VISIBLE
                    viewHolder.mLayoutStartShare?.visibility = View.GONE
                    bean.is_sharing = true
                    mPresenter.onClickTeacherShare(
                        true,
                        object : ShareScreenActivity.StartShareCallback {
                            override fun onSuccess() {}

                            override fun onFailure() {
                                viewHolder.mLayoutCloseShare?.callOnClick()
                            }
                        })
                }
                viewHolder.mLayoutCloseShare?.setOnClickListener {
                    viewHolder.mLayoutCloseShare?.visibility = View.GONE
                    viewHolder.mLayoutStartShare?.visibility = View.VISIBLE
                    bean.is_sharing = false
                    mPresenter.onClickTeacherShare(false)
                }
            } else {
                if (bean.is_online) {
                    bean.online_state = 5
                } else {
                    bean.online_state = 0
                }
                viewHolder.mLayoutChosen?.isSelected = /*bean.online_state in listOf(1, 2, 5) ||*/ bean.is_online
                viewHolder.mLayoutChosen?.setOnClickListener { mPresenter.onCallStudent(bean) }
            }

            viewHolder.mProjectionView.showIsProjectionUI(bean.is_projection)
            viewHolder.mProjectionView.projectionListener =
                object : ProjectionView.OnProjectionListener {
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
                        notifyVideoView(
                            viewHolder,
                            bean,
                            RtcEngine.CreateRendererView(viewHolder.itemView.context)
                        )
                    }
                }
//        } else {
//
//        }
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
        viewHolder.mIconCamera.isSelected = bean.is_mute_video
    }

    override fun getItemViewType(position: Int): Int {
        if (mList[position].class_role == Role.TEACHER.intValue()) {
            return 1
        }
        return 0
    }

    class MyViewHolder(itemView: View, viewType: Int = 0) : RecyclerView.ViewHolder(itemView) {
        // teacher
        var mLayoutStartShare: FrameLayout? = null
        var mLayoutCloseShare: LinearLayout? = null
        // student
        var mLayoutChosen: CheckableLinearLayout? = null
        // all
        var mIconMic: ImageView
        var mIconCamera: ImageView
        var mLayoutVideo: FrameLayout
        var mLayoutBg: FrameLayout
        var mProjectionView: ProjectionView
        var mName: TextView

        init {
            mLayoutVideo = itemView.findViewById(R.id.layout_video)
            mLayoutBg = itemView.findViewById(R.id.layout_bg)
            mIconCamera = itemView.findViewById(R.id.iv_icon_camera)
            mIconMic = itemView.findViewById(R.id.iv_icon_mic)
            mName = itemView.findViewById(R.id.tv_name)
            if (viewType == 1) {
                mLayoutStartShare = itemView.findViewById(R.id.layout_start_share)
                mLayoutCloseShare = itemView.findViewById(R.id.layout_close_share)
            } else {
                mLayoutChosen = itemView.findViewById(R.id.checkable_layout_chosen)
            }
            mProjectionView = itemView.findViewById(R.id.projection_view)
        }
    }
}