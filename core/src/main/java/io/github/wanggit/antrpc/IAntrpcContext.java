package io.github.wanggit.antrpc;

import io.github.wanggit.antrpc.client.monitor.IRpcCallLogHolder;
import io.github.wanggit.antrpc.client.rate.IRateLimiting;
import io.github.wanggit.antrpc.client.spring.BeanContainer;
import io.github.wanggit.antrpc.client.spring.IOnFailHolder;
import io.github.wanggit.antrpc.client.zk.IZkClient;
import io.github.wanggit.antrpc.client.zk.lb.LoadBalancerHelper;
import io.github.wanggit.antrpc.client.zk.listener.Listener;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.client.zk.register.IZkRegisterHolder;
import io.github.wanggit.antrpc.client.zk.zknode.INodeHostContainer;
import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeKeeper;
import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.breaker.ICircuitBreaker;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodecHolder;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.server.invoker.IRpcRequestBeanInvoker;
import org.springframework.context.ConfigurableApplicationContext;

public interface IAntrpcContext {

    /**
     * 是否已初始化完成
     *
     * @return
     */
    boolean isInited();

    IZkNodeKeeper getZkNodeKeeper();

    ISerializerHolder getSerializerHolder();

    ICodecHolder getCodecHolder();

    IOnFailHolder getOnFailHolder();

    /**
     * 获取接口频控管理器
     *
     * @return 频控管理器
     */
    IRateLimiting getRateLimiting();

    /**
     * 获取RpcClients
     *
     * @return RpcClients
     */
    IRpcClients getRpcClients();

    /**
     * 获取Cglib对象的容器
     *
     * @return Cglib动态对象的容器
     */
    BeanContainer getBeanContainer();

    /**
     * 获取配置对象
     *
     * @return 配置对象
     */
    IConfiguration getConfiguration();

    /**
     * 获取熔断器
     *
     * @return 熔断器
     */
    ICircuitBreaker getCircuitBreaker();

    IRpcCallLogHolder getRpcCallLogHolder();

    IZkClient getZkClient();

    IZkRegisterHolder getZkRegisterHolder();

    IRegister getRegister();

    Listener getListener();

    LoadBalancerHelper getLoadBalancerHelper();

    INodeHostContainer getNodeHostContainer();

    IZkNodeBuilder getZkNodeBuilder();

    IRpcRequestBeanInvoker getRpcRequestBeanInvoker();

    /**
     * 初始化, 再设置完 Configuration 之后再进行初始化
     *
     * @param applicationContext Spring应用上下文
     */
    void init(ConfigurableApplicationContext applicationContext);
}
