package com.tencent.klevinDemo;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tencent.klevinDemo.log.LogView;

/**
 * 封装Log和Toast功能
 */
public abstract class BaseActivity extends AppCompatActivity {

    private LogView logView;

    private Toast toast;

    protected void setLogDisplay(boolean isVisible) {
        if (logView != null) {
            logView.setLogDisplay(isVisible);
        }
    }

    /**
     * 请在onCreate中调用setContentView()后调用
     */
    protected void initLog() {
        logView = findViewById(R.id.logView);
        logView.registerObserver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logView != null) {
            logView.unregisterObserver();
        }
        toast = null;
    }

    public void showToast(CharSequence text) {
        if (toast != null) {
            toast.cancel();
        }
        (toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)).show();
    }

}
