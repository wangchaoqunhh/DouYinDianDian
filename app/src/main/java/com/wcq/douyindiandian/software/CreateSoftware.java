package com.wcq.douyindiandian.software;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import static com.wcq.douyindiandian.constants.Constant.isUsable;
import static com.wcq.douyindiandian.software.Software.mSoftware;
import static com.wcq.douyindiandian.util.ExpandFunctionKt.showLoge;

public class CreateSoftware {
    private static CreateSoftware mCreateSoftware;

    public static CreateSoftware getInstance(String packageName, AccessibilityService accessibilityService) {
        if (mCreateSoftware == null || mSoftware == null || !packageName.equals(mSoftware.getPackageName())) {
            synchronized (CreateSoftware.class) {
                if (mCreateSoftware == null || mSoftware == null || !packageName.equals(mSoftware.getPackageName())) {
                    mCreateSoftware = new CreateSoftware(packageName, accessibilityService);
                }
            }
        }
        return mCreateSoftware;
    }

    private CreateSoftware(String packageName, AccessibilityService accessibilityService) {
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
        mCreateSoftware = null;
    }
}
