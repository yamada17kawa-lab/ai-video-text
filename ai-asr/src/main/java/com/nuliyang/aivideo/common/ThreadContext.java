package com.nuliyang.aivideo.common;

public class ThreadContext {


    // 在 ThreadContext.java 中添加
    private static final ThreadLocal<String> RESOURCE_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> FILE_NAME_HOLDER = new ThreadLocal<>();

    public static void setResourceId(String id) {
        RESOURCE_ID_HOLDER.set(id);
    }

    public static String getResourceId() {
        return RESOURCE_ID_HOLDER.get();
    }

    public static void setFileName(String name) {
        FILE_NAME_HOLDER.set(name);
    }

    public static String getFileName() {
        return FILE_NAME_HOLDER.get();
    }

    public static void clear() {
        RESOURCE_ID_HOLDER.remove();
        FILE_NAME_HOLDER.remove();
    }

}
