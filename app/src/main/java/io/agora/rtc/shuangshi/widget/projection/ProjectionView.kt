package io.agora.rtc.shuangshi.widget.projection

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import io.agora.rtc.shuangshi.R

class ProjectionView : FrameLayout {
    constructor(context: Context) : super(context) {
        this.init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.init()
    }

    private lateinit var mLayoutParent: ConstraintLayout
    private lateinit var mTvBtnProjection: TextView
    private lateinit var mTvBtnCancel: TextView

    fun init() {
        View.inflate(context, R.layout.layout_projection, this)
        mLayoutParent = findViewById(R.id.layout_parent)
        mTvBtnProjection = findViewById<TextView>(R.id.tv_btn_projection)
        mTvBtnCancel = findViewById<TextView>(R.id.tv_btn_cancel)

        mTvBtnProjection.setOnClickListener {
            projectionListener?.onStartProjection()
        }

        mTvBtnCancel.setOnClickListener {
            projectionListener?.onCancelProjection()
        }

        mLayoutParent.visibility = View.GONE
        registerLongClick()
    }

    public fun showProjectionPreUI() {
        mLayoutParent.visibility = View.VISIBLE
        mTvBtnProjection.visibility = View.VISIBLE
    }

    public fun showIsProjectionUI(isProjection: Boolean) {
        if (isProjection) {
            mLayoutParent.visibility = View.VISIBLE
            mTvBtnProjection.visibility = View.GONE
            unRegisterLongClick()
        } else {
            mLayoutParent.visibility = View.GONE
            registerLongClick()
        }
    }

    private fun registerLongClick() {
        setOnLongClickListener {
            showProjectionPreUI()
            return@setOnLongClickListener true
        }
    }

    private fun unRegisterLongClick() {
        setOnClickListener(null)
    }

    public var projectionListener: OnProjectionListener? = null
        set(value) {
            field = value
        }

    interface OnProjectionListener {
        fun onStartProjection()
        fun onCancelProjection()
    }

}