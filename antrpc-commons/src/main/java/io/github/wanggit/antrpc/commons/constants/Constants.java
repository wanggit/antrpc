package io.github.wanggit.antrpc.commons.constants;

public interface Constants {

    /** 在System.Properties中获取RPC负载均衡实现类的类全名 */
    String RPC_LOAD_BALANCER_PROP_NAME = "rpc.load.balancer";

    /** 在System.Properties中获取RPC端口 */
    String RPC_PORT_PROP_NAME = "rpc.port";

    /** 在System.Properties中获取监控平台的地址, 多个使用逗号间隔. */
    String RPC_MONITOR_PROP_NAME = "rpc.monitor.host";
}
