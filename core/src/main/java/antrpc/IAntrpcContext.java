package antrpc;

import antrpc.client.monitor.IRpcCallLogHolder;
import antrpc.client.rate.IRateLimiting;
import antrpc.client.spring.BeanContainer;
import antrpc.client.spring.IOnFailHolder;
import antrpc.client.zk.IZkClient;
import antrpc.client.zk.lb.LoadBalancerHelper;
import antrpc.client.zk.register.IZkRegisterHolder;
import antrpc.client.zk.register.Register;
import antrpc.client.zk.zknode.INodeHostContainer;
import antrpc.client.zk.zknode.IZkNodeBuilder;
import antrpc.commons.IRpcClients;
import antrpc.commons.breaker.ICircuitBreaker;
import antrpc.commons.codec.cryption.ICodecHolder;
import antrpc.commons.config.IConfiguration;
import antrpc.server.invoker.IRpcRequestBeanInvoker;

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
