package io.agora.rtc.shuangshi.classroom.teacher

import android.view.SurfaceView
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtc.shuangshi.widget.dialog.MyDialogFragment

interface TeacherView {
    fun showDialog(resStr:Int, listener : MyDialogFragment.DialogClickListener, tag:String = "dialog")
    fun showToast(text: String)
    fun showInClass(inClass: Boolean)
    fun getTeacherViewMax(): SurfaceView
    fun updateTimer(strForTime: String)
    fun showTeacherMax(isInClass: Boolean, teacherAttr: Member)
    fun dismissTeacherMax()
    fun showStudents(students: MutableList<Member>)
    fun dismissStudents()
    fun showAllMembers(allMembers: MutableList<Member>)
    fun dismissAllMembers()
    fun startShare(callback: ShareScreenActivity.StartShareCallback? = null)
    fun stopShare()
    fun finish()
    fun showSettingDialog(settingListener: SettingFragmentDialog.SettingListener? = null)
    fun onPartChanged(changeList: MutableList<Member>)
}