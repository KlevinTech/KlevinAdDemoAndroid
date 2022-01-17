package com.tencent.klevinDemo.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.log.LogObservable.LogMsg;

import java.util.LinkedList;
import java.util.List;

public class LogView extends ConstraintLayout {

    private LogAdapter logAdapter;
    private RecyclerView logsView;

    public LogView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public LogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 设置全局Log显示
     *
     * @param isVisible true为显示
     */
    public void setLogDisplay(boolean isVisible) {
        LogObservable.setLogDisplay(isVisible);
        visibility(isVisible);
        registerObserver();
    }

    /**
     * 请在onCreate中调用
     */
    public void registerObserver() {
        if (LogObservable.isLogDisplay()) {
            LogObservable.get().registerObserver(logAdapter);
        }
    }

    /**
     * 请在onDestroy中调用
     */
    public void unregisterObserver() {
        LogObservable.get().unregisterObserver(logAdapter);
    }

    private void visibility(boolean visible) {
        this.setVisibility(visible ? VISIBLE : GONE);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_bottom_log, this);
        logsView = findViewById(R.id.recycler_bottom_logs);
        logsView.setVerticalScrollBarEnabled(true);
        logAdapter = new LogAdapter(LogObservable.get().logs());
        logsView.setAdapter(logAdapter);
        logsView.setLayoutManager(new LinearLayoutManager(context));
        logsView.setItemAnimator(null);//取消动画, 避免日志多时产生重叠

        findViewById(R.id.txt_log_title).setOnClickListener(v -> LogObservable.get().clearLogs());

        visibility(LogObservable.isLogDisplay());
    }


    class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> implements LogObservable.LogObserver {
        private static final int LOG_MAX_COUNT = 100;

        private final LinkedList<LogMsg> logs;

        public LogAdapter(List<LogMsg> preLog) {
            logs = new LinkedList<>(preLog);
        }

        @Override
        public void addLog(LogMsg log) {
            if (logs.size() >= LOG_MAX_COUNT) {
                logs.removeLast();
            }
            logs.addFirst(log);
            notifyItemInserted(0);
            logsView.scrollToPosition(0);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void clearLog() {
            logs.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LogAdapter.LogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View itemView = layoutInflater.inflate(R.layout.item_log, viewGroup, false);
            return new LogAdapter.LogViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull LogAdapter.LogViewHolder viewHolder, int i) {
            viewHolder.setLog(logs.get(i));
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        class LogViewHolder extends RecyclerView.ViewHolder {
            TextView txt;
            LogMsg msg;

            public LogViewHolder(View itemView) {
                super(itemView);
                txt = itemView.findViewById(R.id.txt_log);
            }

            public void setLog(LogMsg log) {
                msg = log;
                txt.setText(msg.toString());
            }
        }
    }


}
