package com.wcq.douyindiandian.software.factory;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import com.wcq.douyindiandian.software.DouYinSoftware;
import com.wcq.douyindiandian.software.Software;

import static com.wcq.douyindiandian.constants.Constant.isUsable;
import static com.wcq.douyindiandian.software.Software.mSoftware;
import static com.wcq.douyindiandian.util.ExpandFunctionKt.showLoge;

public class SoftwareFactory {
    private static SoftwareFactory mSoftwareFactory;

    public static SoftwareFactory getInstance(String packageName, AccessibilityService accessibilityService) {
        if (mSoftwareFactory == null || mSoftware == null || !packageName.equals(mSoftware.getPackageName())) {
            synchronized (SoftwareFactory.class) {
                if (mSoftwareFactory == null || mSoftware == null || !packageName.equals(mSoftware.getPackageName())) {
                    if("com.ss.android.ugc.aweme".equals(packageName)){
                        mSoftwareFactory = new SoftwareFactory(packageName, accessibilityService);
                    }
                }
            }
        }
        return mSoftwareFactory;
    }

    private SoftwareFactory(String packageName, AccessibilityService accessibilityService) {
        showLoge(accessibilityService, "packageName", packageName);
        switch (packageName) {
            case "com.ss.android.ugc.aweme"://抖音
                Software.getInstance(DouYinSoftware.class, packageName, accessibilityService);
                break;
            default:
                showLoge(accessibilityService, "没有此包", "没有此包");
                break;
        }
    }

    public void startRun(AccessibilityEvent event) {
        //是否可用
        if (!isUsable) {
            return;
        }
        if (mSoftware != null) {
            mSoftware.onAccessibilityEvent(event);
        }
    }

    public void destroyInstance() {
        if (mSoftware != null && mSoftware.timer != null) {
            mSoftware.timer.cancel();
        }
        mSoftware = null;
        mSoftwareFactory = null;
    }
}
