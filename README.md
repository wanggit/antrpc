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


##### TODO
1. 在RPC请求发出与接收到RPC请求，都要记录日志
2. 处理服务提供方的异常消息，现在服务提供方发生异常会被认为是连接问题需要处理。
3. 兼容protobuf协议
4. 心跳处理

---
1. 注意```curator```包与```Zookeeper```的兼容性；
http://curator.apache.org/zk-compatibility.html
```xml
<properties>
    <curator-recipes.version>2.13.0</curator-recipes.version>
</properties>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>${curator-recipes.version}</version>
</dependency>
```
经测试兼容zookeeper 3.4.5版本
