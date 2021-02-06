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
import com.wcq.douyindiandian.application.AppApplication
import com.wcq.douyindiandian.entity.MainDataBean
import com.wcq.douyindiandian.util.showToast
import com.wcq.douyindiandian.view.SuspendButton
import kotlinx.android.synthetic.main.activity_main3.*
import java.util.*

class MainActivity : AppCompatActivity(), OnTouchListener {
    private var mParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null
    private var mFloatingButton: SuspendButton? = null

    private var isFinish: Boolean? = false

    private lateinit var mApplication: AppApplication
    private lateinit var mainDataBean: MainDataBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        mApplication = (application as AppApplication)
        mainDataBean = mApplication.mainDataBean

        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        createSuspend()
        initListener()

    }

    private fun initListener() {
        but_start.setOnClickListener {
            initData()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        but_clear.setOnClickListener {
            mApplication.clearMainData()
            this.showView()
        }
    }

    private fun initData() {
        mainDataBean.isLiveAttention = cb_is_on_live_play_attention.isChecked
        mainDataBean.attentionMixYinLang = et_mix_yin_lang.text.toString().toInt()
        mainDataBean.isCancelAttention = cb_is_cancel_attention.isChecked
        mainDataBean.isOpenCultivateAccount = cb_is_open_cultivate_account.isChecked
        mainDataBean.isBrushHomeRecommend = cb_is_brush_home_recommend.isChecked
        mainDataBean.isBrushCityRecommend = cb_is_brush_city_recommend.isChecked
        mainDataBean.isLiveRandomComment = cb_is_live_random_comment.isChecked
        mainDataBean.seeLiveNum = et_see_live_num.text.toString().toInt()
        mainDataBean.seeLiveTime = et_see_live_time.text.toString().toInt()
        mainDataBean.commentContent = et_comment_content.text.toString()
        mainDataBean.isWatchVideo = cb_is_watch_video.isChecked
        mainDataBean.seeVideoNum = et_video_num.text.toString().toInt()
        mainDataBean.isTopSearch = cb_is_top_search.isChecked
        mainDataBean.topSearchNum = et_top_search_num.text.toString().toInt()
        mainDataBean.isRandomFollow = cb_is_random_follow.isChecked
        mainDataBean.randomFollowNum = et_random_follow_num.text.toString().toInt()
        mApplication.saveMainData()
    }

    override fun onResume() {
        super.onResume()
        showView()
    }

    private fun showView() {
        tv_home_recommend_complete_time.text = "完成${mainDataBean.homeRecommendCompleteTime}分钟"
        tv_city_recommend_complete_time.text = "完成${mainDataBean.cityRecommendCompleteTime}分钟"
        tv_comments_completed_num.text = "当日评论完成个数${mainDataBean.commentCompletedNum}"
        tv_video_completed_num.text = "当日完成个数${mainDataBean.seeVideoCompletedNum}"
        tv_top_search_completed_num.text = "当日完成个数${mainDataBean.topSearchCompletedNum}"
        tv_random_follow_completed_num.text = "当日完成个数${mainDataBean.randomFollowCompletedNum}"
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
        mParams = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.RGBA_8888)
        //设置悬浮窗坐标
        val height = windowManager.defaultDisplay.height
        mParams?.x = 100
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
                initData()
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