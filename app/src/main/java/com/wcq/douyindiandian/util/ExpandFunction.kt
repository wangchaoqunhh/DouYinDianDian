package com.wcq.douyindiandian.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.wcq.douyindiandian.BuildConfig.LOG
import java.util.*

fun Context.showToast(content: String?) {
    Toast.makeText(this, content, Toast.LENGTH_LONG).show()
}

fun Context.showLoge(tag: String, msgs: String?) {
    if (LOG) {
//        Log.e(tag, msg)
        var msg = msgs ?: ""
        if (tag == null || tag.length === 0 || msg == null || msg.length === 0) return

        val segmentSize = 3 * 1024
        val length: Int = msg.length
        if (length <= segmentSize) { // 长度小于等于限制直接打印
            Log.e(tag, msg)
        } else {
            while (msg.length > segmentSize) { // 循环分段打印日志
                val logContent = msg.substring(0, segmentSize)
                msg = msg.replace(logContent, "")
                Log.e(tag, logContent)
            }
            Log.e(tag, msg) // 打印剩余日志
        }
    }
}


class NodeInfoBean2(var className: String? = null, var text: String? = null, var childes: List<NodeInfoBean2>? = null)

fun getAllNodeInfo(nodeInfo: AccessibilityNodeInfo?, childes: ArrayList<NodeInfoBean2>?) {
    if (nodeInfo != null && !TextUtils.isEmpty(nodeInfo.className.toString())) {
        val nodeInfoBean = NodeInfoBean2(className = nodeInfo.className?.toString(), text = nodeInfo.text?.toString())
        childes?.add(nodeInfoBean)
        val childCount = nodeInfo.childCount
        if (childCount > 0) {
            val childes = arrayListOf<NodeInfoBean2>()
            for (i in 0 until childCount) {
                val child = nodeInfo.getChild(i)
                getAllNodeInfo(child, childes)
            }
            nodeInfoBean.childes = childes
            nodeInfo.recycle()
        } else {
            nodeInfo.recycle()
        }
    }
}