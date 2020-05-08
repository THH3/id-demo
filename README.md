# id-demo
Snowflake powered by Redis or Zookeeper

## redis
```bash
docker pull docker.mirrors.ustc.edu.cn/library/redis
docker run -it -p 6379:6379 redis
```

## zookeeper
```bash
docker pull docker.mirrors.ustc.edu.cn/library/zookeeper
docker run -it -p 2181:2181 zookeeper
```

## test
```bash
curl  http://localhost:8080/redis/id
curl  http://localhost:8080/zk/id
```