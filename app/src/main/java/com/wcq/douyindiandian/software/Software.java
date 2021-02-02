package com.wcq.douyindiandian.software;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.wcq.douyindiandian.entity.DYUser;
import com.wcq.douyindiandian.util.NodeInfoHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

import static com.wcq.douyindiandian.constants.Constant.isUsable;
import static com.wcq.douyindiandian.util.ExpandFunctionKt.showLoge;

//软件 接口，要给那些软件加功能实现它
public abstract class Software {
    //当前的无障碍服务
    public AccessibilityService mAccessibilityService;
    //获取节点的工具类
    public NodeInfoHelper mNodeInfoHelper;
    //是否应该获取节点 因为AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED 会一直在调用，所以需要他做判断
    public boolean isNeedGetInfo;
    //主要给定时器用的 判断什么时候应该让 isNeedGetInfo = true;  每完成一次功能++1
    public int mAttentionNumber;

    //软件的实例 用一个静态保存
    public static Software mSoftware;

    //当前这个应用的包名
    private String packageName;
    public Timer timer;

    public void onAccessibilityEvent(AccessibilityEvent event) {
        checkAccountUsable();
    }

    public static <T extends Software> Software getInstance(Class<T> clazz, String packageName, AccessibilityService accessibilityService) {
        if (mSoftware == null || !packageName.equals(mSoftware.getPackageName())) {
            synchronized (clazz) {
                if (mSoftware == null || !packageName.equals(mSoftware.getPackageName())) {
                    try {
                        Constructor<T> softwareConstructor = clazz.getDeclaredConstructor();
                        mSoftware = softwareConstructor.newInstance();
                        mSoftware.setPackageName(packageName);
                        mSoftware.mAccessibilityService = accessibilityService;
                        mSoftware.mNodeInfoHelper = new NodeInfoHelper(accessibilityService);
                        showLoge(accessibilityService, "创建项目成功", packageName);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mSoftware;
    }

    /**
     * 开启一个定时器 并且在20s内如果页面没变化 就 isNeedGetInfo = true;
     */
    public void startEventControl(){
        isNeedGetInfo = false;
        //开启一个定时 这样在外界影响下中段 isNeedGetInfo 让他自己也能 true
        final int an = mAttentionNumber;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (an == mAttentionNumber) {
                    showLoge(mAccessibilityService, "点击定时器刷新", "点击定时器刷新");
                    isNeedGetInfo = true;
                }
                timer.cancel();
            }
        };
        timer.schedule(task, 20000);
    }

    /**
     * 判断账号是否还有效
     */
    private void checkAccountUsable() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SharedPreferences sp = mAccessibilityService.getSharedPreferences("login_code", Context.MODE_PRIVATE);
                String loginCode = sp.getString("login_code", "");
                if (!TextUtils.isEmpty(loginCode)) {
                    BmobQuery<DYUser> dYUser = new BmobQuery();
                    dYUser.getObject(loginCode, new QueryListener<DYUser>() {
                        @Override
                        public void done(DYUser dy, BmobException e) {
                            if (dy != null) {
                                isUsable = dy.isUsable();
                            } else {
                                isUsable = false;
                            }
                        }
                    });
                } else {
                    isUsable = false;
                }
            }
        }, 1000 * 60 * 20, 1000 * 60 * 20);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void sleep(int sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
