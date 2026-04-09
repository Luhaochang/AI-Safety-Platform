package com.naic.api.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author HuZhenSha
 * @since 2021/10/14
 */
@Service
@AllArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisClient;

    public void set(String key, String value, Duration timeout) {
        Assert.notNull(timeout, "Timeout must not be null!");
        redisClient.opsForValue().set(key, value, timeout);
    }

    public String get(String key){
        return redisClient.opsForValue().get(key);
    }

    public Boolean hasKey(String key){
        return redisClient.hasKey(key);
    }

    public void remove(String key){
        redisClient.delete(key);
    }

    // 获取键的过期时间，单位为秒
    public Long expireTimeGet(String key){
        return redisClient.getExpire(key, TimeUnit.SECONDS);
    }

    public Map<String,Double> getZset(String key){
        Map<String,Double> result=new HashMap<>();
        ZSetOperations<String, String> zSetOps = redisClient.opsForZSet();
        Set<String> zSetMembers = zSetOps.range(key, 0, -1);
        for (String member : zSetMembers) {
            Double score = zSetOps.score(key, member);
            result.put(member,score);
        }
        return result;
    }

}
