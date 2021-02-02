package com.wcq.douyindiandian.util;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.wcq.douyindiandian.util.ExpandFunctionKt.showToast;

public class TimeUtil {
    //  1、Date对象转换为时间戳 效果如下： 1508824283292
    public static long dateToTimestamp(Date date) {
        long times = date.getTime();
        return times;
    }

    // 2、时间戳转换为Date日期对象   效果如下： Tue Oct 24 13:49:28 CST 2017
    public static Date timestampToDate(long times) {
        Date date = new Date(times);
        return date;
    }

    //3、时间戳转换为指定日期格式   效果如下：  2017-10-24 13:50:46
    public static String timestampToYYYY(long times) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(times);
        return str;
    }

    // 4、时间字符串<年月日时分秒毫秒 >转为 时间戳
    public static long YYYYToTimestamp(String time) {
        //大写HH：24小时制，小写hh：12小时制
        //毫秒：SSS
        //指定转化前的格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //转化后为Date日期格式
        Date date = null;
        long shootTime = 0;
        try {
            date = sdf.parse(time);
            //Date转为时间戳long
            shootTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return shootTime;
    }

    /**
     * 获取 某一天 的 后几天
     *
     * @param str      2020-02-03 11:11:11
     * @param laterDay 后的天 30
     */
    public static String dateLaterDay(String str, int laterDay) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = sdf.parse(str, new ParsePosition(0));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, laterDay);
        Date time = calendar.getTime();
        return sdf.format(time);
    }

    private static boolean isSuccess;

    //5获取当前时间
    public static void getCurrentTime(Activity activity, TimeBack timeBack) {
        isSuccess = false;
        new Thread() {
            @Override
            public void run() {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(() -> {
                            if (!isSuccess && timeBack != null) {
                                timeBack.onFailed();
                            }
                        });
                        timer.cancel();
                    }
                }, 10000);

                SntpClient sntpClient = new SntpClient();
                if (sntpClient.requestTime("cn.pool.ntp.org", 30000)) {
                    long now = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
                    Date current = new Date(now);
                    Log.d("当前时间", current.toString());
                    activity.runOnUiThread(() -> {
                        isSuccess = true;
                        if (timeBack != null) {
                            timeBack.onTimeBack(current);
                        }
                    });
                }
            }
        }.start();
    }

    public interface TimeBack {
        void onTimeBack(Date date);

        void onFailed();
    }
}
