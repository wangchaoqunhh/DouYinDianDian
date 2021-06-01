package com.wcq.douyindiandian.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import com.wcq.douyindiandian.software.factory.SoftwareFactory;
import com.wcq.douyindiandian.util.Logging;

import java.util.Timer;
import java.util.TimerTask;

import static com.wcq.douyindiandian.util.ExpandFunctionKt.showLoge;
import static com.wcq.douyindiandian.util.ExpandFunctionKt.showToast;

public class MyAccessibilityService extends AccessibilityService {
    private String TAG = "MyAccessibilityService";

    private SoftwareFactory mSoftwareFactory;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        必须。通过这个函数可以接收系统发送来的AccessibilityEvent，接收来的AccessibilityEvent是经过过滤的，过滤是在配置工作时设置的。
        showLoge(this, TAG + "所有都能看", event.toString());
        mSoftwareFactory = SoftwareFactory.getInstance(event.getPackageName().toString(), this);
        if (mSoftwareFactory != null) {
            mSoftwareFactory.startRun(event);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
//        可选。系统会在成功连接上你的服务的时候调用这个方法，在这个方法里你可以做一下初始化工作，
//        例如设备的声音震动管理，也可以调用setServiceInfo()进行配置工作。
        showToast(this, "连接成功");
        showLoge(this, "连接", "连接");
    }

    @Override
    public void onInterrupt() {
//        必须。这个在系统想要中断AccessibilityService返给的响应时会调用。在整个生命周期里会被调用多次。
        showLoge(this, "中断AccessibilityService", "中断AccessibilityService");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        showLoge(this, "服务关闭", "关闭");
        if (mSoftwareFactory != null) {
            mSoftwareFactory.destroyInstance();
        }
        return super.onUnbind(intent);
//        可选。在系统将要关闭这个AccessibilityService会被调用。在这个方法中进行一些释放资源的工作。
    }

}
