package com.tencent.klevinDemo.ads.interstitialAD;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.tencent.klevin.KlevinManager;
import com.tencent.klevin.ads.ad.InterstitialAd;
import com.tencent.klevin.ads.ad.InterstitialAdRequest;
import com.tencent.klevinDemo.BuildConfig;
import com.tencent.klevinDemo.ConfigConsts;
import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.ads.BaseADActivity;
import com.tencent.klevinDemo.utils.NoDoubleClickUtil;
import com.tencent.klevinDemo.utils.PxUtil;

public class InterstitialADActivity extends BaseADActivity {

    private static final String TAG = ConfigConsts.DEMO_TAG + "Interstitial";

    private Button loadADBtn;
    private Button showADBtn;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial_ad);
        initView();
        initLog();
        boolean isTestEnv = getIntent().getBooleanExtra(getString(R.string.env_test), true);
        initCommonView(isTestEnv ? ConfigConsts.DEBUG_INTERSTITIAL_POSID : ConfigConsts.RELEASE_INTERSTITIAL_POSID);
        setBtnListener();
    }

    private void initView() {
        loadADBtn = findViewById(R.id.btn_load_interstitial_ad);
        showADBtn = findViewById(R.id.btn_show_interstitial_ad);
    }

    private void setBtnListener() {
        loadADBtn.setOnClickListener(this::clickListener);
        showADBtn.setOnClickListener(this::clickListener);
    }

    private void clickListener(View view) {
        if (NoDoubleClickUtil.isDoubleClick()) {
            return;
        }
        if (view.getId() == R.id.btn_load_interstitial_ad) {
            loadAD(false);
        } else if (view.getId() == R.id.btn_show_interstitial_ad) {
            showAD();
        }
    }

    @Override
    protected void loadAD(boolean withShow) {
        if (isLoading()) return;
        setLoading(true);
        InterstitialAdRequest.Builder interstitialBuilder = new InterstitialAdRequest.Builder();
        interstitialBuilder.setAdCount(getAdCount())
                .setPosId(getPosId());
        InterstitialAd.load(interstitialBuilder.build(), new InterstitialAd.InterstitialAdLoadListener() {
            @Override
            public void onAdLoadError(int err, String msg) {
                setLoading(false);
                Log.e(TAG, "interstitial ad load err: " + err + " " + msg);
            }

            @Override
            public void onAdLoaded(InterstitialAd ad) {
                setLoading(false);
                Log.i(TAG, "interstitial ad loaded");
                mInterstitialAd = ad;
            }
        });
    }

    @Override
    protected void showAD() {
        if (mInterstitialAd != null && mInterstitialAd.isValid()) {
            mInterstitialAd.setListener(new InterstitialAd.InterstitialAdListener() {
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
                }

                @Override
                public void onAdError(int err, String msg) {
                    Log.e(TAG, "onAdError err: " + err + " " + msg);
                }
            });
            mInterstitialAd.show();
        }
    }
}