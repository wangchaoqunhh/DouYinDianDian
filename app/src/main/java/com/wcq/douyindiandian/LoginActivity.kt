package com.wcq.douyindiandian

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.QueryListener
import cn.bmob.v3.listener.UpdateListener
import com.wcq.douyindiandian.constants.Constant.isUsable
import com.wcq.douyindiandian.entity.DYUser
import com.wcq.douyindiandian.util.TimeUtil.*
import com.wcq.douyindiandian.util.showToast
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.reflect.Method
import java.util.*


class LoginActivity : AppCompatActivity() {
    var mContext: Context? = null

    lateinit var dialog: ZLoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dialog = ZLoadingDialog(this)
        dialog.setLoadingBuilder(Z_TYPE.STAR_LOADING) //设置类型
                .setLoadingColor(Color.BLACK) //颜色
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setHintText("Loading...")

        mContext = this
        val sp = getSharedPreferences("login_code", Context.MODE_PRIVATE)
        val loginCode = sp.getString("login_code", "")
        if (!TextUtils.isEmpty(loginCode)) {
            et_login_code.setText(loginCode)
        }

        but_login.setOnClickListener {
            //获取登录码
            val etLoginCode: String? = et_login_code.text.toString()
            if (TextUtils.isEmpty(etLoginCode)) {
                showToast("请输入登陆码")
                return@setOnClickListener
            }

            dialog.show()
            val dYUser: BmobQuery<DYUser>? = BmobQuery()
            dYUser?.getObject(etLoginCode, object : QueryListener<DYUser>() {
                override fun done(user: DYUser?, e: BmobException?) {
                    if (e == null) {
                        //说明此注册码 没被注册
                        if (user != null && TextUtils.isEmpty(user.meid) && !user.isUsable) {
                            if (TextUtils.isEmpty(user.sellTime)) {
                                //如果出售时间为空 说明他第一登录 获取当前时间为出售时间
                                getCurrentTime(this@LoginActivity,object :TimeBack{
                                    override fun onTimeBack(date: Date?) {
                                        updateUser(etLoginCode, timestampToYYYY(dateToTimestamp(date)))
                                    }

                                    override fun onFailed() {
                                        showToast("请重新登陆")
                                        dialog.dismiss()
                                    }
                                })
                            } else {
                                //如果出售时间不为空 他是修改 出售时间不能动
                                updateUser(etLoginCode, "")
                            }
                        } else if (user != null && user.isUsable) {
                            //说明此注册码 被注册过
                            if (user.meid == getIMEINew()) {
                                loginUser(user)
                            } else {
                                //是否限制手机 不限制手机直接登录
                                if (user.isNOLimitPhone) {
                                    loginUser(user)
                                } else {
                                    //换新手机了 不能登录
                                    Toast.makeText(mContext, "此注册码已被用过,请用原来手机登录", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } else {
                        dialog.dismiss()
                        Toast.makeText(mContext, "请输入正确的登录码", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    /**
     * 第一次登录 修改user
     *
     * @param etLoginCode 登录码
     * @param sellTime 当前时间 也就是出售时间 yyyy-MM-dd HH:mm:ss
     */
    private fun updateUser(etLoginCode: String?, sellTime: String?) {
        val du = DYUser()
        du.meid = getIMEINew()
        du.isUsable = true
        if (!TextUtils.isEmpty(sellTime)) {
            du.sellTime = sellTime
            du.sellDay = 30
            du.isNOLimitPhone = false
        }
        du.update(etLoginCode, object : UpdateListener() {
            override fun done(e: BmobException) {
                dialog.dismiss()
                if (e != null && e.errorCode == 9015) {
                    //保存 sp
                    val edit = getSharedPreferences("login_code", Context.MODE_PRIVATE).edit()
                    edit.putString("login_code", etLoginCode)
                    edit.commit()
                    Toast.makeText(mContext, "登陆成功", Toast.LENGTH_LONG).show()
                    isUsable = true
                    MainActivity.launchActivity(mContext)
                    finish()
                } else {
                    Toast.makeText(mContext, "登陆失败", Toast.LENGTH_LONG).show()
                }
            }
        })
    }


    /**
     * 登录user
     */
    private fun loginUser(user: DYUser) {
        //到期时间
        val laseDay = dateLaterDay(user.sellTime, user.sellDay)
        getCurrentTime(this@LoginActivity,object :TimeBack{
            override fun onTimeBack(date: Date?) {
                dialog.dismiss()
                //如果当前时间大于 到期时间  说明过期了
                if (dateToTimestamp(date) >= YYYYToTimestamp(laseDay)) {
                    showToast("你的登录码已经过期了")
                } else {
                    //是原来的手机登录的  可以登录
                    Toast.makeText(mContext, "登陆成功", Toast.LENGTH_LONG).show()
                    isUsable = user.isUsable
                    MainActivity.launchActivity(mContext)
                    finish()
                }
            }

            override fun onFailed() {
                showToast("请重新登陆")
                dialog.dismiss()
            }

        })
    }

    private fun getIMEI1(): String? {
        //实例化TelephonyManager对象
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var method: Method? = null
        try {
            method = telephonyManager.javaClass.getMethod("getDeviceId", Int::class.javaPrimitiveType)
            //获取IMEI号
            @SuppressLint("MissingPermission") val meid = telephonyManager.deviceId
            //获取MEID号
            val imei1 = method.invoke(telephonyManager, 2) as String
            val imei2 = method.invoke(telephonyManager, 1) as String
            Log.e("手机唯一", "MEID：$meid")
            Log.e("手机唯一", "IMEI1：$imei1")
            Log.e("手机唯一", "IMEI2：$imei2")
            return imei1
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    /**
     * Pseudo-Unique ID, 这个在任何Android手机中都有效 解决手机中IMEI获取不到情况，兼容所有手机
     */
    fun getIMEINew(): String? {
        //we make this look like a valid IMEI
        return "35" +
                Build.BOARD.length % 10 +
                Build.BRAND.length % 10 +
                Build.CPU_ABI.length % 10 +
                Build.DEVICE.length % 10 +
                Build.DISPLAY.length % 10 +
                Build.HOST.length % 10 +
                Build.ID.length % 10 +
                Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 +
                Build.PRODUCT.length % 10 +
                Build.TAGS.length % 10 +
                Build.TYPE.length % 10 +
                Build.USER.length % 10
    }
}