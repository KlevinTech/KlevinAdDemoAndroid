package com.tencent.klevinDemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.klevinDemo.ConfigConsts;

public class SPUtil {
    private static final String SPTAG = ConfigConsts.DEMO_TAG + "_file";

    public static void SPSaveString(final Context context, final String key, final String value) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SPSaveStringInMain(final Context context, final String key, final String value) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 默认返回“”
     *
     * @author hyqiao
     * @time 2017/8/15 10:46
     */
    public static String SPGetString(Context context, String key) {
        return SPGetString(context, key, "");
    }

    public static String SPGetString(Context context, String key, String default_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, default_value);
    }

    public static void SPSaveInt(final Context context, final String key, final int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void SPSaveIntInMain(final Context context, final String key, final int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 默认返回0
     *
     * @author hyqiao
     * @time 2017/8/15 10:46
     */
    public static int SPGetInt(Context context, String key) {
        return SPGetInt(context, key, 0);
    }

    public static int SPGetInt(Context context, String key, int default_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, default_value);
    }

    public static void SPSaveLong(final Context context, final String key, final long value) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(key, value);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 默认返回0
     *
     * @author hyqiao
     * @time 2017/8/15 10:46
     */
    public static long SPGetLong(Context context, String key) {
        return SPGetLong(context, key, 0L);
    }

    public static long SPGetLong(Context context, String key, long default_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, default_value);
    }

    public static void SPSaveBool(final Context context, final String key, final boolean value) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 默认返回false
     *
     * @author hyqiao
     * @time 2017/8/15 10:46
     */
    public static boolean SPGetBool(Context context, String key) {
        return SPGetBool(context, key, false);
    }

    public static boolean SPGetBool(Context context, String key, boolean default_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPTAG, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, default_value);
    }
}
