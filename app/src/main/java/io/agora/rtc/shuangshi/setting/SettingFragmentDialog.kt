package io.agora.rtc.shuangshi.setting

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import io.agora.rtc.shuangshi.R
import io.agora.rtc.shuangshi.view.CheckableLinearLayout

class SettingFragmentDialog : DialogFragment() {
    interface SettingListener {
        fun onApply()

        fun onCancel()
    }
    private var settingListener:SettingListener? = null


    fun show(manager: FragmentManager?, tag: String?, settingListener: SettingListener?) {
        super.show(manager, tag)
        this.settingListener = settingListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity!!, theme)
        isCancelable = false
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.dialog_setting, container, false)
        initUI(root)
        initFragment()
        selectTabToAudio()
        return root
    }

    override fun onResume() {
        super.onResume()

        dialog.window!!.setLayout(resources.getDimensionPixelSize(R.dimen.dp_370), resources.getDimensionPixelSize(R.dimen.dp_264))
    }

    private lateinit var audioFragment: AudioFragment
    private lateinit var videoFragment: VideoFragment
    private lateinit var layoutFragment: LayoutFragment

    private fun initFragment() {
        audioFragment = AudioFragment()
        videoFragment = VideoFragment()
        layoutFragment = LayoutFragment()

        childFragmentManager.beginTransaction()
            .add(R.id.fl_content, audioFragment)
            .add(R.id.fl_content, videoFragment)
            .add(R.id.fl_content, layoutFragment)
            .commit()
    }

    private fun selectTabToAudio() {
        audioTab.isChecked = true
        videoTab.isChecked = false
        layoutTab.isChecked = false
        childFragmentManager.beginTransaction()
            .hide(videoFragment)
            .hide(layoutFragment)
            .show(audioFragment)
            .commit()
    }

    private fun selectTabToVideo() {
        videoTab.isChecked = true
        audioTab.isChecked = false
        layoutTab.isChecked = false
        childFragmentManager.beginTransaction()
            .hide(audioFragment)
            .hide(layoutFragment)
            .show(videoFragment)
            .commit()
    }

    private fun selectTabToLayout() {
        layoutTab.isChecked = true
        videoTab.isChecked = false
        audioTab.isChecked = false
        childFragmentManager.beginTransaction()
            .hide(audioFragment)
            .hide(videoFragment)
            .show(layoutFragment)
            .commit()
    }

    private lateinit var audioTab: CheckableLinearLayout
    private lateinit var videoTab: CheckableLinearLayout
    private lateinit var layoutTab: CheckableLinearLayout
    private lateinit var ivBtnClose: ImageView
    private lateinit var tvBtnApply: TextView
    private lateinit var tvBtnCancel: TextView
    private lateinit var layoutContent: FrameLayout

    private fun initUI(root: View) {
        audioTab = root.findViewById(R.id.setting_tab_audio)
        videoTab = root.findViewById(R.id.setting_tab_video)
        layoutTab = root.findViewById(R.id.setting_tab_layout)
        ivBtnClose = root.findViewById(R.id.iv_icon_close)
        tvBtnApply = root.findViewById(R.id.tv_btn_apply)
        tvBtnCancel = root.findViewById(R.id.tv_btn_cancel)
        layoutContent = root.findViewById(R.id.fl_content)

        audioTab.setOnClickListener { selectTabToAudio() }
        videoTab.setOnClickListener { selectTabToVideo() }
        layoutTab.setOnClickListener { selectTabToLayout() }
        ivBtnClose.setOnClickListener { clickCancel() }
        tvBtnCancel.setOnClickListener { clickCancel() }
        tvBtnApply.setOnClickListener { clickApply() }
    }

    private fun clickApply() {
        audioFragment.apply()
        videoFragment.apply()
        layoutFragment.apply()

        dismiss()
        settingListener?.onApply()
    }

    private fun clickCancel() {
        dismiss()
        settingListener?.onCancel()
    }

}
