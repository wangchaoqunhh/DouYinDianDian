package com.wcq.douyindiandian.software;

import android.view.accessibility.AccessibilityEvent;

import static com.wcq.douyindiandian.util.ExpandFunctionKt.showLoge;

public class DianSoftware extends Software {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        showLoge(mAccessibilityService, "DouYinD点", "DouYinD点");
    }
}
