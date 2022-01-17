package com.tencent.klevinDemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.tencent.klevin.KlevinConfig;
import com.tencent.klevin.KlevinCustomController;
import com.tencent.klevin.KlevinManager;
import com.tencent.klevin.base.log.ARMLog;
import com.tencent.klevin.listener.InitializationListener;
import com.tencent.klevinDemo.ads.interstitialAD.InterstitialADActivity;
import com.tencent.klevinDemo.ads.nativeAD.NativeMenuActivity;
import com.tencent.klevinDemo.ads.rewardAD.RewardADActivity;
import com.tencent.klevinDemo.ads.splashAd.SplashADActivity;
import com.tencent.klevinDemo.log.LogObservable;
import com.tencent.klevinDemo.menu.BaseMenuActivity;
import com.tencent.klevinDemo.utils.CacheUtil;
import com.tencent.klevinDemo.utils.NoDoubleClickUtil;
import com.tencent.klevinadtest.KlevinAdTestActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Objects;

public class MainActivity extends BaseMenuActivity {
    private static final String TAG = ConfigConsts.DEMO_TAG;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private Menu barMenu;
    private MenuItem testEnvItem;
    private MenuItem logItem;
    private MenuItem appIdItem;
    private MenuItem networkItem;

    private SharedPreferences sharedPreferences;

