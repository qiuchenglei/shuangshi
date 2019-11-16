package io.agora.rtc.shuangshi.classroom.student

import android.content.Context
import android.view.ViewGroup
import io.agora.rtc.lib.util.DensityUtil
import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.classroom.student.StudentPresenter

class LinearAdapter(mPresenter: StudentPresenter, myUserId: Int) :
    MembersAdapter(mPresenter, myUserId) {
    override fun getItemWidth(ctx: Context): Int {
        return ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun getItemHeight(ctx: Context): Int {
        return ctx.resources.getDimensionPixelSize(R.dimen.dp_84)
    }
}