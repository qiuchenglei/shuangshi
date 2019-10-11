package io.agora.rtc.shuangshi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import io.agora.rtc.lib.rtm.RtmManager
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.lib.util.AppUtil
import io.agora.rtc.lib.util.ToastUtil
import io.agora.rtc.shuangshi.base.BaseActivity
import io.agora.rtc.shuangshi.classroom.student.ClassRoomStudentActivity
import io.agora.rtc.shuangshi.classroom.teacher.ClassRoomTeacherActivity
import io.agora.rtc.shuangshi.constant.IntentKey
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtm.RtmMessage

class MainActivity : BaseActivity(), View.OnClickListener {
    lateinit var ivSetting: ImageView
    lateinit var ivMiniMize: ImageView
    lateinit var ivClose: ImageView
    lateinit var edtRoomName: EditText
    lateinit var edtUserName: EditText
    lateinit var ivRadioBtnTeacher: ImageView
    lateinit var tvRadioBtnTeacher: TextView
    lateinit var ivRadioBtnStudent: ImageView
    lateinit var tvRadioBtnStudent: TextView
    lateinit var tvBtnJoin: TextView
    private val permissionCode = 1001
    private var role: Role = Role.STUDENT

    private lateinit var mLayoutLoading: FrameLayout

    private var userId = 0

    override fun initUI(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        ivSetting = findViewById(R.id.iv_icon_setting)
        ivMiniMize = findViewById(R.id.iv_icon_minimize)
        ivClose = findViewById(R.id.iv_icon_close)
        edtRoomName = findViewById(R.id.edt_room_name)
        edtUserName = findViewById(R.id.edt_user_name)
        ivRadioBtnTeacher = findViewById(R.id.iv_radio_btn_teacher)
        tvRadioBtnTeacher = findViewById(R.id.tv_radio_btn_teacher)
        ivRadioBtnStudent = findViewById(R.id.iv_radio_btn_student)
        tvRadioBtnStudent = findViewById(R.id.tv_radio_btn_student)
        tvBtnJoin = findViewById(R.id.tv_btn_join)
        mLayoutLoading = findViewById(R.id.layout_loading)

        ivSetting.setOnClickListener(this)
        ivMiniMize.setOnClickListener(this)
        ivClose.setOnClickListener(this)
        ivRadioBtnTeacher.setOnClickListener(this)
        tvRadioBtnTeacher.setOnClickListener(this)
        ivRadioBtnStudent.setOnClickListener(this)
        tvRadioBtnStudent.setOnClickListener(this)
        tvBtnJoin.setOnClickListener(this)

        selectToStudent()
    }

    override fun initData() {
        AGApplication.the().initSpUtil()
        AGApplication.the().initWorkerThread()
        AGApplication.the().initRtmManager()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_icon_setting -> {
                SettingFragmentDialog().show(supportFragmentManager, "dialog_setting", null)
            }
            R.id.iv_icon_minimize -> moveTaskToBack(false)
            R.id.iv_icon_close -> finish()
            R.id.iv_radio_btn_teacher, R.id.tv_radio_btn_teacher -> selectToTeacher()
            R.id.iv_radio_btn_student, R.id.tv_radio_btn_student -> selectToStudent()
            R.id.tv_btn_join -> {
                val permission = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (AppUtil.checkAndRequestAppPermission(this, permission, permissionCode)) {
                    joinClassRoom()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != permissionCode)
            return

        var isAllGranted = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED)
                isAllGranted = false
        }
        if (isAllGranted)
            joinClassRoom()
        else
            ToastUtil.showShort(R.string.No_enough_permissions)
    }

    val rtmListener = object : RtmManager.MyRtmClientListener{
        override fun onTokenExpired() {
        }

        override fun onLoginStatusChanged(loginStatus: Int) {
            if (loginStatus == RtmManager.LOGIN_STATUS_SUCCESS) {
                runOnUiThread {
                    mLayoutLoading.visibility = View.GONE
                    val roomNameStr = edtRoomName.text.toString().trim()
                    if (TextUtils.isEmpty(roomNameStr)) {
                        ToastUtil.showShort(R.string.Class_room_name_can_not_be_empty)
                        return@runOnUiThread
                    }
                    val userNameStr = edtUserName.text.toString().trim()
                    if (TextUtils.isEmpty(userNameStr)) {
                        ToastUtil.showShort(R.string.User_name_can_not_be_empty)
                        return@runOnUiThread
                    }
                    val intent = Intent()
                    if (role == Role.TEACHER) {
                        intent.setClass(this@MainActivity, ClassRoomTeacherActivity::class.java)
                    } else {
                        intent.setClass(this@MainActivity, ClassRoomStudentActivity::class.java)
                    }
                    intent.putExtra(IntentKey.INTENT_KEY_ROOM_NAME, roomNameStr)
                        .putExtra(IntentKey.INTENT_KEY_USER_NAME, userNameStr)
                        .putExtra(IntentKey.INTENT_KEY_USER_ID, userId)
                    startActivity(intent)

                    rtmManager().unregisterListener(this)
                }
            } else if (loginStatus == RtmManager.LOGIN_STATUS_FAILURE) {
                runOnUiThread {
                    mLayoutLoading.visibility = View.GONE
                    ToastUtil.showShort("登录失败，请检查网络！")
                }
            }
        }

        override fun onConnectionStateChanged(p0: Int, p1: Int) {
        }

        override fun onMessageReceived(p0: RtmMessage?, p1: String?) {
        }

    }

    private fun joinClassRoom() {
        userId = Math.abs(System.nanoTime().toInt())
        rtmManager().registerListener(rtmListener)
        rtmManager().login(userId.toString())
        mLayoutLoading.visibility = View.VISIBLE
    }

    private fun selectToStudent() {
        role = Role.STUDENT
        ivRadioBtnStudent.isSelected = true
        tvRadioBtnStudent.isSelected = true
        ivRadioBtnTeacher.isSelected = false
        tvRadioBtnTeacher.isSelected = false
    }

    private fun selectToTeacher() {
        role = Role.TEACHER
        ivRadioBtnStudent.isSelected = false
        tvRadioBtnStudent.isSelected = false
        ivRadioBtnTeacher.isSelected = true
        tvRadioBtnTeacher.isSelected = true
    }

}
