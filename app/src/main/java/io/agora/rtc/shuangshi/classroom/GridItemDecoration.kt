package io.agora.rtc.shuangshi.classroom

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class GridItemDecoration(var space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        when(parent.adapter?.itemCount) {
            2 -> {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.right = space
                }
            }
            3 -> {
                when(parent.getChildAdapterPosition(view)) {
                    0 -> outRect.right = space
                    1 -> outRect.bottom = space
                }
            }
            4-> {
                when(parent.getChildAdapterPosition(view)) {
                    0 -> {
                        outRect.right = space
                        outRect.bottom = space
                    }
                    1 -> outRect.right = space
                    2 -> outRect.bottom = space
                }
            }
        }
    }
}