package com.tencent.klevinDemo.ads.rewardAD;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.tencent.klevin.KlevinManager;
import com.tencent.klevin.ads.ad.RewardAd;
import com.tencent.klevin.ads.ad.RewardAdRequest;
import com.tencent.klevinDemo.BuildConfig;
import com.tencent.klevinDemo.ConfigConsts;
import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.ads.BaseADActivity;
import com.tencent.klevinDemo.utils.EditTextUtil;
import com.tencent.klevinDemo.utils.NoDoubleClickUtil;

public class RewardADActivity extends BaseADActivity {

    private static final String TAG = ConfigConsts.DEMO_TAG + "_RewardAD";

    private EditText mRewardTimeView;
    private EditText mRewardTypeView;
    private CheckBox mMuteView;
    private CheckBox mPlayOnline;
    private Button loadADBtn;
    private Button showADBtn;

    private RewardAd mRewardAd;

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_ad);
        initView();
        boolean isTestEnv = getIntent().getBooleanExtra(getString(R.string.env_test), true);
        initCommonView(isTestEnv ? ConfigConsts.DEBUG_REWARD_POSID : ConfigConsts.RELEASE_REWARD_POSID);
        initLog();
        setBtnListener();
        initADConfig();
    }


    private void initView() {
        mRewardTimeView = findViewById(R.id.edt_reward_time);
        mRewardTypeView = findViewById(R.id.edt_reward_type);
        mMuteView = findViewById(R.id.cb_reward_auto_mute);
        mPlayOnline = findViewById(R.id.cb_reward_play_online);
        loadADBtn = findViewById(R.id.btn_load_reward_ad);
        showADBtn = findViewById(R.id.btn_show_reward_ad);

    }

    private void setBtnListener() {
        loadADBtn.setOnClickListener(this::clickListener);
        showADBtn.setOnClickListener(this::clickListener);
    }

    private void clickListener(View view) {
        if (NoDoubleClickUtil.isDoubleClick()) {
            return;
        }
        if (view.getId() == R.id.btn_load_reward_ad) {
            loadAD(false);
        } else if (view.getId() == R.id.btn_show_reward_ad) {
            showAD();
        }
    }

    private void initADConfig() {
        sharedPreferences = getSharedPreferences(getString(R.string.sp_name_reward_config), Context.MODE_PRIVATE);
        mRewardTimeView.setHint(R.string.reward_time_default);
        mRewardTypeView.setHint(Integer.toString(RewardAdRequest.TRIGGER_OTHER));
        loadADConfig();

    }

    private void loadADConfig() {
        mRewardTimeView.setText(sharedPreferences.getString(getString(R.string.input_reward_time), ""));
        mRewardTypeView.setText(sharedPreferences.getString(getString(R.string.input_reward_type), ""));
        mMuteView.setChecked(sharedPreferences.getBoolean(getString(R.string.autoMute), false));
        mPlayOnline.setChecked(sharedPreferences.getBoolean(getString(R.string.play_online), false));
    }

    private void saveADConfig() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (mRewardTimeView != null) {
            editor.putString(getString(R.string.input_reward_time), mRewardTimeView.getText().toString());
        }
        if (mRewardTypeView != null) {
            editor.putString(getString(R.string.input_reward_type), mRewardTypeView.getText().toString());
        }
        if (mMuteView != null) {
            editor.putBoolean(getString(R.string.autoMute), mMuteView.isChecked());
        }
        if (mPlayOnline != null) {
            editor.putBoolean(getString(R.string.play_online), mPlayOnline.isChecked());
        }
        editor.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveADConfig();
    }

    @Override
    protected void loadAD(boolean withShow) {
        if (isLoading()) return;
        setLoading(true);
        int rewardTime = 3;
        try {
            rewardTime = Integer.parseInt(EditTextUtil.getText(mRewardTimeView).trim());
        }catch (NumberFormatException ignore) {
        }
        int rewardTrigger = 5;
        try {
            rewardTrigger = Integer.parseInt(EditTextUtil.getText(mRewardTypeView).trim());
        } catch (NumberFormatException ignore) {
        }
        boolean rewardAutoMute = mMuteView.isChecked();
        boolean rewardPlayOnline = mPlayOnline.isChecked();

        RewardAdRequest.Builder rewardAdBuilder = new RewardAdRequest.Builder();
        rewardAdBuilder.autoMute(rewardAutoMute)
                .setRewardTime(rewardTime)
                .setRewardTrigger(rewardTrigger)
                .setPosId(getPosId())
                .setAdCount(getAdCount());
        RewardAd.load(rewardAdBuilder.build(), new RewardAd.RewardAdLoadListener() {

            @Override
            public void onVideoPrepared(RewardAd ad) {
                setLoading(false);
                Log.i(TAG, "reward video prepared");
                if (rewardPlayOnline) {
                    if (mRewardAd != null && ad != mRewardAd) {
                        // 之前的广告还没有展示完成
                        showToast("已有未展示的广告");
                        return;
                    }
                    mRewardAd = ad;
                    showAD();
                }
            }

            @Override
            public void onAdLoadError(int err, String msg) {
                setLoading(false);
                Log.e(TAG, "reward ad load err: " + err + " " + msg);
            }

            @Override
            public void onAdLoaded(RewardAd ad) {
                setLoading(false);
                Log.i(TAG, "reward ad loaded");
                mRewardAd = ad;
            }
        });
    }

    @Override
    protected void showAD() {
        if (mRewardAd != null && mRewardAd.isValid()) {
            mRewardAd.setListener(new RewardAd.RewardAdListener() {
                @Override
                public void onAdSkip() {
                    Log.i(TAG, "onAdSkip");
                }

                @Override
                public void onReward() {
                    Log.i(TAG, "onReward");
                }

                @Override
                public void onVideoComplete() {
                    Log.i(TAG, "onVideoComplete");
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
                    mRewardAd = null;
                }

                @Override
                public void onAdError(int err, String msg) {
                    Log.e(TAG, "onAdError err: " + err + " " + msg);
                }
            });
            mRewardAd.show();
        }
    }
}