package com.tencent.klevinDemo.ads.nativeAD;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tencent.klevinDemo.ConfigConsts;
import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.menu.BaseMenuActivity;
import com.tencent.klevinDemo.utils.NoDoubleClickUtil;

public class NativeMenuActivity extends BaseMenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListTexts(R.array.native_type);
    }

    @Override
    public void onClick(View v) {
        if (NoDoubleClickUtil.isDoubleClick()) {
            return;
        }
        int index = (int) v.getTag();//Strings.xml中的native_type数组下标
        long posId = isTestEnv ? ConfigConsts.DEBUG_NATIVE_POSID : ConfigConsts.RELEASE_NATIVE_POSID;
        Class<?> activity;
        switch (index) {
            case 0: {//测试
                activity = NativeADActivity.class;
                break;
            }
            case 1: {//效果展示
                activity = UnifiedFeedRecyclerActivity.class;
                break;
            }
            case 2: {//图片接入方式测试
                activity = UnifiedImageRecyclerTestActivity.class;
                break;
            }
            default: {
                throw new IllegalStateException("No such menu!");
            }
        }
        Intent start = new Intent(NativeMenuActivity.this, activity);
        start.putExtra(getString(R.string.intent_pod_id), posId);
        start.putExtra(getString(R.string.intent_button_name), listMenuAdapter.getText(index));
        startActivity(start);
    }
}