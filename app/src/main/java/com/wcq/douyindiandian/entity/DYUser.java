package com.wcq.douyindiandian.entity;

import cn.bmob.v3.BmobObject;

public class DYUser extends BmobObject {
    private boolean isUsable;
    private boolean isNOLimitPhone;
    private String meid;
    private String sellTime;
    private int sellDay;

    public boolean isNOLimitPhone() {
        return isNOLimitPhone;
    }

    public void setNOLimitPhone(boolean NOLimitPhone) {
        isNOLimitPhone = NOLimitPhone;
    }

    public boolean isUsable() {
        return isUsable;
    }

    public void setUsable(boolean usable) {
        isUsable = usable;
    }

    public String getMeid() {
        return meid;
    }

    public void setMeid(String meid) {
        this.meid = meid;
    }

    public String getSellTime() {
        return sellTime;
    }

    public void setSellTime(String sellTime) {
        this.sellTime = sellTime;
    }

    public int getSellDay() {
        return sellDay;
    }

    public void setSellDay(int sellDay) {
        this.sellDay = sellDay;
    }
}
