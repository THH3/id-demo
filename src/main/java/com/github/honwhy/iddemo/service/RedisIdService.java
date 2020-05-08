package com.github.honwhy.iddemo.service;

import com.relops.snowflake.Snowflake;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RedisIdService {

    private static final String DATE_NODE = "uuid:node";
    private final RedisTemplate redisTemplate;

    private Snowflake snowflake;

    public RedisIdService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        Long workerId = redisTemplate.opsForValue().increment(DATE_NODE);
        workerId = workerId % 1024;
        snowflake = new Snowflake(workerId.intValue());
    }

    public Long nextId() {
        return snowflake.next();
    }
}
