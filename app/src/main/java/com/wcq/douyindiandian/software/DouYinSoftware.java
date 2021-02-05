package com.wcq.douyindiandian.software;

import android.accessibilityservice.AccessibilityService;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.wcq.douyindiandian.util.ListUtil;
import com.wcq.douyindiandian.util.bean.NodeInfoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.wcq.douyindiandian.util.ExpandFunctionKt.showLoge;
import static com.wcq.douyindiandian.util.ExpandFunctionKt.showToast;

public class DouYinSoftware extends Software {

    //是否是抖音播放直播页  com.ss.android.ugc.aweme.live.LivePlayActivity
    //是否是自己关注的人页面 用于取消 所有关注 com.ss.android.ugc.aweme.following.ui.FollowRelationTabActivity
    private String currentActivityName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        super.onAccessibilityEvent(event);
//        必须。通过这个函数可以接收系统发送来的AccessibilityEvent，接收来的AccessibilityEvent是经过过滤的，过滤是在配置工作时设置的。
        showLoge(mAccessibilityService, "所有都能看DouYin", event.toString());

        //通过AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED 判断当前页面是那页
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            switch (event.getClassName().toString()) {
                case "com.ss.android.ugc.aweme.live.LivePlayActivity":
                    showToast(mAccessibilityService, "已自动开启榜上加关注");
                    showLoge(mAccessibilityService, "已自动开启榜上加关注", "已自动开启榜上加关注");
                    currentActivityName = event.getClassName().toString();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            List<NodeInfoBean> list = new ArrayList<>();
                            list.add(new NodeInfoBean(NodeInfoBean.className, "androidx.viewpager.widget.ViewPager"));
                            boolean pageIsClick = mNodeInfoHelper.pageIsClick(mAccessibilityService.getRootInActiveWindow(), list);
                            if (pageIsClick) {
                                isNeedGetInfo = true;
                                timer.cancel();
                            }
                        }
                    }, 0, 500);
                    break;
                case "com.ss.android.ugc.aweme.following.ui.FollowRelationTabActivity":
                    showToast(mAccessibilityService, "已自动开启取消未互相关注的用户");
                    isNeedGetInfo = true;
                    currentActivityName = event.getClassName().toString();
                    break;
                default:
                    break;
            }
        }

        //播放直播页 点击关注方法
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && !TextUtils.isEmpty(currentActivityName)) {
            switch (currentActivityName) {
                case "com.ss.android.ugc.aweme.live.LivePlayActivity":
                    showLoge(mAccessibilityService, "抖音直播页", event.toString());
                    if (mainDataBean.isLiveAttention())
                        livePlayClickAttention();
                    break;
                case "com.ss.android.ugc.aweme.following.ui.FollowRelationTabActivity":
                    if (mainDataBean.isCancelAttention())
                        cancelAllGuanZhu();
                    break;
            }
        }
    }

    private void cancelAllGuanZhu() {
        if (isNeedGetInfo) {
            super.startEventControl();
            sleep(1000);
            AccessibilityNodeInfo rootInActiveWindow = mAccessibilityService.getRootInActiveWindow();
            if (rootInActiveWindow != null) {
                List<AccessibilityNodeInfo> yiGuan = rootInActiveWindow.findAccessibilityNodeInfosByText("已关注");
                if (!ListUtil.isEmpty(yiGuan)) {
                    for (AccessibilityNodeInfo info : yiGuan) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        sleep(500);
                    }

                    scrollRecycler(rootInActiveWindow);
                } else {
                    scrollRecycler(rootInActiveWindow);
                }
            }
        }
    }

    /**
     * 上滑动recyclerView
     */
    private void scrollRecycler(AccessibilityNodeInfo rootInActiveWindow) {
        mNodeInfoHelper.getAllNodeInfo("androidx.recyclerview.widget.RecyclerView", rootInActiveWindow, nodeInfo -> {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            sleep(2000);
            isNeedGetInfo = true;
            mAttentionNumber++;
            cancelAllGuanZhu();
        });
    }

    //播放直播页 点击关注方法
    private void livePlayClickAttention() {
        if (isNeedGetInfo) {
            super.startEventControl();
            mNodeInfoHelper.getAllNodeInfo("android.widget.TextView", mAccessibilityService.getRootInActiveWindow(), child -> {
                try {
                    if (child.getText() != null) {
                        Integer.parseInt(child.getText().toString().substring(0, 1));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
                if (child.isClickable() && mNodeInfoHelper.excludeText(child, "关注", "说点") && mNodeInfoHelper.judgeParent(child, "androidx.viewpager.widget.ViewPager")) {
                    showLoge(mAccessibilityService, "我找到了", "我找打了 " + child.isClickable() + "---" + child.getText());
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    showLoge(mAccessibilityService, "点击用户", "点击用户 " + child.getText());
                    sleep(3000);

                    clickGuanZhu();
                    showLoge(mAccessibilityService, "点击完成", "点击完成 ");

                    //点击返回 关闭底部弹窗
                    mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    showLoge(mAccessibilityService, "点击关闭弹窗", "点击关闭弹窗");
                    sleep(1000);

                    //上滑翻页
                    mNodeInfoHelper.getAllNodeInfo("androidx.viewpager.widget.ViewPager", mAccessibilityService.getRootInActiveWindow(), nodeInfo -> {
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        showLoge(mAccessibilityService, "点击滑动翻页", "点击滑动翻页");
                        isNeedGetInfo = true;
                        mAttentionNumber++;
                        sleep(2000);
                    });

                }
            });
        }
    }

    private void clickGuanZhu() {
        mNodeInfoHelper.getAllNodeInfo(
                "android.widget.Button",
                mAccessibilityService.getRootInActiveWindow(),
                0,
                (nodeInfo, position, brotherNum) -> {
                    if (!mNodeInfoHelper.judgeParent(nodeInfo, "androidx.recyclerview.widget.RecyclerView")) {
                        return;
                    }
                    //有多少个 but
                    showLoge(mAccessibilityService, "num个数", "位置" + position + "--num-- 兄弟个数" + brotherNum);
                    //音浪值
                    String yinLang = null;
                    try {
                        yinLang = nodeInfo.getChild(nodeInfo.getChildCount() - 1).getText().toString();
                    } catch (Exception e) {
                        yinLang = "0";
                        e.printStackTrace();
                    }
                    showLoge(mAccessibilityService, "yinLang", "yinLang " + yinLang);
                    boolean isNeedGuanZhu = false;
                    if (nodeInfo.getChildCount() > 3 && nodeInfo.getChild(2).isClickable()) {
                        try {
                            //音浪值 大于某个数时在点击关注此人
                            if (Integer.parseInt(yinLang) > mainDataBean.getAttentionMixYinLang()) {
                                isNeedGuanZhu = true;
                            }
                        } catch (NumberFormatException e) {
                            isNeedGuanZhu = true;
                            e.printStackTrace();
                        }
                    }

                    if (isNeedGuanZhu) {
                        nodeInfo.getChild(2).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        showLoge(mAccessibilityService, "点击关注", "点击关注 " + position);
                        sleep(1500);
                    }

                    //判断 最后一条 之后是否需要继续
                    if (position == brotherNum - 1) {
                        boolean isNeedScreen = false;
                        try {
                            //音浪值 大于某个数时在点击关注此人
                            if (Integer.parseInt(yinLang) > mainDataBean.getAttentionMixYinLang()) {
                                isNeedScreen = true;
                            }
                        } catch (NumberFormatException e) {
                            isNeedScreen = true;
                            e.printStackTrace();
                        }
                        //上滑方法
                        if (isNeedScreen) {
                            mNodeInfoHelper.getAllNodeInfo("androidx.recyclerview.widget.RecyclerView", mAccessibilityService.getRootInActiveWindow(), nodeInfo1 -> {
                                nodeInfo1.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                                showLoge(mAccessibilityService, "点击本次上滑完成", "点击本次上滑完成");
                                sleep(1000);
                                clickGuanZhu();
                            });
                        } else {
                            showLoge(mAccessibilityService, "点击上滑所有完成", "点击上滑所有完成");
                        }
                        //递归
//                        mNodeInfoHelper.screenTo(() -> {
//                              showLoge(this, "点击弹窗滑动", "点击弹窗滑动");
//                            sleep(1000);
//                        }, new int[]{width / 2, (2148 * height) / 2267 - 10}, new int[]{width / 2, (1273 * height) / 2267 + 10});
                    } else {
                        //不需要上滑点击即可
                        showLoge(mAccessibilityService, "点击本次Item完成", "点击本次Item完成");
                    }
                });
    }

}
