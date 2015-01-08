package com.dlink.mydlinkbase;

import android.util.Log;

public class Loger {

    private static boolean CLOSE = false;

    public static void closeLoger() {
        CLOSE = true;
    }

    public static void openLoger() {
        CLOSE = false;
    }

    public static void v(String msg) {
        if (CLOSE)
            return;

        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        Log.v(className,
                String.format("[%s][%d]%s", ste.getMethodName(),
                        ste.getLineNumber(), msg));
    }

    public static void v(String tag, String msg) {
        if (CLOSE)
            return;
        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        String newMsg = String.format("[%s][%s][%d]%s", className, ste.getMethodName(),
                ste.getLineNumber(), msg);
        Log.v(tag, newMsg);
    }

    public static void d(String msg) {
        if (CLOSE)
            return;

        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        Log.d(className,
                String.format("[%s][%d]%s", ste.getMethodName(),
                        ste.getLineNumber(), msg));
    }

    public static void d(String tag, String msg) {
        if (CLOSE)
            return;
        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        String newMsg = String.format("[%s][%s][%d]%s", className, ste.getMethodName(),
                ste.getLineNumber(), msg);
        Log.d(tag, newMsg);
    }

    public static void i(String msg) {
        if (CLOSE)
            return;

        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];

        Log.i(className,
                String.format("[%s][%d]%s", ste.getMethodName(),
                        ste.getLineNumber(), msg));
    }

    public static void i(String tag, String msg) {
        if (CLOSE)
            return;
        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        String newMsg = String.format("[%s][%s][%d]%s", className, ste.getMethodName(),
                ste.getLineNumber(), msg);
        Log.i(tag, newMsg);
    }

    public static void w(String msg) {
        if (CLOSE)
            return;

        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];

        Log.w(className,
                String.format("[%s][%d]%s", ste.getMethodName(),
                        ste.getLineNumber(), msg));
    }

    public static void w(String tag, String msg) {
        if (CLOSE)
            return;
        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        String newMsg = String.format("[%s][%s][%d]%s", className, ste.getMethodName(),
                ste.getLineNumber(), msg);
        Log.w(tag, newMsg);
    }

    public static void e(String msg) {
        if (CLOSE)
            return;

        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        Log.e(className,
                String.format("[%s][%d]%s", ste.getMethodName(),
                        ste.getLineNumber(), msg));
    }

    public static void e(String tag, String msg) {
        if (CLOSE)
            return;
        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        String newMsg = String.format("[%s][%s][%d]%s", className, ste.getMethodName(),
                ste.getLineNumber(), msg);
        Log.e(tag, newMsg);
    }

    public static void e(String tag, String msg, Exception e) {
        if (CLOSE)
            return;
        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement ste = stack[1];
        String clips[] = ste.getFileName().split("\\.");
        String className = clips[0];
        String newMsg = String.format("[%s][%s][%d]%s", className, ste.getMethodName(),
                ste.getLineNumber(), msg);
        Log.e(tag, newMsg, e);
    }

}
