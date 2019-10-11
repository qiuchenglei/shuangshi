package io.agora.rtc.shuangshi.setting

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.*
import io.agora.rtc.shuangshi.R

class SettingListPopupWindow(
    strArray: Array<String>,
    ctx: Context,
    selectedIndex: Int,
    onSelectListener: OnSelectListener
) {
    var mView: PopupWindow
    val itemHeight = ctx.resources.getDimensionPixelSize(R.dimen.dp_13)

    init {
        val layout: View = View.inflate(ctx, R.layout.popup_window_setting_list, null)
        mView = PopupWindow(
            layout,
            ctx.resources.getDimensionPixelSize(R.dimen.dp_75),
            itemHeight * strArray.size
        )
        mView.isFocusable = true
        mView.isOutsideTouchable = true

        val lv: ListView = layout.findViewById(R.id.lv)
        lv.adapter = ListAdapter(strArray, itemHeight, selectedIndex)

        lv.setOnItemClickListener { _, _, position, _ ->
            onSelectListener.onSelect(position)
            mView.dismiss()
        }
    }

    private class ListAdapter(
        val strArray: Array<String>,
        val itemHeight: Int,
        val selectedIndex: Int
    ) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val ctx: Context = parent!!.context
            val tv = TextView(ctx)
            val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(MATCH_PARENT, itemHeight)
            tv.setPadding(size(ctx, R.dimen.dp_8), 0, 0, 0)
            tv.gravity = Gravity.CENTER_VERTICAL
            if (position == selectedIndex) {
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.grey_333333))
            } else {
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.grey_666666))
            }
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.resources.getDimension(R.dimen.dp_6))
            tv.layoutParams = lp
            tv.text = strArray[position]
            return tv
        }

        override fun getItem(position: Int): Any {
            return strArray[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return strArray.size
        }

    }

}

public interface OnSelectListener {
    fun onSelect(index: Int)
}

fun showSettingItems(
    strArray: Array<String>,
    selectedIndex: Int,
    onSelectListener: OnSelectListener,
    view: View,
    xOff: Int = 0,
    yOff: Int = 0
) {
    val settingListPopupWindow =
        SettingListPopupWindow(strArray, view.context, selectedIndex, onSelectListener)
    settingListPopupWindow.mView.showAsDropDown(view, xOff, yOff)
}

fun size(ctx: Context, resInt: Int): Int {
    return ctx.resources.getDimensionPixelSize(resInt)
}