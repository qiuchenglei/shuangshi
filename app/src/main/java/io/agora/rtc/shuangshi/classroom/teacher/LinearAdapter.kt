package io.agora.rtc.shuangshi.classroom.teacher

import android.content.Context
import android.view.ViewGroup
import io.agora.rtc.lib.util.DensityUtil
import io.agora.rtc.shuangshi.R

class LinearAdapter(mPresenter: TeacherPresenter) : MembersAdapter(mPresenter) {
    override fun getItemWidth(ctx: Context): Int {
        return ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun getItemHeight(ctx: Context): Int {
        return ctx.resources.getDimensionPixelSize(R.dimen.dp_84)
    }
}