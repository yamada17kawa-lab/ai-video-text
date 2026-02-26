package com.nuliyang.aivideo.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;


//    public void add(String key, String value, long timeout){
//        stringRedisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
//    }

    public void add(String key, String value){
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public String get(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key){
        stringRedisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }



    //此项目非大项目，所以直接使用粗鲁的方法
    public Map<String, Object> getAll(){
        Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").count(1000).build());
            while (cursor.hasNext()) {
                keysTmp.add(new String(cursor.next()));
            }
            return keysTmp;
        });

        Map<String, Object> map = new HashMap<>();
        if (!keys.isEmpty()){
            for (String key : keys){
                String value = stringRedisTemplate.opsForValue().get(key);
                map.put(key, value);
            }
        }
        return map;
    }



    /**
     * 获取第一个键值对
     * @return 第一个键值对，如果Redis为空则返回null
     */
    public Map.Entry<String, Object> getFirstEntry() {
        Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").count(1000).build());
            while (cursor.hasNext()) {
                keysTmp.add(new String(cursor.next()));
            }
            return keysTmp;
        });

        if (keys.isEmpty()) {
            return null;
        }

        // 获取第一个key（按自然排序）
        String firstKey = keys.stream()
                .sorted()
                .findFirst()
                .orElse(null);

        if (firstKey != null) {
            String value = stringRedisTemplate.opsForValue().get(firstKey);
            return new AbstractMap.SimpleEntry<>(firstKey, value);
        }

        return null;
    }

}
