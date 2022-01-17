package com.tencent.klevinDemo.utils;

/**
 * Created by fluxliu on 2016/8/25.
 */
public class NoDoubleClickUtil {
    private final static int SPACE_TIME = 300;
    private static long lastClickTime;

    public synchronized static boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClick2 = currentTime - lastClickTime <= SPACE_TIME;
        if (!isClick2) {
            lastClickTime = currentTime;
        }
        return isClick2;
    }
}
