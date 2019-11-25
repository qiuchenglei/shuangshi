package io.agora.rtc.shuangshi.widget.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import io.agora.rtc.shuangshi.R


class MyDialogFragment : DialogFragment() {

    private var content: String? = null
    private var listener: DialogClickListener? = null
    private var resIntText = 0

    override fun onCancel(dialog: DialogInterface?) {
        listener!!.clickNo()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity!!, theme)
        isCancelable = true
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.dialog_my, container, false)
        root.findViewById<View>(R.id.tv_btn_confirm).setOnClickListener {
            listener!!.clickYes()
            dismiss()
        }
        root.findViewById<View>(R.id.tv_btn_cancel).setOnClickListener {
            listener!!.clickNo()
            dismiss()
        }
        val tvContent = root.findViewById<TextView>(R.id.tv_content)
        if (resIntText != 0) {
            tvContent.setText(resIntText)
        } else {
            tvContent.text = content
        }
        return root
    }

    interface DialogClickListener {
        fun clickYes()

        fun clickNo()
    }

    companion object {

        fun newInstance(listener: DialogClickListener, content: String): MyDialogFragment {
            val fragment = MyDialogFragment()
            fragment.content = content
            fragment.listener = listener
            return fragment
        }

        fun newInstance(listener: DialogClickListener, resInt: Int): MyDialogFragment {
            val fragment = MyDialogFragment()
            fragment.resIntText = resInt
            fragment.listener = listener
            return fragment
        }
    }
}
