package com.tencent.klevinDemo.menu;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tencent.klevinDemo.BaseActivity;
import com.tencent.klevinDemo.R;

import java.util.Objects;

/**
 * 封装了按钮列表和标题栏功能
 * <p>
 * 使用:
 * 1. 在子类onCreate()中用setListTexts()传入标题数组;
 * 2. 重写OnClick()方法, 可通过View的Tag获取点击的按钮标题对应的标题数组下标.
 */
public abstract class BaseMenuActivity extends BaseActivity implements View.OnClickListener {

    private static final int DEFAULT_ID = -1;
    private @ArrayRes
    int textResId = DEFAULT_ID;

    protected boolean isTestEnv;

    protected ButtonRecyclerAdapter listMenuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTestEnv = getIntent().getBooleanExtra(getString(R.string.env_test), true);

        initActionBar();
        onOrientationChange();
    }

    private void initActionBar() {
        ActionBar bar = Objects.requireNonNull(getSupportActionBar());
        bar.setTitle(getIntent().getStringExtra(getString(R.string.intent_button_name)));
    }

    private void initRecyclerView() {
        RecyclerView recycler_native = findViewById(R.id.recycler_buttons);
        listMenuAdapter = new ButtonRecyclerAdapter();
        recycler_native.setAdapter(listMenuAdapter);
        recycler_native.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        listMenuAdapter.setClickListener(this);

    }

    protected void setListTexts(@ArrayRes int resId) {
        textResId = resId;
        listMenuAdapter.setTexts(getResources().getStringArray(resId));
    }

    /**
     * 屏幕方向改变时调用
     */
    protected void onOrientationChange() {
        setContentView(R.layout.activity_button_menu);
        initRecyclerView();
        initLog();
        if (textResId != DEFAULT_ID) {
            setListTexts(textResId);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onOrientationChange();
    }
}
