package com.tencent.klevinDemo.ads.splashAd;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tencent.klevin.ads.ad.SplashAd;
import com.tencent.klevin.ads.ad.SplashAdRequest;
import com.tencent.klevinDemo.ConfigConsts;
import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.ads.BaseADActivity;
import com.tencent.klevinDemo.utils.NoDoubleClickUtil;

public class SplashADActivity extends BaseADActivity {

    private static final String TAG = ConfigConsts.DEMO_TAG + "_SplashAD";

    private Button loadADBtn;
    private Button showADBtn;
    private Button loadAndShowADBtn;

    private SplashAd mSplashAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_ad);
        initView();
        boolean isTestEnv = getIntent().getBooleanExtra(getString(R.string.env_test), true);
        initCommonView(isTestEnv ? ConfigConsts.DEBUG_SPLASH_POSID : ConfigConsts.RELEASE_SPLASH_POSID);
        initLog();
        setBtnListener();
    }

    private void initView() {
        loadADBtn = findViewById(R.id.btn_load_splash_ad);
        showADBtn = findViewById(R.id.btn_show_splash_ad);
        loadAndShowADBtn = findViewById(R.id.btn_load_and_show_splash_ad);
    }

    private void setBtnListener() {
        loadADBtn.setOnClickListener(this::clickListener);
        showADBtn.setOnClickListener(this::clickListener);
        loadAndShowADBtn.setOnClickListener(this::clickListener);
    }

    private void clickListener(View view) {
        if (NoDoubleClickUtil.isDoubleClick()) {
            return;
        }
        if (view.getId() == R.id.btn_load_splash_ad) {
            loadAD(false);
        } else if (view.getId() == R.id.btn_show_splash_ad) {
            showAD();
        } else if (view.getId() == R.id.btn_load_and_show_splash_ad) {
            loadAD(true);
        }
    }

    @Override
    protected void loadAD(boolean withShow) {
        if (isLoading()) return;
        setLoading(true);

        SplashAdRequest.Builder splashAdBuilder = new SplashAdRequest.Builder();
        splashAdBuilder.setWaitTime(5000)
                .setAdCount(getAdCount())
                .setPosId(getPosId());
        SplashAd.load(splashAdBuilder.build(), new SplashAd.SplashAdLoadListener() {
            @Override
            public void onTimeOut() {
                setLoading(false);
                Log.e(TAG, "splash ad load timeout");
            }

            @Override
            public void onAdLoadError(int err, String msg) {
                setLoading(false);
                Log.e(TAG, "splash ad load err: " + err + " " + msg);
            }

            @Override
            public void onAdLoaded(SplashAd ad) {
                Log.i(TAG, "splash ad loaded");
                setLoading(false);

                if (withShow) {
                    if (mSplashAd != null && ad != mSplashAd) {
                        // 上一个广告还没有展示完成
                        showToast("已有未展示的广告");
                        return;
                    }
                    mSplashAd = ad;
                    showAD();
                } else {
                    mSplashAd = ad;
                }
            }
        });
    }

    @Override
    protected void showAD() {
        if (mSplashAd != null && mSplashAd.isValid()) {
            mSplashAd.setListener(new SplashAd.SplashAdListener() {
                @Override
                public void onAdSkip() {
                    Log.i(TAG, "onAdSkip");
                }

                @Override
                public void onAdShow() {
                    Log.i(TAG, "onAdShow");
                }

                @Override
                public void onAdClick() {
                    Log.i(TAG, "onAdClick");
                }

                @Override
                public void onAdClosed() {
                    Log.i(TAG, "onAdClosed");
                    mSplashAd = null;
                }

                @Override
                public void onAdError(int err, String msg) {
                    Log.e(TAG, "onAdError err: " + err + " " + msg);
                }
            });
            mSplashAd.show();
        }
    }
}