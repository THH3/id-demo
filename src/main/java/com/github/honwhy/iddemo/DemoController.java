package com.github.honwhy.iddemo;

import com.github.honwhy.iddemo.service.RedisIdService;
import com.github.honwhy.iddemo.service.ZookeeperIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    private RedisIdService redisIdService;
    @Autowired
    private ZookeeperIdService zookeeperIdService;

    @GetMapping("/redis/id")
    public Long getId() {
        return redisIdService.nextId();
    }

    @GetMapping("/zk/id")
    public Long getId2() {
        return zookeeperIdService.nextId();
    }
}
