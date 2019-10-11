package io.agora.rtc.shuangshi.classroom.student

import android.view.SurfaceView
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtc.shuangshi.widget.dialog.MyDialogFragment

interface StudentView {
    fun showDialog(resStr:Int, listener : MyDialogFragment.DialogClickListener, tag:String = "dialog")
    fun showInClass(inClass: Boolean)
    fun getTeacherViewMax(): SurfaceView
    fun updateTimer(strForTime: String)
    fun showTeacherMax(isInClass: Boolean, teacherAttr: Member, isShowMin: Boolean)
    fun dismissTeacherMax()
    fun showStudents(students: MutableList<Member>)
    fun dismissStudents()
    fun showAllMembers(allMembers: MutableList<Member>)
    fun dismissAllMembers()
    fun finish()
    fun showSettingDialog(settingListener: SettingFragmentDialog.SettingListener? = null)
}