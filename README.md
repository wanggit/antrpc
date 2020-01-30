# antrpc
Another Tool for RPC

1. 服务之间直连
2. 调用链路标记
3. 调用日志，可配置是否上报日志，可选是否上报参数。
4. 熔断
5. Jvm监控
6. 接口频控，每个节点单独配置接口频控。
7. 接口默认返回
8. 数据加解密
9. 使用zk做配置中心（未完成）
10. 日志消息通过kafka异步发送
11. 兼容avro序列化与反序列化（未完成）
12. 兼容protobuf协议（未完成）
13. 指定本服务要暴露的IP地址
14. 接入Eureka注册中心（未完成）
15. 熔断器半开尝试（未完成）
16. 熔断器更换为Hystrix（未完成）

##### TODO


---
1. 注意```curator```包与```Zookeeper```的兼容性；
http://curator.apache.org/zk-compatibility.html
```xml
<properties>
    <curator-recipes.version>2.13.0</curator-recipes.version>
</properties>
```
```xml
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>${curator-recipes.version}</version>
</dependency>
```
经测试兼容zookeeper 3.4.5版本

spring-boot 测试过2.0.3.RELEASE与2.2.0.RELEASE两个版本

### 版本兼容表
|    AntRpc    | Spring Boot | Spring Kafka | curator-recipes | zookeeper | 
|--------------|-------------|--------------|-----------------|-----------|
| 2.0.0.RELEASE | 2.0.0.RELEASE | 2.1.4.RELEASE | 2.13.0      | 3.4.8     |



