package io.agora.rtc.shuangshi.classroom

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class LinearItemDecoration(var space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
//        if (parent.getChildLayoutPosition(view) == 0) {
//            outRect.top = space
//        }
        outRect.bottom = space
    }
}