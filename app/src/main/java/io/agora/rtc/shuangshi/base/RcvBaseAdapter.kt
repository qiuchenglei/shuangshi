package io.agora.rtc.shuangshi.base

import android.support.v7.widget.RecyclerView

import java.util.ArrayList

abstract class RcvBaseAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    var mList: MutableList<T> = mutableListOf()
        set(list) {
            field = list
        }

    fun addList(list: MutableList<T>) {
        mList.addAll(list)
    }

    fun updateItem(t: T) {
        for (i in mList.indices) {
            if (isEqual(mList[i], t)) {
                mList[i] = t
                notifyItemChanged(i, t)
            }
        }
    }

    open fun isEqual(old: T, new: T): Boolean {
        return false
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

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, position, mList!![position], mutableListOf())
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position, mList!![position], payloads)
    }

    abstract fun onBindViewHolder(
        viewHolder: VH,
        position: Int,
        bean: T,
        payloads: MutableList<Any>
    )
}
