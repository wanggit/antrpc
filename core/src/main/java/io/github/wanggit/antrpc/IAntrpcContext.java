package io.github.wanggit.antrpc;

import io.github.wanggit.antrpc.client.monitor.IRpcCallLogHolder;
import io.github.wanggit.antrpc.client.rate.IRateLimiting;
import io.github.wanggit.antrpc.client.spring.BeanContainer;
import io.github.wanggit.antrpc.client.spring.IOnFailHolder;
import io.github.wanggit.antrpc.client.zk.IZkClient;
import io.github.wanggit.antrpc.client.zk.lb.LoadBalancerHelper;
import io.github.wanggit.antrpc.client.zk.register.IZkRegisterHolder;
import io.github.wanggit.antrpc.client.zk.register.Register;
import io.github.wanggit.antrpc.client.zk.zknode.INodeHostContainer;
import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.breaker.ICircuitBreaker;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodecHolder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.server.invoker.IRpcRequestBeanInvoker;

public interface IAntrpcContext {

    /**
     * 是否已初始化完成
     *
     * @return
     */
    boolean isInited();

    ICodecHolder getCodecHolder();

    IOnFailHolder getOnFailHolder();

    /**
     * 获取接口频控管理器
     *
     * @return
     */
    IRateLimiting getRateLimiting();

    /**
     * 获取RpcClients
     *
     * @return
     */
    IRpcClients getRpcClients();

    /**
     * 获取Cglib对象的容器
     *
     * @return
     */
    BeanContainer getBeanContainer();

    /**
     * 获取配置对象
     *
     * @return
     */
    IConfiguration getConfiguration();

    /**
     * 获取熔断器
     *
     * @return
     */
    ICircuitBreaker getCircuitBreaker();

    IRpcCallLogHolder getRpcCallLogHolder();

    IZkClient getZkClient();

    IZkRegisterHolder getZkRegisterHolder();

    Register getRegister();

    LoadBalancerHelper getLoadBalancerHelper();

    INodeHostContainer getNodeHostContainer();

    IZkNodeBuilder getZkNodeBuilder();

    IRpcRequestBeanInvoker getRpcRequestBeanInvoker();

    /** 初始化, 再设置完 Configuration 之后再进行初始化 */
    void init();

    void startServer();
}
