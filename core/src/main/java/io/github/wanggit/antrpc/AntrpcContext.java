package io.github.wanggit.antrpc;

import io.github.wanggit.antrpc.client.RpcClients;
import io.github.wanggit.antrpc.client.monitor.IRpcCallLogHolder;
import io.github.wanggit.antrpc.client.rate.IRateLimiting;
import io.github.wanggit.antrpc.client.rate.RateLimiting;
import io.github.wanggit.antrpc.client.spring.BeanContainer;
import io.github.wanggit.antrpc.client.spring.IOnFailHolder;
import io.github.wanggit.antrpc.client.spring.OnFailHolder;
import io.github.wanggit.antrpc.client.zk.IZkClient;
import io.github.wanggit.antrpc.client.zk.ZkClient;
import io.github.wanggit.antrpc.client.zk.lb.LoadBalancerHelper;
import io.github.wanggit.antrpc.client.zk.register.IZkRegisterHolder;
import io.github.wanggit.antrpc.client.zk.register.Register;
import io.github.wanggit.antrpc.client.zk.register.ZkRegisterHolder;
import io.github.wanggit.antrpc.client.zk.zknode.INodeHostContainer;
import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostContainer;
import io.github.wanggit.antrpc.client.zk.zknode.ZkNodeBuilder;
import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.breaker.ICircuitBreaker;
import io.github.wanggit.antrpc.commons.codec.cryption.CodecHolder;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodecHolder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.server.IServer;
import io.github.wanggit.antrpc.server.RpcServer;
import io.github.wanggit.antrpc.server.exception.RpcServerStartErrorException;
import io.github.wanggit.antrpc.server.invoker.IRpcRequestBeanInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AntrpcContext implements IAntrpcContext {

    private BeanContainer beanContainer;

    private IConfiguration configuration;

    private ICircuitBreaker circuitBreaker;

    private IRpcCallLogHolder rpcCallLogHolder;

    private IZkClient zkClient;

    private IZkRegisterHolder zkRegisterHolder;

    private LoadBalancerHelper loadBalancerHelper;

    private INodeHostContainer nodeHostContainer;

    private IZkNodeBuilder zkNodeBuilder;

    private IRpcRequestBeanInvoker rpcRequestBeanInvoker;

    private AtomicBoolean inited = new AtomicBoolean(false);

    private Register register;

    private IServer server;

    private IRpcClients rpcClients;

    private IRateLimiting rateLimiting;

    private IOnFailHolder onFailHolder;

    private ICodecHolder codecHolder;

    public AntrpcContext(
            IConfiguration configuration,
            BeanContainer beanContainer,
            ICircuitBreaker circuitBreaker,
            IRpcCallLogHolder rpcCallLogHolder) {
        this.configuration = configuration;
        this.beanContainer = beanContainer;
        this.circuitBreaker = circuitBreaker;
        this.rpcCallLogHolder = rpcCallLogHolder;
    }

    @Override
    public boolean isInited() {
        return inited.get();
    }

    @Override
    public ICodecHolder getCodecHolder() {
        return codecHolder;
    }

    @Override
    public IOnFailHolder getOnFailHolder() {
        return onFailHolder;
    }

    @Override
    public IRateLimiting getRateLimiting() {
        return rateLimiting;
    }

    @Override
    public IRpcClients getRpcClients() {
        return rpcClients;
    }

    @Override
    public BeanContainer getBeanContainer() {
        return beanContainer;
    }

    @Override
    public IConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public ICircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    @Override
    public IRpcCallLogHolder getRpcCallLogHolder() {
        return rpcCallLogHolder;
    }

    @Override
    public IZkClient getZkClient() {
        return zkClient;
    }

    @Override
    public IZkRegisterHolder getZkRegisterHolder() {
        return zkRegisterHolder;
    }

    @Override
    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    @Override
    public LoadBalancerHelper getLoadBalancerHelper() {
        return loadBalancerHelper;
    }

    @Override
    public INodeHostContainer getNodeHostContainer() {
        return nodeHostContainer;
    }

    @Override
    public IZkNodeBuilder getZkNodeBuilder() {
        return zkNodeBuilder;
    }

    @Override
    public IRpcRequestBeanInvoker getRpcRequestBeanInvoker() {
        return rpcRequestBeanInvoker;
    }

    public void setRpcRequestBeanInvoker(IRpcRequestBeanInvoker rpcRequestBeanInvoker) {
        this.rpcRequestBeanInvoker = rpcRequestBeanInvoker;
    }

    @Override
    public void init() {
        if (inited.compareAndSet(false, true)) {
            circuitBreaker.init(configuration);
            this.rpcClients = new RpcClients(configuration);
            this.rateLimiting = new RateLimiting();
            this.onFailHolder = new OnFailHolder();
            this.initZkRegisterHolder();
            this.initLoadBalancerHelper(configuration);
            this.initNodeHostContainer(configuration);
            this.initZkClient(configuration);
            this.initZkNodeBuilder();
            this.initBeanContainer();
            this.initRpcCallLogHolder(configuration);
            this.initCodecHolder();
        }
    }

    void destroy() {
        this.server.close();
    }

    private void initCodecHolder() {
        try {
            this.codecHolder = new CodecHolder(configuration.getCodecConfig());
        } catch (Exception e) {
            throw new BeanCreationException(
                    "An exception occurred while initializing " + CodecHolder.class.getName(), e);
        }
    }

    private void initZkRegisterHolder() {
        this.zkRegisterHolder = new ZkRegisterHolder(this);
    }

    private void initZkNodeBuilder() {
        this.zkNodeBuilder = new ZkNodeBuilder(zkClient.getCurator(), nodeHostContainer);
    }

    private void initNodeHostContainer(IConfiguration configuration) {
        this.nodeHostContainer =
                new NodeHostContainer(this.loadBalancerHelper, configuration.getDirectHosts());
    }

    private void initLoadBalancerHelper(IConfiguration configuration) {
        loadBalancerHelper = new LoadBalancerHelper(configuration);
    }

    private void initZkClient(IConfiguration configuration) {
        zkClient = new ZkClient(configuration);
    }

    private void initBeanContainer() {
        beanContainer.setAntrpcContext(this);
    }

    private void initRpcCallLogHolder(IConfiguration configuration) {
        rpcCallLogHolder.init(configuration, rpcClients);
    }

    public IServer getServer() {
        return server;
    }

    @Override
    public void startServer() {
        if (configuration.isStartServer()) {
            this.server = new RpcServer(this);
            try {
                this.server.open(configuration.getPort());
                RpcServer rpcServer = (RpcServer) this.server;
                rpcServer.setActive();
            } catch (InterruptedException e) {
                throw new RpcServerStartErrorException("Antrpc server failed to start.", e);
            }
        }
    }
}
