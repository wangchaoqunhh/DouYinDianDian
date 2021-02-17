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

import static com.wcq.douyindiandian.util.ExpandFunctionKt.eventSchedule;
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
                case "com.ss.android.ugc.aweme.main.MainActivity"://首页推荐
                    currentActivityName = event.getClassName().toString();
                    eventSchedule(mAccessibilityService, timer1 -> {
                        List<NodeInfoBean> list = new ArrayList<>();
                        list.add(new NodeInfoBean(NodeInfoBean.className, "androidx.viewpager.widget.ViewPager"));
                        list.add(new NodeInfoBean(NodeInfoBean.textContent, "推荐"));
                        list.add(new NodeInfoBean(NodeInfoBean.textContent, "朋友"));
                        list.add(new NodeInfoBean(NodeInfoBean.textContent, "消息"));
                        list.add(new NodeInfoBean(NodeInfoBean.textContent, "我"));
                        boolean pageIsClick = mNodeInfoHelper.pageIsClick(mAccessibilityService.getRootInActiveWindow(), list);
                        if (pageIsClick) {
                            isNeedGetInfo = true;
                            timer1.cancel();
                        }
                        return null;
                    });
                    break;
//                case "androidx.appcompat.app.AlertDialog"://这个是 首页可能出的弹窗(青少年提示)
//                    mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
//                    break;
                case "com.bytedance.android.livesdk.widget.LiveBottomSheetDialog"://这个是关注好友的弹窗列表
                    break;
                default:
                    if ("com.ss.android.ugc.aweme".equals(event.getPackageName()))
                        mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
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
                case "com.ss.android.ugc.aweme.main.MainActivity":
                    if (mainDataBean.isOpenCultivateAccount()) {
                        openCultivateAccount();
                    }
                    break;
            }
        }
    }

    //
    //这个30是一共看30分钟
    private final int totalTime = 30;
    //这个30是 一是视频看30S
    private final int oneTime = 30;

    private void openCultivateAccount() {
        if (isNeedGetInfo) {
            isNeedGetInfo = false;
            if (mainDataBean.isBrushHomeRecommend() && mainDataBean.getHomeRecommendCompleteTime() < totalTime) {
                List<AccessibilityNodeInfo> recommend = mAccessibilityService.getRootInActiveWindow().findAccessibilityNodeInfosByText("推荐");
                recommend.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mainDataBean.isBrushHomeRecommend() && mainDataBean.getHomeRecommendCompleteTime() < totalTime) {
                            mNodeInfoHelper.getAllNodeInfo("androidx.viewpager.widget.ViewPager", mAccessibilityService.getRootInActiveWindow(), nodeInfo -> {
                                //这个30是一共看30分钟
                                if (mainDataBean.getHomeRecommendCompleteTime() < totalTime) {
                                    //保存完成的个数
                                    mainDataBean.setHomeRecommendCompleteNum(mainDataBean.getHomeRecommendCompleteNum() + 1);
                                    //保存完成的总时间 用个数算的  mainDataBean.getHomeRecommendCompleteNum() * 3 % 60
                                    // 这个30是 一是视频看30S
                                    mainDataBean.setHomeRecommendCompleteTime(mainDataBean.getHomeRecommendCompleteNum() * oneTime / 60);
                                    mApplication.saveMainData();
                                    showLoge(mAccessibilityService, "首页推荐当日完成个数", "" + mainDataBean.getHomeRecommendCompleteNum());
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                                    sleep(2000);
                                } else {
                                    timer.cancel();
                                    isNeedGetInfo = true;
                                    openCultivateAccount();
                                }
                            });
                        } else {
                            timer.cancel();
                            isNeedGetInfo = true;
                            openCultivateAccount();
                        }
                    }
                }, oneTime * 1000, oneTime * 1000); // 这个30是 一是视频看30S
                return;
            }
            if (mainDataBean.isBrushCityRecommend() && mainDataBean.getCityRecommendCompleteTime() < totalTime) {
                List<AccessibilityNodeInfo> attention = mAccessibilityService.getRootInActiveWindow().findAccessibilityNodeInfosByText("关注");
                List<AccessibilityNodeInfo> recommend = mAccessibilityService.getRootInActiveWindow().findAccessibilityNodeInfosByText("推荐");
                if (!ListUtil.isEmpty(attention) && !ListUtil.isEmpty(recommend)) {
                    int attentionBrotherSize = attention.get(0).getParent().getChildCount();
                    int recommendBrotherSize = recommend.get(0).getParent().getChildCount();
                    if (attentionBrotherSize == recommendBrotherSize) {
                        AccessibilityNodeInfo parent = attention.get(0).getParent();
                        AccessibilityNodeInfo city = null;
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            if (parent.getChild(i).getText() != null && "关注".equals(parent.getChild(i).getText().toString())) {
                                //点击同城按钮
                                city = parent.getChild(i - 1);
                                break;
                            }
                        }
                        if (city != null) {
                            city.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            sleep(2000);
                            brushCityRecommend();
                        }
                    }
                }
            }
            if (mainDataBean.isLiveRandomComment()) {
                showLoge(mAccessibilityService, "直播页随机评论", "直播页随机评论");
            }
            if (mainDataBean.isWatchVideo()) {

            }
            if (mainDataBean.isTopSearch()) {

            }
            if (mainDataBean.isRandomFollow()) {

            }
        }
    }

    private void brushCityRecommend() {
        mNodeInfoHelper.getAllNodeInfo("android.view.ViewGroup", mAccessibilityService.getRootInActiveWindow(), 0, (nodeInfo, inParentPosition, brotherNum) -> {
            if (mNodeInfoHelper.judgeParent(nodeInfo, "androidx.recyclerview.widget.RecyclerView")) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                sleep(10 * 1000);
                mainDataBean.setCityRecommendCompleteNum(mainDataBean.getCityRecommendCompleteNum() + 1);
                mainDataBean.setCityRecommendCompleteTime(mainDataBean.getCityRecommendCompleteNum() * oneTime / 60);
                mApplication.saveMainData();
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                sleep(1 * 1000);
                if (mainDataBean.getCityRecommendCompleteTime() < totalTime) {
                    if (inParentPosition == brotherNum - 1) {
                        nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        sleep(1 * 1000);
                        brushCityRecommend();
                    }
                } else {
                    isNeedGetInfo = true;
                }
            }
        });
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
            DouYinSoftware.this.sleep(2000);
            isNeedGetInfo = true;
            mAttentionNumber++;
            DouYinSoftware.this.cancelAllGuanZhu();
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
                    DouYinSoftware.this.sleep(3000);

                    DouYinSoftware.this.clickGuanZhu();
                    showLoge(mAccessibilityService, "点击完成", "点击完成 ");

                    //点击返回 关闭底部弹窗
                    mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    showLoge(mAccessibilityService, "点击关闭弹窗", "点击关闭弹窗");
                    DouYinSoftware.this.sleep(1000);

                    //上滑翻页
                    mNodeInfoHelper.getAllNodeInfo("androidx.viewpager.widget.ViewPager", mAccessibilityService.getRootInActiveWindow(), nodeInfo -> {
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        showLoge(mAccessibilityService, "点击滑动翻页", "点击滑动翻页");
                        isNeedGetInfo = true;
                        mAttentionNumber++;
                        DouYinSoftware.this.sleep(2000);
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
                        DouYinSoftware.this.sleep(1500);
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
                                DouYinSoftware.this.sleep(1000);
                                DouYinSoftware.this.clickGuanZhu();
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
