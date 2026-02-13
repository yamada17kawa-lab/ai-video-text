package com.nuliyang.aivideo.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaskIdStore {

    private final static Map<String, Object> taskMap = new ConcurrentHashMap<>();

    public static void put(String taskId, Object value) {
        taskMap.put(taskId, value);
    }

    public static Object get(String taskId) {
        return taskMap.get(taskId);
    }

    public static Map<String, Object> getAll() {
        return Map.copyOf(taskMap);
    }

    public static boolean containsKey(String taskId) {
        return taskMap.containsKey(taskId);
    }

    public static void remove(String taskId) {
        taskMap.remove(taskId);
    }

    public static int size() {
        return taskMap.size();
    }


    public static void clear() {
        taskMap.clear();
    }
}
