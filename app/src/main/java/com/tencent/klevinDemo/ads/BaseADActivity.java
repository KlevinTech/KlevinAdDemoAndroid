package com.tencent.klevinDemo.ads;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.tencent.klevinDemo.BaseActivity;
import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.view.AdCommonView;

import java.util.Objects;

/**
 * 封装了Log, PosID,Count和标题栏
 * <p>
 * 在子类的onCreate中在setContentView()后调用initLog()和initCommonView()即可
 */
public abstract class BaseADActivity extends BaseActivity {

    private boolean isAdLoading = false;
    private MenuItem loadingItem;
    private AdCommonView commonView;

    private String buttonName;

    private ActionBar bar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        setBarShow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        commonView.saveADConfig();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setBarShow();
        super.onConfigurationChanged(newConfig);
    }

    private void setBarShow() {
        //横屏隐藏标题栏
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE: {
                bar.hide();
                break;
            }
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_SQUARE:
            case Configuration.ORIENTATION_UNDEFINED:
                bar.show();
                break;
        }
    }

    public boolean isLoading() {
        return isAdLoading;
    }

    public void setLoading(boolean isADShowing) {
        this.isAdLoading = isADShowing;
        setLoadingState(isADShowing);
    }

    /**
     * @param posId 传入默认的posId
     */
    protected void initCommonView(long posId) {
        commonView = findViewById(R.id.adCommonView);
        commonView.initConfig(buttonName, posId);
    }

    protected Long getPosId() {
        return commonView.getPosId();
    }

    protected int getAdCount() {
        return commonView.getAdCount();
    }

    protected void loadAD(boolean withShow) {
    }

    protected void showAD() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_loading, menu);
        loadingItem = menu.findItem(R.id.menu_loading_item);
        return true;
    }

    public void setLoadingState(boolean refreshing) {
        runOnUiThread(() -> {
            if (loadingItem != null) {
                if (refreshing) {
                    loadingItem.setActionView(R.layout.actionbar_indeterminate_progress);
                    loadingItem.setVisible(true);
                } else {
                    loadingItem.setVisible(false);
                    loadingItem.setActionView(null);
                }
            }
        });
    }

    private void initActionBar() {
        buttonName = getIntent().getStringExtra(getString(R.string.intent_button_name));
        bar = Objects.requireNonNull(getSupportActionBar());
        bar.setTitle(buttonName);
    }
}