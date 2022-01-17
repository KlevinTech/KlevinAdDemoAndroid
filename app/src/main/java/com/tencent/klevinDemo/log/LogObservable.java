package com.tencent.klevinDemo.log;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.tencent.klevin.base.log.IARMLogPrinter;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

public class LogObservable implements IARMLogPrinter {
    private static final int LOG_MAX_COUNT = 100;

    private static boolean logDisplay = true;

    public static void setLogDisplay(boolean isVisible) {
        LogObservable.logDisplay = isVisible;
    }

    public static boolean isLogDisplay() {
        return logDisplay;
    }

    private final LinkedList<WeakReference<LogObserver>> observers = new LinkedList<>();

    private final LinkedList<LogMsg> logs = new LinkedList<>();

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            printLog((LogMsg) msg.obj);
        }
    };

    private LogObservable() {
    }

    public static LogObservable get() {
        return Holder.instance;
    }

    @Override
    public void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    @Override
    public void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    @Override
    public void s(String tag, String msg) {
        print(tag, msg);
        Log.e(tag, msg);
    }

    private void print(String tag, String msg) {
        Message message = handler.obtainMessage();
        message.obj = new LogMsg(tag, msg);
        handler.sendMessage(message);
    }

    private void printLog(LogMsg msg) {
        if (logs.size() >= LOG_MAX_COUNT) {
            logs.removeLast();
        }
        logs.addFirst(msg);

        for (WeakReference<LogObserver> ref : observers) {
            LogObserver observer = ref.get();
            if (observer != null) {
                observer.addLog(msg);
            }
        }
    }

    public void clearLogs() {
        logs.clear();

        for (WeakReference<LogObserver> ref : observers) {
            LogObserver observer = ref.get();
            if (observer != null) {
                observer.clearLog();
            }
        }
    }

    public void registerObserver(LogObserver observer) {
        if (observer == null) return;
        for (WeakReference<LogObserver> ref : observers) {
            if (ref != null && ref.get() == observer) {
                return;
            }
        }
        observers.addFirst(new WeakReference<>(observer));
    }

    public void unregisterObserver(LogObserver observer) {
        if (observer == null) return;
        for (Iterator<WeakReference<LogObserver>> iterator = observers.iterator(); iterator.hasNext(); ) {
            WeakReference<LogObserver> ref = iterator.next();
            if (ref == null || ref.get() == observer) {
                iterator.remove();
                break;
            }
        }
    }

    public LinkedList<LogMsg> logs() {
        return logs;
    }

    interface LogObserver {
        void addLog(LogMsg log);

        void clearLog();
    }

    private static class Holder {
        private static final LogObservable instance = new LogObservable();
    }

    static class LogMsg {
        public String tag;
        public String msg;

        public LogMsg(String tag, String msg) {
            this.tag = tag;
            this.msg = msg;
        }

        @NonNull
        @Override
        public String toString() {
            return msg;
        }
    }
}
