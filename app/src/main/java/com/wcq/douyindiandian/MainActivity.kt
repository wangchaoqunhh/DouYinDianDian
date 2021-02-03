package com.wcq.douyindiandian

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.wcq.douyindiandian.constants.Constant.DY.isCancelAttention
import com.wcq.douyindiandian.constants.Constant.DY.isOnLivePlayAttention
import com.wcq.douyindiandian.constants.Constant.mixYinLang
import com.wcq.douyindiandian.util.showToast
import com.wcq.douyindiandian.view.SuspendButton
import kotlinx.android.synthetic.main.activity_main3.*
import java.util.*

class MainActivity : AppCompatActivity(), OnTouchListener {
    private var mParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null
    private var mFloatingButton: SuspendButton? = null

    private var isFinish: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createSuspend()

    }

    override fun onBackPressed() {
        if (isFinish!!) {
            super.onBackPressed()
        } else {
            showToast("再次点击退出")
        }
        isFinish = true
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                isFinish = false
                timer.cancel()
            }
        }
        timer.schedule(task, 2000)
    }

    private fun requestWindowPermission() {
        //android 6.0或者之后的版本需要发一个intent让用户授权
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(applicationContext)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                startActivityForResult(intent, 100)
            }
        }
    }

    /**
     *创建悬浮窗
     */
    private fun createSuspend() {
        //设置允许弹出悬浮窗口的权限
        requestWindowPermission()
        //创建窗口布局参数
        mParams = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT)
        //设置悬浮窗坐标
        val height = windowManager.defaultDisplay.height
//        mParams?.x = 100
        mParams?.y = height / 2
        //表示该Window无需获取焦点，也不需要接收输入事件
        mParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mParams?.gravity = Gravity.LEFT or Gravity.TOP
        Log.d("MainActivity", "sdk:" + Build.VERSION.SDK_INT)

        //设置window 类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //API Level 26
            mParams?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mParams?.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        //创建悬浮窗(其实就创建了一个Button,这里也可以创建其他类型的控件)
        if (null == mFloatingButton) {
            mFloatingButton = SuspendButton(this)
            mFloatingButton?.setOnTouchListener(this)
            mWindowManager?.addView(mFloatingButton, mParams)

            mFloatingButton?.button?.setOnClickListener {
                isOnLivePlayAttention = cb_is_on_live_play_attention.isChecked
                mixYinLang = et_mix_yin_lang.text.toString().toInt()
                isCancelAttention = cb_is_cancel_attention.isChecked

                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        var isTouch = false
        val rawX = event.rawX.toInt()
        val rawY = event.rawY.toInt()
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                mParams?.x = rawX
                mParams?.y = rawY
                Log.e("x和y", "$rawX---$rawY")
                mWindowManager?.updateViewLayout(mFloatingButton, mParams)
                isTouch = false
            }
        }
        return isTouch
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mFloatingButton) {
            mWindowManager?.removeView(mFloatingButton)
        }
    }

    companion object {
        fun launchActivity(context: Context?) {
            val intent = Intent(context, MainActivity::class.java)
            context?.startActivity(intent)
        }
    }
}