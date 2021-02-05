package com.wcq.douyindiandian.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wcq.douyindiandian.entity.MainDataBean;

import cn.bmob.v3.Bmob;

public class AppApplication extends Application {

    private MainDataBean mainDataBean;

    @Override
    public void onCreate() {
        super.onCreate();
        //提供以下两种方式进行初始化操作：

        //第一：默认初始化
        Bmob.initialize(this, "ca0ef38dcb45459ee1cdf048f1f02573");
        // 注:自v3.5.2开始，数据sdk内部缝合了统计sdk，开发者无需额外集成，传渠道参数即可，不传默认没开启数据统计功能
        //Bmob.initialize(this, "Your Application ID","bmob");

        //第二：自v3.4.7版本开始,设置BmobConfig,允许设置请求超时时间、文件分片上传时每片的大小、文件的过期时间(单位为秒)，
        //BmobConfig config =new BmobConfig.Builder(this)
        ////设置appkey
        //.setApplicationId("Your Application ID")
        ////请求超时时间（单位为秒）：默认15s
        //.setConnectTimeout(30)
        ////文件分片上传时每片的大小（单位字节），默认512*1024
        //.setUploadBlockSize(1024*1024)
        ////文件的过期时间(单位为秒)：默认1800s
        //.setFileExpiration(2500)
        //.build();
        //Bmob.initialize(config);
    }

    public MainDataBean getMainDataBean() {
        SharedPreferences sp = getSharedPreferences("mainDataBean", Context.MODE_PRIVATE);
        String mainDataBeanString = sp.getString("mainDataBean", "");
        if (mainDataBean != null) {

        } else if (!TextUtils.isEmpty(mainDataBeanString)) {
            mainDataBean = new Gson().fromJson(mainDataBeanString, new TypeToken<MainDataBean>() {
            }.getType());
        } else {
            mainDataBean = MainDataBean.INSTANCE;
            saveMainData();
        }
        return mainDataBean;
    }

    public void saveMainData() {
        setMainDataBean(mainDataBean);
    }

    private void setMainDataBean(MainDataBean mainDataBean) {
        this.mainDataBean = mainDataBean;
        SharedPreferences.Editor edit = getSharedPreferences("mainDataBean", Context.MODE_PRIVATE).edit();
        edit.putString("mainDataBean", new Gson().toJson(mainDataBean));
        edit.commit();
    }
}
