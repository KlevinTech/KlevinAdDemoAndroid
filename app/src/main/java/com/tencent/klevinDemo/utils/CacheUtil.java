package com.tencent.klevinDemo.utils;

import android.content.Context;
import android.util.Log;

import com.tencent.klevinDemo.ConfigConsts;

import java.io.File;

public class CacheUtil {

    private static final String TAG = ConfigConsts.DEMO_TAG + "_CacheUtil";

    public static boolean clearCache(Context context) {
        File file = getCacheDir(context.getApplicationContext());
        return deleteFile(file.getPath());
    }

    private static boolean deleteFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return true;
            }
            if (file.isDirectory()) {
                String[] subPaths = file.list();
                if (subPaths != null) {
                    for (String subPath : subPaths) {
                        if (!deleteFile(path + File.separator + subPath)) {
                            return false;
                        }
                    }
                }
            }
            return file.delete();
        } catch (Exception e) {
            Log.e(TAG, "deleteFile err:" + e.getMessage());
            return false;
        }
    }

    private static File getCacheDir(Context context) {
        File externalCache = context.getApplicationContext().getExternalCacheDir();
        File internalCache = context.getApplicationContext().getCacheDir();
        return externalCache != null ? externalCache : internalCache;
    }
}