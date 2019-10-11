package io.agora.rtc.shuangshi.view

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.LinearLayout

class CheckableLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), Checkable {
    private var isChecked: Boolean = false
    override fun isChecked(): Boolean {
        return isChecked
    }

    override fun toggle() {
        setChecked(!isChecked)
    }

    override fun setChecked(checked: Boolean) {
        isChecked = checked
        val count = childCount
        isSelected = checked
        if (count > 0) {
            for (i in 0..count) {
                val view = getChildAt(i)
                if (view != null)
                    view.isSelected = checked
            }
        }
    }

}