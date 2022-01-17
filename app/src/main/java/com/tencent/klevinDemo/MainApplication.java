package com.tencent.klevinDemo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.klevin.KlevinConfig;
import com.tencent.klevin.KlevinManager;
import com.tencent.klevin.listener.InitializationListener;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;

import java.util.HashMap;

/**
 * @author brightjchen
 */
public class MainApplication extends Application {
    private static final String TAG = ConfigConsts.DEMO_TAG;

    @Override
    public void onCreate() {
        super.onCreate();
        initX5();
        CrashReport.initCrashReport(getApplicationContext(), "d976a8d791", false);

        KlevinConfig.Builder builder = new KlevinConfig.Builder()
                .appId(ConfigConsts.RELEASE_APPID)
                .directDownloadNetworkType(KlevinConfig.NETWORK_STATE_ALL)
                .debugMode(BuildConfig.DEBUG);
        KlevinManager.init(getApplicationContext(), builder.build(), new InitializationListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "init success");
            }

            @Override
            public void onError(int err, String msg) {
                Log.e(TAG, "err: " + err + " " + msg);
            }

            @Override
            public void onIdentifier(boolean support, String oaid) {
                if (support) {
                    Log.i(TAG, "oaid:" + oaid);
                } else {
                    Log.w(TAG, "not support oaid");
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName(this);
            String packageName = this.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }

    private String getProcessName(Context context) {
        if (context == null) {
            return null;
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }

    private void initX5(){
        HashMap map = new HashMap();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        QbSdk.initX5Environment(this, null);
    }
}
