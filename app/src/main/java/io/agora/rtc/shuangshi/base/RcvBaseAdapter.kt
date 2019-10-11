package io.agora.rtc.shuangshi.base

import android.support.v7.widget.RecyclerView

import java.util.ArrayList

abstract class RcvBaseAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    protected var mList: MutableList<T>? = null

    var list: MutableList<T>?
        get() = mList
        set(list) {
            mList = list
        }

    fun addList(list: MutableList<T>) {
        if (mList == null)
            mList = list
        else
            mList!!.addAll(list)
    }

    fun updateItemById(id: String?, item: T) {
        if (mList == null || id == null) {
            return
        }
        for (i in mList!!.indices) {
            if (id == getItemStringId(i)) {
                mList!![i] = item
                notifyItemChanged(i)
            }
        }
    }

    protected fun getItemStringId(position: Int): String? {
        return null
    }

    fun addItem(item: T?) {
        if (item == null)
            return
        if (mList == null)
            mList = ArrayList()

        mList!!.add(item)
    }

    override fun getItemCount(): Int {
        return if (mList == null) 0 else mList!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItem(position: Int): T {
        return mList!![position]
    }

    override fun onBindViewHolder(p0: VH, p1: Int) {
        onBindViewHolder(p0, p1, mList!![p1])
    }

    abstract fun onBindViewHolder(viewHolder: VH, position: Int, bean: T)
}
