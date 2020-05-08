package com.github.honwhy.iddemo.service;

import com.relops.snowflake.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.List;

@Service
@Slf4j
public class ZookeeperIdService {
    private static final String PARENT_PATH = "/uuid";
    @Value("${server.port}")
    private Integer serverPort;

    private final CuratorFramework curatorFramework;
    private Snowflake snowflake;

    public ZookeeperIdService(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @PostConstruct
    public void init() {
        try {
            // parent director
            if (curatorFramework.checkExists().forPath(PARENT_PATH) == null) {
                curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath(PARENT_PATH);
            }
            // get IP: prefer IPv4 address
            InetAddress address = InetAddress.getLocalHost();
            String host = address.getHostAddress();
            String childPath = host + "_" + serverPort + "_";
            curatorFramework.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(PARENT_PATH + "/" + childPath);
            List<String> children = curatorFramework.getChildren().forPath(PARENT_PATH);
            if (!CollectionUtils.isEmpty(children)) {
                for (String path : children) {
                    if (path.startsWith(childPath)) {
                        int node = Integer.parseInt(path.replace(childPath, ""));
                        node = node % 1024;
                        snowflake = new Snowflake(node);
                    }
                }
            }
        } catch (Exception e) {
            log.error("start failed", e);
        }
    }

    public Long nextId() {
        return snowflake.next();
    }
}
