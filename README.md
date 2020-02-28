# antrpc
Another Tool for RPC

**依赖于Spring**

1. 服务之间直连
2. 调用链路标记
3. 调用日志，可配置是否上报日志，可选是否上报参数。
4. 熔断
5. Jvm监控
6. 接口频控，每个节点单独配置接口频控。
7. 接口默认返回
8. 数据加解密
10. 日志消息通过kafka异步发送
13. 指定本服务要暴露的IP地址
15. 熔断器半开尝试
16. 日志收集器
17. 优雅停机
18. 服务控制台（未完成）
19. telnet命令运维（未完成测试用例）


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


