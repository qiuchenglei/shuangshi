package io.agora.rtc.shuangshi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.agora.rtc.lib.rtm.RtmManager
import io.agora.rtc.shuangshi.constant.Role
import io.agora.rtc.lib.util.AppUtil
import io.agora.rtc.lib.util.LogUtil
import io.agora.rtc.lib.util.SPUtil
import io.agora.rtc.lib.util.ToastUtil
import io.agora.rtc.shuangshi.base.BaseActivity
import io.agora.rtc.shuangshi.classroom.Member
import io.agora.rtc.shuangshi.classroom.student.ClassRoomStudentActivity
import io.agora.rtc.shuangshi.classroom.teacher.ClassRoomTeacherActivity
import io.agora.rtc.shuangshi.constant.IntentKey
import io.agora.rtc.shuangshi.constant.SPKey
import io.agora.rtc.shuangshi.setting.SettingFragmentDialog
import io.agora.rtm.ErrorInfo
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmChannelAttribute
import kotlin.math.absoluteValue

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

    private var roomName = ""
    private var userName = ""
    private var role: Role = Role.STUDENT

    private lateinit var mLayoutLoading: FrameLayout

    private var userId = 0
    private val log = LogUtil("MainActvity")

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

    override fun onStart() {
        super.onStart()
        mLayoutLoading.visibility = View.GONE
    }

    override fun initData() {
        AGApplication.the().initSpUtil()
        AGApplication.the().initWorkerThread()
        AGApplication.the().initRtmManager()

        checkSystemAlertPermission()
        userId = SPUtil.get(SPKey.MY_USER_ID, 0)
        if (userId < 1) {
            userId = System.nanoTime().toInt().absoluteValue
            SPUtil.put(SPKey.MY_USER_ID, userId)
        }

        log.i("login with userId: $userId")
        rtmManager().login(userId.toString())
    }

    private fun checkSystemAlertPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //SYSTEM_ALERT_WINDOW权限申请
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")//不加会显示所有可能的app
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivityForResult(intent, 1)
                return false
            } else {
                return true
            }
        } else {
            return true
        }
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
                    clickJoin()
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
            clickJoin()
        else
            ToastUtil.showShort(R.string.No_enough_permissions)
    }

    fun getChannelAttr() {
        rtmManager().rtmClient.getChannelAttributes(
            roomName,
            object : ResultCallback<MutableList<RtmChannelAttribute>> {
                override fun onSuccess(p0: MutableList<RtmChannelAttribute>?) {
                    edtUserName.postDelayed({ joinRoom(p0) }, 800)
                }

                override fun onFailure(p0: ErrorInfo?) {
                    runOnUiThread {
                        joinRoom(null)
                    }
                }

            })
    }

    val rtmListener = object : RtmManager.MyRtmClientListener() {
        override fun onLoginStatusChanged(loginStatus: Int) {
            if (loginStatus == RtmManager.LOGIN_STATUS_SUCCESS) {
                rtmManager().unregisterListener(this)
                runOnUiThread {
                    //                    if (role == Role.TEACHER) {
//                        getChannelAttr()
//                    } else {
                    joinRoom(null)
//                    }
                }
            } else if (loginStatus == RtmManager.LOGIN_STATUS_FAILURE) {
                runOnUiThread {
                    mLayoutLoading.visibility = View.GONE
                    ToastUtil.showShort("登录失败，请检查网络！")
                }
            }
        }
    }

    private fun joinRoom(p0: MutableList<RtmChannelAttribute>?) {
        if (isFinishing)
            return
        mLayoutLoading.visibility = View.GONE
        if (p0 != null && p0.isNotEmpty()) {
            val gson = Gson()
            for (it in p0) {
                if (!TextUtils.isEmpty(it.key) && !TextUtils.isEmpty(it.value)) {
                    try {
                        val member = gson.fromJson(
                            it.value,
                            Member::class.java
                        )
                        if (member.class_role == Role.TEACHER.intValue()) {
                            if (member.uid != 0 && member.uid != userId) {
                                ToastUtil.showShort(R.string.There_are_another_teacher_in_this_class)
                                return
                            }
                            break
                        }
                    } catch (e: JsonSyntaxException) {
                    }

                }
            }
        }
        val intent = Intent()
        if (role == Role.TEACHER) {
            intent.setClass(this@MainActivity, ClassRoomTeacherActivity::class.java)
        } else {
            intent.setClass(this@MainActivity, ClassRoomStudentActivity::class.java)
        }
        intent.putExtra(IntentKey.INTENT_KEY_ROOM_NAME, roomName)
            .putExtra(IntentKey.INTENT_KEY_USER_NAME, userName)
            .putExtra(IntentKey.INTENT_KEY_USER_ID, userId)
        log.i("join userId:$userId, userName:$userName, channelName:$roomName")
        startActivity(intent)
    }

    private fun clickJoin() {
        roomName = edtRoomName.text.toString().trim()
        if (TextUtils.isEmpty(roomName)) {
            ToastUtil.showShort(R.string.Class_room_name_can_not_be_empty)
            return
        }
        userName = edtUserName.text.toString().trim()
        if (TextUtils.isEmpty(userName)) {
            ToastUtil.showShort(R.string.User_name_can_not_be_empty)
            return
        }

        if (rtmManager().loginStatus != RtmManager.LOGIN_STATUS_SUCCESS) {
            rtmManager().logout()
            rtmManager().registerListener(rtmListener)
            rtmManager().login(userId.toString())
        } else {
//            if (role == Role.TEACHER) {
//                getChannelAttr()
//            } else {
            joinRoom(null)
//            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        rtmManager().logout()
    }

}
