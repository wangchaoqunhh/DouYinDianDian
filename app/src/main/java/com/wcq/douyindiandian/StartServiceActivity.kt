package com.wcq.douyindiandian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class StartServiceActivity : AppCompatActivity() {
    var mContext: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        but_acc.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        but_start.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        but_start_service.setOnClickListener {

        }
    }

    companion object {
        fun launchActivity(context: Context?) {
            val intent = Intent(context, StartServiceActivity::class.java)
            context?.startActivity(intent)
        }
    }
}