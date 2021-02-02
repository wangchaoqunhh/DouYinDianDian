package com.wcq.douyindiandian.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.wcq.douyindiandian.util.BackData;
import com.wcq.douyindiandian.util.ListUtil;
import com.wcq.douyindiandian.util.NodeInfoHelper;

//抖音10.0.5版本无障碍服务
@TargetApi(Build.VERSION_CODES.N)
public class DouYin10Service extends AccessibilityService {

    private NodeInfoHelper mNodeInfoHelper = new NodeInfoHelper(this);
    private boolean isNeedGetInfo = true;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        必须。通过这个函数可以接收系统发送来的AccessibilityEvent，接收来的AccessibilityEvent是经过过滤的，过滤是在配置工作时设置的。
        Log.e("所有都能看", event.toString());
        if ("com.ss.android.ugc.aweme".equals(event.getPackageName()) && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Log.e("抖音来了", event.toString());
            //有可能有底部按钮 给取消了
            if (getRootInActiveWindow() != null && !ListUtil.isEmpty(getRootInActiveWindow().findAccessibilityNodeInfosByText("立即赠送"))) {
                mNodeInfoHelper.clickCoordinate(518, 104, new BackData.OnClickScreenCoordinate() {
                    @Override
                    public void onClickScreenCoordinate() {
                        sleep(500);
                    }
                });
            }

            if (isNeedGetInfo) {
                isNeedGetInfo = false;
                mNodeInfoHelper.getAllNodeInfo("android.widget.TextView", getRootInActiveWindow(), new BackData.OnBackData() {
                    @Override
                    public void onBackData(AccessibilityNodeInfo child) {
                        try {
                            if (child.getText() != null) {
                                Integer.parseInt(child.getText().toString().substring(0, 1));
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return;
                        }

                        if (child.isClickable() && mNodeInfoHelper.excludeText(child, "关注", "说点") && mNodeInfoHelper.judgeParent(child, "android.support.v4.view.ViewPager")) {
                            Log.e("我找到了", "我找打了 " + child.getText());
                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.e("点击用户", "点击用户 " + child.getText());
                            sleep(3000);
                            mNodeInfoHelper.getAllNodeInfo("android.widget.Button", getRootInActiveWindow(), 0, new BackData.OnBackClassNameData() {
                                @Override
                                public void onBackClassNameData(AccessibilityNodeInfo nodeInfo, int position, int brotherNum) {
                                    //有多少个 but
                                    Log.e("num个数", position + "--num-- " + brotherNum);
                                    //音浪值
                                    String yinLang = null;
                                    try {
                                        yinLang = nodeInfo.getChild(nodeInfo.getChildCount() - 1).getText().toString();
                                    } catch (Exception e) {
                                        yinLang = "0";
                                        e.printStackTrace();
                                    }
                                    Log.e("yinLang", "yinLang " + yinLang);
                                    boolean isNeedGuanZhu = false;
                                    if (nodeInfo.getChildCount() > 3 && nodeInfo.getChild(2).isClickable()) {
                                        try {
                                            //音浪值 大于某个数时在点击关注此人
                                            if (Integer.parseInt(yinLang) > 10) {
                                                isNeedGuanZhu = true;
                                            }
                                        } catch (NumberFormatException e) {
                                            isNeedGuanZhu = true;
                                            e.printStackTrace();
                                        }
                                    }

                                    if (isNeedGuanZhu) {
                                        nodeInfo.getChild(2).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        Log.e("点击关注", "点击关注 " + position);
                                        sleep(1500);
                                    }

                                    if (position == brotherNum - 1) {
                                        Log.e("点击完成", "点击完成 " + position);
                                        mNodeInfoHelper.clickCoordinate(518, 104, new BackData.OnClickScreenCoordinate() {
                                            @Override
                                            public void onClickScreenCoordinate() {
                                                Log.e("点击关闭弹窗", "点击关闭弹窗");
                                                sleep(1000);

                                                mNodeInfoHelper.screenTo(new BackData.OnClickScreenCoordinate() {
                                                    @Override
                                                    public void onClickScreenCoordinate() {
                                                        Log.e("点击滑动", "点击滑动");
                                                        isNeedGetInfo = true;
                                                        sleep(2000);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }


        //点击返回
//        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    private void sleep(int sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
//        可选。系统会在成功连接上你的服务的时候调用这个方法，在这个方法里你可以做一下初始化工作，
//        例如设备的声音震动管理，也可以调用setServiceInfo()进行配置工作。
        Log.e("连接", "连接");
    }

    @Override
    public void onInterrupt() {
//        必须。这个在系统想要中断AccessibilityService返给的响应时会调用。在整个生命周期里会被调用多次。
        Log.e("中断AccessibilityService", "中断AccessibilityService");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("服务关闭", "关闭");
        return super.onUnbind(intent);
//        可选。在系统将要关闭这个AccessibilityService会被调用。在这个方法中进行一些释放资源的工作。
    }
}
