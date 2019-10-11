package io.agora.rtc.shuangshi.setting


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import io.agora.rtc.shuangshi.R

class LayoutFragment : Fragment() {
    val layoutSettingPresenter = LayoutSettingPresenter(this)

    private lateinit var mTvOneVNSelect: TextView
    private lateinit var mTvLayoutSelect: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_layout, container, false)
        mTvOneVNSelect = root.findViewById(R.id.tv_one_v_n_select)
        mTvLayoutSelect = root.findViewById(R.id.tv_layout_select)

        mTvOneVNSelect.setOnClickListener { layoutSettingPresenter.onClickOneVNSelect() }
        mTvLayoutSelect.setOnClickListener { layoutSettingPresenter.onClickLayoutSelect() }
        return root
    }


    public fun apply() {
        layoutSettingPresenter.apply()
    }

}
