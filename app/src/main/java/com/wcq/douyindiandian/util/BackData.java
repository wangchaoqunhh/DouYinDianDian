package com.wcq.douyindiandian.util;

import android.view.accessibility.AccessibilityNodeInfo;

public interface BackData {

    //查找 所有节点 返回其中一种
    public interface OnBackData {
        void onBackData(AccessibilityNodeInfo nodeInfo);
    }

    //查找 所有节点 返回其中一种
    public interface OnBackClassNameData {
        /**
         * @param nodeInfo         当前布局(节点)
         * @param inParentPosition 在父布局中的位置
         * @param brotherNum       兄弟个数
         */
        void onBackClassNameData(AccessibilityNodeInfo nodeInfo, int inParentPosition, int brotherNum);
    }

    /**
     * 点击一个坐标,滑动一个控件 之后要执行的代码
     */
    public interface OnClickScreenCoordinate {
        void onClickScreenCoordinate();
    }
}
