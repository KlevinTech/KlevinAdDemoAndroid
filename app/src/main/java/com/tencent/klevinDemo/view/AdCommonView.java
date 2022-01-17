package com.tencent.klevinDemo.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.utils.EditTextUtil;

public class AdCommonView extends ConstraintLayout {
    private static final String split = ";";

    private EditText mPosIdView;
    private EditText mAdCountView;

    private String buttonName;

    private SharedPreferences sharedPreferences;

    public AdCommonView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public AdCommonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_ad_common, this);
        mPosIdView = findViewById(R.id.edt_posId);
        mAdCountView = findViewById(R.id.edt_ad_count);
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.sp_name_ad_config), Context.MODE_PRIVATE);
    }

    public void initConfig(String buttonName, Long defaultPosID) {
        this.buttonName = buttonName;
        mPosIdView.setHint(Long.toString(defaultPosID));
        mAdCountView.setHint(getContext().getString(R.string.adCount_default));
        loadADConfig();
    }

    public Long getPosId() {
        String str = EditTextUtil.getText(mPosIdView).trim();
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ignore) {
        }
        return 0L;
    }

    public int getAdCount() {
        String str = EditTextUtil.getText(mAdCountView).trim();
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ignore) {
        }
        return 0;
    }

    private void loadADConfig() {
        //以;分割同时存两个
        String src = sharedPreferences.getString(buttonName, split);
        String[] config = src.split(split);
        String posId, adCount;
        switch (config.length) {
            case 1: {
                if (split.charAt(0) == src.charAt(0)) {
                    posId = "";
                    adCount = config[0];
                } else {
                    posId = config[0];
                    adCount = "";
                }
                break;
            }
            case 2: {
                posId = config[0];
                adCount = config[1];
                break;
            }
            default: {
                posId = "";
                adCount = "";
            }
        }
        mPosIdView.setText(posId);
        mAdCountView.setText(adCount);
    }

    public void saveADConfig() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder builder = new StringBuilder();
        if (mPosIdView != null) {
            builder.append(mPosIdView.getText());
        }
        builder.append(split);
        if (mAdCountView != null) {
            builder.append(mAdCountView.getText());
        }
        editor.putString(buttonName, builder.toString());
        editor.apply();
    }
}
