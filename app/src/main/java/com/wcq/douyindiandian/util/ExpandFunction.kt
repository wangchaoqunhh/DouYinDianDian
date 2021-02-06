package com.wcq.douyindiandian.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.wcq.douyindiandian.BuildConfig.LOG
import java.util.*

fun Context.showToast(content: String?) {
    Toast.makeText(this, content, Toast.LENGTH_LONG).show()
}

fun Context.showLoge(tag: String?, msg: String?) {
    if (LOG) {
        Log.e(tag, msg)
    }
}

fun Context.eventSchedule(backData: (Timer) -> Unit) {
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            backData.invoke(timer)
        }
    }, 0, 500)
}