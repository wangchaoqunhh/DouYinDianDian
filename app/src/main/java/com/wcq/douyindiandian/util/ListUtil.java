package com.wcq.douyindiandian.util;

import java.util.List;

public class ListUtil {

    public static boolean isEmpty(List list) {
        if (list == null || list.size() <= 0) {
            return true;
        }
        return false;
    }
}
