package io.github.wanggit.antrpc;

import io.github.wanggit.antrpc.client.RpcClients;
import io.github.wanggit.antrpc.client.monitor.IRpcCallLogHolder;
import io.github.wanggit.antrpc.client.monitor.RpcCallLogHolder;
import io.github.wanggit.antrpc.client.rate.IRateLimiting;
import io.github.wanggit.antrpc.client.rate.RateLimiting;
import io.github.wanggit.antrpc.client.spring.*;
import io.github.wanggit.antrpc.client.zk.IZkClient;
import io.github.wanggit.antrpc.client.zk.ZkClient;
import io.github.wanggit.antrpc.client.zk.lb.LoadBalancerHelper;
import io.github.wanggit.antrpc.client.zk.listener.Listener;
import io.github.wanggit.antrpc.client.zk.listener.ZkListener;
import io.github.wanggit.antrpc.client.zk.register.IZkRegisterHolder;
import io.github.wanggit.antrpc.client.zk.register.Register;
import io.github.wanggit.antrpc.client.zk.register.ZkRegisterHolder;
import io.github.wanggit.antrpc.client.zk.zknode.*;
import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.breaker.CircuitBreaker;
import io.github.wanggit.antrpc.commons.breaker.ICircuitBreaker;
import io.github.wanggit.antrpc.commons.codec.cryption.CodecHolder;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodecHolder;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;
import io.github.wanggit.antrpc.commons.codec.serialize.SerializerHolder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.server.IServer;
import io.github.wanggit.antrpc.server.RpcServer;
import io.github.wanggit.antrpc.server.exception.RpcServerStartErrorException;
import io.github.wanggit.antrpc.server.invoker.IRpcRequestBeanInvoker;
import io.github.wanggit.antrpc.server.invoker.RpcRequestBeanInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

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

    private ISerializerHolder serializerHolder;

    private Listener listener;

    private IZkNodeKeeper zkNodeKeeper;

    private IOnFailProcessor onFailProcessor;

    private IRpcAutowiredProcessor rpcAutowiredProcessor;

    public AntrpcContext(IConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isInited() {
        return inited.get();
    }

    @Override
    public IZkNodeKeeper getZkNodeKeeper() {
        return zkNodeKeeper;
    }

    @Override
    public ISerializerHolder getSerializerHolder() {
        return serializerHolder;
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

    @Override
    public Listener getListener() {
        return listener;
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

    @Override
    public void init(ConfigurableApplicationContext applicationContext) {
        if (inited.compareAndSet(false, true)) {
            long start = System.currentTimeMillis();
            this.initRpcRequestBeanInvoker(applicationContext);
            this.initRpcCallLogHolder(applicationContext, configuration);
            this.rateLimiting = new RateLimiting();
            this.onFailHolder = new OnFailHolder();
            this.initOnFailProcessor(applicationContext, onFailHolder);
            this.initZkClient(configuration);
            this.initLoadBalancerHelper(configuration);
            this.initNodeHostContainer(configuration, loadBalancerHelper);
            this.initZkNodeBuilder(zkClient, nodeHostContainer);
            this.initZkNodeKeeper(applicationContext, zkClient, zkNodeBuilder);
            this.initZkRegisterHolder(register, zkNodeBuilder, zkClient);
            this.initListener(applicationContext, zkClient, zkRegisterHolder, zkNodeBuilder);
            this.initRegister(applicationContext, zkNodeBuilder, zkRegisterHolder, configuration);
            this.initCircuitBreaker(configuration);
            this.initCodecHolder(configuration);
            this.initSerializerHolder(configuration);
            this.rpcClients = new RpcClients(configuration, codecHolder, serializerHolder);
            this.initRpcBeanContainer(
                    rateLimiting,
                    rpcCallLogHolder,
                    onFailHolder,
                    circuitBreaker,
                    rpcClients,
                    serializerHolder,
                    nodeHostContainer);
            this.initRpcAutowiredProcessor(applicationContext, beanContainer);
            this.startServer(configuration, codecHolder, rpcRequestBeanInvoker, serializerHolder);
            if (log.isInfoEnabled()) {
                log.info(
                        "Antrpc startup completed, "
                                + (System.currentTimeMillis() - start)
                                + "ms was used.");
            }
        }
    }

    void destroy() {
        this.server.close();
    }

    private void initRpcAutowiredProcessor(
            ConfigurableApplicationContext applicationContext, BeanContainer beanContainer) {
        IRpcAutowiredProcessor rpcAutowiredProcessor =
                applicationContext.getBean(IRpcAutowiredProcessor.class);
        rpcAutowiredProcessor.init(beanContainer);
    }

    private void initOnFailProcessor(
            ConfigurableApplicationContext applicationContext, IOnFailHolder onFailHolder) {
        IOnFailProcessor onFailProcessor = applicationContext.getBean(IOnFailProcessor.class);
        onFailProcessor.init(onFailHolder);
    }

    private void initZkNodeKeeper(
            ConfigurableApplicationContext applicationContext,
            IZkClient zkClient,
            IZkNodeBuilder zkNodeBuilder) {
        this.zkNodeKeeper = new ZkNodeKeeper(zkClient, zkNodeBuilder);
        applicationContext
                .getBeanFactory()
                .registerSingleton(IZkNodeKeeper.class.getName(), this.zkNodeKeeper);
        this.zkNodeKeeper.keep();
    }

    private void initListener(
            ConfigurableApplicationContext applicationContext,
            IZkClient zkClient,
            IZkRegisterHolder zkRegisterHolder,
            IZkNodeBuilder zkNodeBuilder) {
        this.listener = new ZkListener(zkClient, zkRegisterHolder, zkNodeBuilder);
        this.listener.listen();
        applicationContext
                .getBeanFactory()
                .registerSingleton(Listener.class.getName(), this.listener);
    }

    private void initRpcRequestBeanInvoker(ConfigurableApplicationContext applicationContext) {
        this.rpcRequestBeanInvoker = new RpcRequestBeanInvoker(applicationContext.getBeanFactory());
    }

    private void initRegister(
            ApplicationContext applicationContext,
            IZkNodeBuilder zkNodeBuilder,
            IZkRegisterHolder zkRegisterHolder,
            IConfiguration configuration) {
        register = applicationContext.getBean(Register.class);
        register.init(zkNodeBuilder, zkRegisterHolder, configuration);
    }

    private void initRpcBeanContainer(
            IRateLimiting rateLimiting,
            IRpcCallLogHolder rpcCallLogHolder,
            IOnFailHolder onFailHolder,
            ICircuitBreaker circuitBreaker,
            IRpcClients rpcClients,
            ISerializerHolder serializerHolder,
            INodeHostContainer nodeHostContainer) {
        this.beanContainer =
                new RpcBeanContainer(
                        rateLimiting,
                        rpcCallLogHolder,
                        onFailHolder,
                        circuitBreaker,
                        rpcClients,
                        serializerHolder,
                        nodeHostContainer);
    }

    private void initCircuitBreaker(IConfiguration configuration) {
        this.circuitBreaker = new CircuitBreaker(configuration);
    }

    private void initSerializerHolder(IConfiguration configuration) {
        try {
            this.serializerHolder = new SerializerHolder(configuration);
        } catch (Exception e) {
            throw new BeanCreationException(
                    "SerializerHolder initialization failed, please heartBeatWasContinuousLoss that the configured [antrpc.serialize-type = "
                            + configuration.getSerializeConfig().getType()
                            + "] was correct.");
        }
    }

    private void initCodecHolder(IConfiguration configuration) {
        try {
            this.codecHolder = new CodecHolder(configuration.getCodecConfig());
        } catch (Exception e) {
            throw new BeanCreationException(
                    "An exception occurred while initializing " + CodecHolder.class.getName(), e);
        }
    }

    private void initZkRegisterHolder(
            Register register, IZkNodeBuilder zkNodeBuilder, IZkClient zkClient) {
        this.zkRegisterHolder = new ZkRegisterHolder(register, zkNodeBuilder, zkClient);
    }

    private void initZkNodeBuilder(IZkClient zkClient, INodeHostContainer nodeHostContainer) {
        this.zkNodeBuilder = new ZkNodeBuilder(zkClient.getCurator(), nodeHostContainer);
    }

    private void initNodeHostContainer(
            IConfiguration configuration, LoadBalancerHelper loadBalancerHelper) {
        this.nodeHostContainer =
                new NodeHostContainer(loadBalancerHelper, configuration.getDirectHosts());
    }

    private void initLoadBalancerHelper(IConfiguration configuration) {
        loadBalancerHelper = new LoadBalancerHelper(configuration);
    }

    private void initZkClient(IConfiguration configuration) {
        zkClient = new ZkClient(configuration);
    }

    private void initRpcCallLogHolder(
            ApplicationContext applicationContext, IConfiguration configuration) {
        try {
            rpcCallLogHolder = new RpcCallLogHolder(configuration, applicationContext);
        } catch (Exception e) {
            throw new BeanCreationException(
                    "An exception occurred while initializing " + IRpcCallLogHolder.class.getName(),
                    e);
        }
    }

    public IServer getServer() {
        return server;
    }

    private void startServer(
            IConfiguration configuration,
            ICodecHolder codecHolder,
            IRpcRequestBeanInvoker rpcRequestBeanInvoker,
            ISerializerHolder serializerHolder) {
        if (configuration.isStartServer()) {
            this.server =
                    new RpcServer(
                            configuration, codecHolder, rpcRequestBeanInvoker, serializerHolder);
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
