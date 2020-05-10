package com.github.honwhy.iddemo.service;

import com.google.common.collect.Maps;
import com.relops.snowflake.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

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
            // get IP: prefer IPv4 address
            InetAddress address = InetAddress.getLocalHost();
            String host = address.getHostAddress();
            String childPath = host + ":" + serverPort;

            // parent director
            Stat stat = curatorFramework.checkExists().forPath(PARENT_PATH);
            if (stat == null) {
                //不存在根节点,机器第一次启动,创建/snowflake/ip:port-000000000,并上传数据
                createNode(curatorFramework, childPath);
            }
            List<String> children = curatorFramework.getChildren().forPath(PARENT_PATH);
            Map<String, Integer> nodeMap = Maps.newHashMap();//ip:port->00001
            Map<String, String> realNode = Maps.newHashMap();//ip:port->(ipport-000001)
            //存在根节点,先检查是否有属于自己的根节点
            for (String key : children) {
                String[] nodeKey = key.split("-");
                realNode.put(nodeKey[0], key);
                nodeMap.put(nodeKey[0], Integer.parseInt(nodeKey[1]));
            }
            Integer worker = nodeMap.get(childPath);
            if (worker == null) {
                String newNode = createNode(curatorFramework, childPath);
                String[] nodeKey = newNode.split("-");
                worker = Integer.parseInt(nodeKey[1]);
            }
            snowflake = new Snowflake(worker);
        } catch (Exception e) {
            log.error("start failed", e);
        }
    }

    public Long nextId() {
        return snowflake.next();
    }

    /**
     * 创建持久顺序节点
     *
     * @param curator
     * @return
     * @throws Exception
     */
    private String createNode(CuratorFramework curator, String childPath) throws Exception {
        try {
            return curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(PARENT_PATH + "/" + childPath + "-");
        } catch (Exception e) {
            log.error("create node error msg", e);
            throw e;
        }
    }
}