    //隐私配置相关
    private JSONObject customControllerJSON;
    private final KlevinCustomController klevinCustomController = new KlevinCustomController() {
        private boolean getBoolean(String name) {
            try {
                return customControllerJSON.getBoolean(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //默认打开
            return true;
        }

        @Override
        public boolean isCanUseLocation() {
            return getBoolean("isCanUseLocation");
        }

        @Override
        public boolean isCanUseWifiState() {
            return getBoolean("isCanUseWifiState");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(getString(R.string.sp_name_setting), Context.MODE_PRIVATE);
        customControllerJSON = initPrivacyConfig();

        initActionBar();
        setListTexts(R.array.ad_type);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        barMenu = menu;
        initMenu();
        checkPermission();
        return true;
    }

    @Override
    public void onClick(View v) {
        if (NoDoubleClickUtil.isDoubleClick()) {
            return;
        }
        int adType = (int) v.getTag();//Strings.xml中的ad_type数组下标
        Class<?> menuActivity;
        switch (adType) {
            case 0: {//开屏广告
                menuActivity = SplashADActivity.class;
                break;
            }
            case 1: {//激励视频
                menuActivity = RewardADActivity.class;
                break;
            }
            case 2: {//插屏广告
                menuActivity = InterstitialADActivity.class;
                break;
            }
            case 3: {//自渲染广告
                menuActivity = NativeMenuActivity.class;
                break;
            }
            default: {
                throw new IllegalStateException("No such ad type!");
            }
        }

        Intent start = new Intent(MainActivity.this, menuActivity);
        start.putExtra(getString(R.string.env_test), isTestEnv());
        start.putExtra(getString(R.string.intent_button_name), listMenuAdapter.getText(adType));
        startActivity(start);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sw_test_env) {
            item.setChecked(!item.isChecked());
            setAppID(isTestEnv() ? ConfigConsts.DEBUG_APPID : ConfigConsts.RELEASE_APPID);
            flushAppId();
            initSDK();
        } else if (id == R.id.menu_btn_appId) {
            changeAppID();
        } else if (id == R.id.menu_btn_network) {
            changeNetworkType();
        } else if (id == R.id.menu_btn_privacy) {
            changePrivacyConfig();
        } else if (id == R.id.menu_btn_ad_test) {
            showKlevinAdTestActivity();
        } else if (id == R.id.menu_sw_log) {
            item.setChecked(!item.isChecked());
            setLogDisplay(item.isChecked());
        } else if (id == R.id.menu_btn_clear_cache) {
            showToast(getString(
                    CacheUtil.clearCache(this) ?
                            R.string.clear_cache_success : R.string.clear_cache_fail));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveBooleanConfig();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ARMLog.setLogPrinter(null);

    }

    private void initActionBar() {
        ActionBar bar = Objects.requireNonNull(getSupportActionBar());
        bar.setTitle(getString(R.string.app_name)
                + "("
                + getSDKVersion()
                + ")");
    }

    @Override
    protected void initLog() {
        ARMLog.setLogPrinter(LogObservable.get());
        super.initLog();
    }

    private void initMenu() {
        barMenu.findItem(R.id.menu_txt_version)
                .setTitle(getString(R.string.version) + getSDKVersion());

        testEnvItem = barMenu.findItem(R.id.menu_sw_test_env);
        logItem = barMenu.findItem(R.id.menu_sw_log);
        appIdItem = barMenu.findItem(R.id.menu_btn_appId);
        networkItem = barMenu.findItem(R.id.menu_btn_network);
        loadBooleanConfig();
        flushAppId();
        flushNetworkType();
        initSDK();
    }

    private void initSDK() {
        KlevinConfig.Builder initBuilder = new KlevinConfig.Builder()
                .appId(getAppID())
                .debugMode(BuildConfig.DEBUG)
                .directDownloadNetworkType(getNetworkType())
                .testEnv(isTestEnv())
                .customController(klevinCustomController);
        KlevinManager.init(getApplicationContext(), initBuilder.build(), new InitializationListener() {
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
                    Log.i(TAG, "oaid: " + oaid);
                } else {
                    Log.w(TAG, "not support oaid");
                }
            }
        });
    }

    private void showKlevinAdTestActivity() {
        Intent start = new Intent(MainActivity.this, KlevinAdTestActivity.class);
        startActivity(start);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, 101);
                    break;
                }
            }
        }
    }

    private void loadBooleanConfig() {
        testEnvItem.setChecked(sharedPreferences.getBoolean(getString(R.string.env_test),
                false));

        logItem.setChecked(sharedPreferences.getBoolean(getString(R.string.log),
                true));
        setLogDisplay(logItem.isChecked());
    }

    private void saveBooleanConfig() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (testEnvItem != null) {
            editor.putBoolean(getString(R.string.env_test), testEnvItem.isChecked());
        }
        if (logItem != null) {
            editor.putBoolean(getString(R.string.log), logItem.isChecked());
        }
        editor.apply();
    }

    private String getSDKVersion() {
        return "v" + KlevinManager.getVersion();
    }

    private boolean isTestEnv() {
        return testEnvItem.isChecked();
    }

    private String getAppID() {
        return sharedPreferences.getString(getString(R.string.appId), ConfigConsts.RELEASE_APPID);
    }

    private void setAppID(String appId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.appId), appId);
        editor.apply();
    }

    private void flushAppId() {
        appIdItem.setTitle(getString(R.string.appId) + "：" + getAppID());
    }

    private void changeAppID() {
        //构造一个输入框,输入appId
        final EditText inputServer = new EditText(MainActivity.this);
        inputServer.setFocusable(true);
        inputServer.setSingleLine(true);
        inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputServer.setHint(getAppID());
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.input_appId)).
                setView(inputServer).
                setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.confirm), (dia, which) -> {
                    String text = inputServer.getText().toString();
                    if (!text.isEmpty()) {
                        setAppID(text);
                        flushAppId();
                        showToast(getString(R.string.appId) + getString(R.string.modify_successful));
                        initSDK();
                    }
                });
        builder.show();
    }

    private int getNetworkType() {
        return sharedPreferences.getInt(getString(R.string.network_type), KlevinConfig.NETWORK_STATE_ALL);
    }

    private void setNetworkType(int type) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.network_type), type);
        editor.apply();
    }

    private int getNetworkTypeArrayIndex() {
        return 3 - (int) Math.ceil(getNetworkType() / 15.0);//向上取整对应数组下标
    }

    private void flushNetworkType() {
        networkItem.setTitle(getString(R.string.network_type)
                + getResources().getStringArray(R.array.network_type)[getNetworkTypeArrayIndex()]);
    }

    private void changeNetworkType() {
        int[] type = new int[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.input_network_type));
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.network_type),
                getNetworkTypeArrayIndex(), (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            type[0] = KlevinConfig.NETWORK_STATE_ALL;
                            break;
                        }
                        case 1: {
                            type[0] = KlevinConfig.NETWORK_STATE_WIFI;
                            break;
                        }
                        case 2: {
                            type[0] = KlevinConfig.NETWORK_STATE_MOBILE;
                            break;
                        }
                        case 3: {
                            type[0] = KlevinConfig.NETWORK_STATE_NONE;
                            break;
                        }
                        default: {
                        }
                    }
                });
        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            setNetworkType(type[0]);
            flushNetworkType();
            initSDK();
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private JSONObject initPrivacyConfig() {
        String beforePrefix = "isCanUse";
        String str = sharedPreferences.getString(getString(R.string.privacy_config), "");
        JSONObject res = new JSONObject();
        if (str == null || str.isEmpty()) {
            //为空则初始化为默认
            for (Method method : KlevinCustomController.class.getMethods()) {
                try {
                    if (!method.getName().startsWith(beforePrefix)) {
                        continue;
                    }
                    res.put(method.getName(), true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                res = new JSONObject(str);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private void savePrivacyConfig() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.privacy_config), customControllerJSON.toString());
        editor.apply();
    }

    private void changePrivacyConfig() {
        final int len = customControllerJSON.length();
        final String[] items = new String[len];
        final boolean[] checkedItems = new boolean[len];

        int i = 0;
        for (Iterator<String> it = customControllerJSON.keys(); it.hasNext(); ) {
            String name = it.next();
            items[i] = name;
            boolean res = true;
            try {
                res = customControllerJSON.getBoolean(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            checkedItems[i] = res;
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.input_privacy_config));
        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);
        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            for (int j = 0; j < items.length; j++) {
                try {
                    customControllerJSON.put(items[j], checkedItems[j]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            savePrivacyConfig();
            initSDK();
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }
}
