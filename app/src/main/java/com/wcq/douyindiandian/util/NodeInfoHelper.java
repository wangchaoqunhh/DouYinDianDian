package com.wcq.douyindiandian.util;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityNodeInfo;

import com.wcq.douyindiandian.util.BackData.OnBackClassNameData;
import com.wcq.douyindiandian.util.BackData.OnBackData;
import com.wcq.douyindiandian.util.BackData.OnClickScreenCoordinate;
import com.wcq.douyindiandian.util.bean.NodeInfoBean;

import java.util.List;
import java.util.Map;

import static com.wcq.douyindiandian.util.ExpandFunctionKt.showLoge;

@TargetApi(Build.VERSION_CODES.N)
public class NodeInfoHelper {
    private Context mContext;
    private AccessibilityService mAccessibilityService;

    public NodeInfoHelper(AccessibilityService accessibilityService) {
        this.mContext = accessibilityService;
        this.mAccessibilityService = accessibilityService;
    }

    //遍历所有节点时为了 回收用
//    public Map<AccessibilityNodeInfo, String> allNodeInfo;

    public void getAllNodeInfo(String className, AccessibilityNodeInfo nodeInfo, OnBackData onBackData) {
        if (nodeInfo != null && !TextUtils.isEmpty(nodeInfo.getClassName().toString())) {
            if (onBackData != null && className.equals(nodeInfo.getClassName())) {
                //把找到的NOdeInfo 打印出来
                showLoge(mContext, className, nodeInfo.isClickable() + "-----" + nodeInfo.getText() + "---" + nodeInfo.getParent().getClassName());
                onBackData.onBackData(nodeInfo);
            }
            int childCount = nodeInfo.getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo child = nodeInfo.getChild(i);
                    getAllNodeInfo(className, child, onBackData);
                }
            }
            nodeInfo.recycle();
        }
    }

    private boolean pageIsHaveNodeInfo(String className, AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null && !TextUtils.isEmpty(nodeInfo.getClassName().toString())) {
            if (className.equals(nodeInfo.getClassName())) {
                //把找到的NOdeInfo 打印出来
                showLoge(mContext, className, nodeInfo.isClickable() + "-----" + nodeInfo.getText() + "---" + nodeInfo.getParent().getClassName());
                return true;
            }
            int childCount = nodeInfo.getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo child = nodeInfo.getChild(i);
                    boolean b = pageIsHaveNodeInfo(className, child);
                    if (b) {
                        return true;
                    }
                }
            }
            nodeInfo.recycle();
        }
        return false;
    }


