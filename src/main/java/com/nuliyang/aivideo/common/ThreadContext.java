package com.nuliyang.aivideo.common;

public class ThreadContext {


    private static final ThreadLocal<String> TASK_ID_HOLDER = new ThreadLocal<>();

    public static void setData(String data) {
        TASK_ID_HOLDER.set(data);
    }

    public static String getData() {
        return TASK_ID_HOLDER.get();
    }

    public static void clear() {
        TASK_ID_HOLDER.remove();
    }
}
