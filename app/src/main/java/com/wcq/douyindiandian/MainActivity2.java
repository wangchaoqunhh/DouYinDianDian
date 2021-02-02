package com.wcq.douyindiandian;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.wcq.douyindiandian.entity.DYUser;
import com.wcq.douyindiandian.util.SntpClient;
import com.wcq.douyindiandian.view.SuspendButton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private SuspendButton mFloatingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button but_save = findViewById(R.id.but_save);
        but_save.setOnClickListener(v -> {
            DYUser dyUser = new DYUser();
            dyUser.setUsable(false);
            dyUser.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if (e == null) {
                        Toast.makeText(MainActivity2.this, "添加数据成功" + s, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity2.this, "创建数据失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        Button but_meid = findViewById(R.id.but_meid);
        but_meid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImei1();
            }
        });

        findViewById(R.id.but_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        SntpClient sntpClient = new SntpClient();
                        if (sntpClient.requestTime("cn.pool.ntp.org", 30000)) {
                            long now = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
                            Date current = new Date(now);
                            Log.d("当前时间", current.toString());
                        }
                    }
                }.start();
            }
        });

        Timer timer = new Timer();
        Log.e("timer", "timer:" + timer);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Log.e("timer", "5秒后执行");
                Log.e("timer", "timer:" + timer);
                timer.cancel();
                Log.e("timer", "timer:" + timer);
            }
        };
        timer.schedule(task, 5000);

        findViewById(R.id.add).setOnClickListener(this);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        findViewById(R.id.open).setOnClickListener(v -> {
            Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.wcq.douyindiandian.service/MyAccessibilityService");
            Settings.Secure.putString(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "1");
        });

        findViewById(R.id.close).setOnClickListener(v -> {
            Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.wcq.douyindiandian.service/MyAccessibilityService");
            Settings.Secure.putString(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "0");
        });
    }


    /**
     * 这个方法一定可以获取到
     *
     * @param slotId slotId为卡槽Id，它的值为 0、1；
     * @return
     */
    @SuppressLint({"MissingPermission", "NewApi"})
    public String getIMEI(Context context, int slotId) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method method = manager.getClass().getMethod("getImei", int.class);
//            String imei = (String) method.invoke(manager, slotId);
            @SuppressLint("MissingPermission")
            String imei = manager.getDeviceId();

            return manager.getMeid();
        } catch (Exception e) {
            return "";
        }
    }

    private String getImei1() {
        //实例化TelephonyManager对象
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Method method = null;
        try {
            method = telephonyManager.getClass().getMethod("getDeviceId", int.class);
            //获取IMEI号
            @SuppressLint("MissingPermission")
            String meid = telephonyManager.getDeviceId();
            //获取MEID号
            String imei1 = (String) method.invoke(telephonyManager, 2);
            String imei2 = (String) method.invoke(telephonyManager, 1);

            Log.e("手机唯一", "MEID：" + meid);
            Log.e("手机唯一", "IMEI1：" + imei1);
            Log.e("手机唯一", "IMEI2：" + imei2);
            return imei1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getMEID2() {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);

            String meid = (String) method.invoke(null, "ril.cdma.meid", "");
            if (!TextUtils.isEmpty(meid)) {
                Log.d("手机", "getMEID meid: " + meid);
                return meid;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.w("手机", "getMEID error : " + e.getMessage());
        }
        return "";
    }

    private void requestWindowPermission() {
        //android 6.0或者之后的版本需要发一个intent让用户授权
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 100);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add) {
            //设置允许弹出悬浮窗口的权限
            requestWindowPermission();
            //创建窗口布局参数
            mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
            //设置悬浮窗坐标
            mParams.x = 100;
            mParams.y = 100;
            //表示该Window无需获取焦点，也不需要接收输入事件
            mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mParams.gravity = Gravity.LEFT | Gravity.TOP;
            Log.d("MainActivity", "sdk:" + Build.VERSION.SDK_INT);
            //设置window 类型
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//API Level 26
                mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }
            //创建悬浮窗(其实就创建了一个Button,这里也可以创建其他类型的控件)
            if (null == mFloatingButton) {
                mFloatingButton = new SuspendButton(this);
                mFloatingButton.setOnTouchListener(this);
//                mFloatingButton.setOnClickListener(this);
                mWindowManager.addView(mFloatingButton, mParams);
            }
        }
//        else if(v.getId()==R.id.remove){
//            if(null != mFloatingButton){
//                mWindowManager.removeView(mFloatingButton);
//                mFloatingButton=null;
//            }
//        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean isTouch = false;
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mParams.x = rawX;
                mParams.y = rawY;
                Log.e("x和y", rawX + "---" + rawY);
                mWindowManager.updateViewLayout(mFloatingButton, mParams);
                isTouch = false;
                break;
        }
        return isTouch;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mFloatingButton) {
            mWindowManager.removeView(mFloatingButton);
        }
    }

}