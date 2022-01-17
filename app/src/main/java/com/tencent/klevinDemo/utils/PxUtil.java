package com.tencent.klevinDemo.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

public class PxUtil {

    public static int dpToPx(Context context, int dp) {
        Resources r = context.getApplicationContext().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int) px;
    }

    public static int pxToDp(Context context, int px) {
        float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }


    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 检查设备类型：unknown(0)，手机(1)，平板(2)
     */
    public static int getDeviceType(Context context) {
        int type = 0;

        try {
            if ((context.getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
                type = 2;
            } else {
                type = 1;
            }
        } catch (Exception e) {
            Log.e("PxUtil", "Get device type encounter exception: " + e.getMessage());
        }

        return type;
    }

    public static int getDeviceWidthInPixel(Context context) {
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getDeviceHeightInPixel(Context context) {
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
