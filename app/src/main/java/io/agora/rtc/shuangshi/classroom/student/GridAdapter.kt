package io.agora.rtc.shuangshi.classroom.student

import android.content.Context
import android.view.ViewGroup
import io.agora.rtc.lib.util.DensityUtil

class GridAdapter(mPresenter: StudentPresenter, myUserId: Int) : MembersAdapter(mPresenter, myUserId) {
    override fun getItemWidth(ctx: Context):Int {
        if (itemCount < 2) {
            return DensityUtil.getScreenWidth(ctx)
        } else {
            return DensityUtil.getScreenWidth(ctx) / 2
        }
    }

    override fun getItemHeight(ctx: Context):Int {
        return ViewGroup.LayoutParams.MATCH_PARENT
    }
}