//    public void nodeInfoRecycle() {
//        if (allNodeInfo != null) {
//            for (AccessibilityNodeInfo key : allNodeInfo.keySet()) {
//                if (key != null)
//                    key.recycle();
//            }
//        }
//        allNodeInfo = null;
//    }

    /**
     * 获取所有与className相同的节点 并且通过back 返回调用者，并且已经做节点回收了
     *
     * @param className        要查找的类名 className
     * @param nodeInfo         在这个节点后代里查找
     * @param inParentPosition 在父布局 的位置
     * @param back             找到之后返回给调用者
     */
    public void getAllNodeInfo(String className, AccessibilityNodeInfo nodeInfo, int inParentPosition, OnBackClassNameData back) {
        if (nodeInfo != null && !TextUtils.isEmpty(nodeInfo.getClassName().toString())) {
            if (back != null && className.equals(nodeInfo.getClassName())) {
                //把找到的NOdeInfo 打印出来
                showLoge(mContext, className, nodeInfo.isClickable() + "-----" + nodeInfo.getText() + "---" + nodeInfo.getParent().getClassName());
                back.onBackClassNameData(nodeInfo, inParentPosition, nodeInfo.getParent().getChildCount());
            }
            int childCount = nodeInfo.getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo child = nodeInfo.getChild(i);
                    getAllNodeInfo(className, child, i, back);
                }
                nodeInfo.recycle();
            } else {
                nodeInfo.recycle();
            }
        }
    }


    /**
     * 判断页面是否应该点击
     */
    public boolean pageIsClick(AccessibilityNodeInfo info, List<NodeInfoBean> list) {
        //合格个数 最后这个数 应该等于 list.size() 才对
        int qualifiedNum = 0;
        boolean pageIsClick = false;
        for (NodeInfoBean nodeInfoBean : list) {
            switch (nodeInfoBean.getType()) {
                case NodeInfoBean.className:
                    if (pageIsHaveNodeInfo(nodeInfoBean.getContent(), info)) {
                        qualifiedNum++;
                    }
                    break;
                case NodeInfoBean.textContent:
                    List<AccessibilityNodeInfo> nodeInfo = info.findAccessibilityNodeInfosByText(nodeInfoBean.getContent());
                    if (!ListUtil.isEmpty(nodeInfo)) {
                        qualifiedNum++;
                    }
                    break;
            }
        }
        if (!ListUtil.isEmpty(list) && qualifiedNum == list.size()) {
            pageIsClick = true;
        }
        return pageIsClick;
    }

    /**
     * 判断 这个节点的 父节点  爷节点  太爷节点 以此类推， 是否满足后面的可变参数
     * 如果满足返回true
     *
     * @param info       当前节点
     * @param classNames 可变参数 父节点 爷节点 以此类推
     */
    public boolean judgeParent(AccessibilityNodeInfo info, String... classNames) {
        if (info != null) {
            AccessibilityNodeInfo nodeInfo = info;
            String[] className = classNames;
            for (int i = 0; i < className.length; i++) {
                AccessibilityNodeInfo parent = nodeInfo.getParent();
//                nodeInfo.recycle();
                nodeInfo = parent;
                if (!nodeInfo.getClassName().toString().equals(className[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    //当前页 排除有的 文字
    public boolean excludeText(AccessibilityNodeInfo info, String... text) {
        if (info != null) {
            for (String s : text) {
                if (info.getText() != null && s.contains(info.getText().toString())) {
                    return false;
                }
            }
        }
        return true;
    }

    //滑动方法 固定写法 我这个是上滑 如果想改滑动方向 可设置path
    public void screenTo(final OnClickScreenCoordinate onClickScreenCoordinate, int[]... xy) {
//        int width =getWindowManager().getDefaultDisplay().getWidth();
//        int height =getWindowManager().getDefaultDisplay().getHeight();

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        showLoge(mContext, "长宽", width + "px");
        showLoge(mContext, "长宽", height + "px");

        Path path = new Path();
        if (xy == null || xy.length == 0) {
            path.moveTo(width / 2, height / 2);
            path.lineTo(width / 2, 200);
        } else {
            path.moveTo(xy[0][0], xy[0][1]);
            path.lineTo(xy[1][0], xy[1][1]);
        }

        GestureDescription.Builder builder = new GestureDescription.Builder();

        GestureDescription gestureDescription = builder
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 1))
                .build();

        //上滑方法
        mAccessibilityService.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                if (onClickScreenCoordinate != null) {
                    onClickScreenCoordinate.onClickScreenCoordinate();
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                showLoge(mContext, "screenTo", "取消");
            }
        }, null);
    }

    /**
     * 点击 某一个 位置
     *
     * @param x
     * @param y
     */
    public void clickCoordinate(int x, int y, final OnClickScreenCoordinate onClickScreenCoordinate) {
//        int width =getWindowManager().getDefaultDisplay().getWidth();
//        int height =getWindowManager().getDefaultDisplay().getHeight();

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();

        GestureDescription gestureDescription = builder
                .addStroke(new GestureDescription.StrokeDescription(path, 1, 1))
                .build();

        //上滑方法
        mAccessibilityService.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                if (onClickScreenCoordinate != null) {
                    onClickScreenCoordinate.onClickScreenCoordinate();
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                showLoge(mContext, "点击", "点击取消");
            }
        }, null);
    }

//
//    //查找 所有节点 返回其中一种
//    public interface OnBackData {
//        void onBackData(AccessibilityNodeInfo nodeInfo);
//    }
//
//    //查找 所有节点 返回其中一种
//    public interface OnBackClassNameData {
//        /**
//         * @param nodeInfo         当前布局(节点)
//         * @param inParentPosition 在父布局中的位置
//         * @param brotherNum       兄弟个数
//         */
//        void onBackClassNameData(AccessibilityNodeInfo nodeInfo, int inParentPosition, int brotherNum);
//    }
//
//    /**
//     * 点击一个坐标,滑动一个控件 之后要执行的代码
//     */
//    public interface OnClickScreenCoordinate {
//        void onClickScreenCoordinate();
//    }
}
